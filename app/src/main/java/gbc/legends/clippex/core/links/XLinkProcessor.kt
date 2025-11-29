package gbc.legends.clippex.core.links

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import gbc.legends.clippex.core.api.ApiRequestResult
import gbc.legends.clippex.core.api.Failure
import gbc.legends.clippex.core.api.Success
import gbc.legends.clippex.core.api.platforms.XApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class XLinkProcessor() : GenericLinkProcessor() {
    private val xRegex = Regex(
        pattern = "^(https?://)?(www\\.)?(twitter\\.com|x\\.com)/.+",
        option = RegexOption.IGNORE_CASE
    )

    override fun canProcess(url: String): Boolean = xRegex.matches(url)

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun processLink(
        context: Context,
        url: String,
        filename: String,
        mime: String,
        channel: Channel<Int>
    ): DownloadResult {
        Log.d("XLinkProcessor", "Processing link: $url")
        return super.subprocessLink(context, url, filename, mime, channel)
    }

    override fun parseApiResponse(input: String): XApiResponse {
        return json.decodeFromString(XApiResponse.serializer(), input)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun requestToClippexApi(context: Context, url: String): ApiRequestResult {
        return withContext(Dispatchers.IO) {
            Log.d("XLinkProcessor", "Requesting to clippex api: $url")
            try {
                val apiUrl = URL(super.apiUrl)
                val connection = apiUrl.openConnection() as HttpURLConnection
                Log.d("XLinkProcessor", "Connection declared: $connection")

                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.doInput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connection.setRequestProperty("Accept", "application/json")

                val jsonBody = """{"url": "$url"}"""

                connection.outputStream.use { os ->
                    os.write(jsonBody.toByteArray(Charsets.UTF_8))
                    os.flush()
                }

                Log.d("XLinkProcessor", "Body written: $jsonBody")

                connection.connect()
                Log.d("XLinkProcessor", "Connection opened: $connection")

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw IOException("Error ${connection.responseCode}")
                }

                val json = connection.inputStream.bufferedReader().use { it.readText() }

                connection.disconnect()

                val xResponse = parseApiResponse(json)
                Success(xResponse)
            } catch (e: Exception) {
                e.printStackTrace()
                Failure("Download processing failed: ${e.message}", e)
            }
        }
    }
}