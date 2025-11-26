package gbc.legends.clippex.core.links

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class InstagramLinkProcessor : LinkProcessor {
    private val instagramRegex = Regex("(?:https?://)?(?:www\\.)?instagram\\.com/[^\\s/]+/[^\\s/]+")

    private val API_ENDPOINT = ""

    override fun canProcess(url: String): Boolean = instagramRegex.matches(url)

    override suspend fun processLink(context: Context, url: String): DownloadResult = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply { put("url", url) }.toString()

            val apiUrl = URL(API_ENDPOINT)
            val connection = apiUrl.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true

            connection.outputStream.bufferedWriter().use {
                it.write(payload)
                it.flush()
            }

            if (connection.responseCode !in 200..299) {
                return@withContext Failure(
                    "Backend API error: ${connection.responseCode} ${connection.responseMessage}"
                )
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)

            val directUrl = json.getString("downloadUrl")
            val fileName = json.getString("fileName")
            val mimeType = json.getString("mimeType")

            return@withContext downloadFile(context, directUrl, fileName, mimeType)

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext Failure("Instagram processing failed: ${e.message}", e)
        }
    }
}