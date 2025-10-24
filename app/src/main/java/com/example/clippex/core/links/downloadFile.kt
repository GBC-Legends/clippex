package com.example.clippex.core.links

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

suspend fun downloadFile(
    context: Context,
    fileUrl: String,
    fileName: String,
    mimeType: String
): DownloadResult = withContext(Dispatchers.IO) {
    try {
        val connection = URL(fileUrl).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connect()

        if (connection.responseCode !in 200..299) {
            return@withContext Failure(
                "Backend API error: ${connection.responseCode} ${connection.responseMessage}"
            )
        }

        val inputStream = connection.inputStream
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
        Success(outputFile, mimeType)
    } catch (e: Exception) {
        e.printStackTrace()
        return@withContext Failure("Generic processing failed: ${e.message}", e)
    }
}