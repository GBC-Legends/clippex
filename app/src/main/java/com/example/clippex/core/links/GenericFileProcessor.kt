package com.example.clippex.core.links

import android.content.Context
import android.os.Environment
import android.webkit.URLUtil
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class GenericFileProcessor : LinkProcessor {

    override fun canProcess(url: String): Boolean = true

    override suspend fun processLink(context: Context, url: String): DownloadResult = withContext(Dispatchers.IO) {
        try {
            val urlConnection = URL(url).openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.connect()

            if (urlConnection.responseCode in 200..299) {
                val inputStream = urlConnection.inputStream
                val contentType = urlConnection.contentType ?: "application/octet-stream"

                val disposition = urlConnection.getHeaderField("Content-Disposition")
                val fileName = disposition?.let {
                    val index = it.indexOf("filename=")
                    if (index > 0) it.substring(index + 9).trim('"') else null
                } ?: URLUtil.guessFileName(url, disposition, contentType)

                val targetDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "clippex"
                )
                if (!targetDir.exists()) targetDir.mkdirs()

                val outputFile = File(targetDir, fileName)
                FileOutputStream(outputFile).use { output ->
                    inputStream.use { input ->
                        input.copyTo(output)
                    }
                }

                DownloadResult.Success(outputFile, contentType)
            } else {
                DownloadResult.Failure("Server error: ${urlConnection.responseCode} ${urlConnection.responseMessage}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            DownloadResult.Failure("Download failed: ${e.message}", e)
        }
    }
}