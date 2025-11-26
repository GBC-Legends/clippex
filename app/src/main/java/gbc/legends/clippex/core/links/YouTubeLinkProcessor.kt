package gbc.legends.clippex.core.links

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class YouTubeLinkProcessor : LinkProcessor {
    private val youTubeRegex = Regex(
        "(?:https?://)?(?:www\\.)?(youtube\\.com/(watch\\?v=|shorts/)|youtu\\.be/)[a-zA-Z0-9_-]+"
    )
    private val API_ENDPOINT = ""

    override fun canProcess(url: String): Boolean = youTubeRegex.matches(url)

    override suspend fun processLink(context: Context, url: String): DownloadResult =
        withContext(Dispatchers.IO) {
            try {
                val payload = JSONObject().apply { put("url", url) }.toString()

                val connection = (URL(API_ENDPOINT).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                    doOutput = true
                }

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
                return@withContext Failure("YouTube processing failed: ${e.message}", e)
            }
        }
}