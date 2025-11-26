package gbc.legends.clippex.core.links

import android.content.Context
import android.os.Environment
import android.webkit.URLUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class GenericFileProcessor : LinkProcessor {

//    protected getDownloadPath() {
//
//    }

    override fun canProcess(url: String): Boolean = true

    override suspend fun processLink(context: Context, url: String): DownloadResult =
        withContext(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                if (connection.responseCode in 200..299) {
                    val inputStream = connection.inputStream
                    val contentType = connection.contentType ?: "application/octet-stream"

                    val disposition = connection.getHeaderField("Content-Disposition")
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

                    Success(outputFile, contentType)
                } else {
                    Failure("Server error: ${connection.responseCode} ${connection.responseMessage}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Failure("Download failed: ${e.message}", e)
            }
        }
}