package gbc.legends.clippex.core.links


import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import gbc.legends.clippex.core.api.ApiRequestResult
import gbc.legends.clippex.core.api.Failure
import gbc.legends.clippex.core.api.Success
import gbc.legends.clippex.core.api.platforms.YouTubeApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class YouTubeLinkProcessor : GenericLinkProcessor() {

    private val youTubeRegex = Regex(
        "^(https?://)?(www\\.|m\\.)?(youtube\\.com/(watch\\?v=[\\w-]+|shorts/[\\w-]+)|youtu\\.be/[\\w-]+).*$",
        RegexOption.IGNORE_CASE
    )

    override fun canProcess(url: String): Boolean = youTubeRegex.matches(url)
    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun requestToClippexApi(context: Context, url: String): ApiRequestResult {
        return withContext(Dispatchers.IO) {
            Log.d("YouTubeProcessor", "Fetching YouTube info from Clippex API: $url")
            try {
                val connection = URL(apiUrl).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.doInput = true
                connection.connectTimeout = 20000
                connection.readTimeout = 30000
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("User-Agent", "Clippex-App/1.0")

                val body = """{"url": "$url"}"""
                connection.outputStream.bufferedWriter().use { it.write(body) }

                if (connection.responseCode != 200) {
                    val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error body"
                    Log.e("YouTubeProcessor", "API Error ${connection.responseCode}: $errorBody")
                    return@withContext Failure("Server error: ${connection.responseCode}")
                }

                val jsonResponse = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d("YouTubeProcessor", "Raw response: $jsonResponse")

                val response = json.decodeFromString<YouTubeApiResponse>(jsonResponse)

                if (response.result.error) {
                    return@withContext Failure("API returned error: ${jsonResponse}")
                }

                Success(response)

            } catch (e: Exception) {
                Log.e("YouTubeProcessor", "Request failed", e)
                Failure("Network error: ${e.message}", e)
            }
        }
    }
    override fun parseApiResponse(input: String): YouTubeApiResponse {
        return json.decodeFromString(YouTubeApiResponse.serializer(), input)
    }
}