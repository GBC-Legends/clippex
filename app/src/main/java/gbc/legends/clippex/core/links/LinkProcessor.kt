package gbc.legends.clippex.core.links

import android.content.Context
import gbc.legends.clippex.core.api.ApiRequestResult
import gbc.legends.clippex.core.api.platforms.ApiResponse
import kotlinx.coroutines.channels.Channel

interface LinkProcessor {
    fun canProcess(url: String): Boolean

    fun getFilename(renameInput: String, urlString: String): String

    fun getMime(filename: String): String

    suspend fun processLink(context: Context, url: String, filename: String, mime: String, channel: Channel<Int>): DownloadResult

    fun parseApiResponse(input: String): ApiResponse

    suspend fun requestToClippexApi(context: Context, url: String) : ApiRequestResult
}