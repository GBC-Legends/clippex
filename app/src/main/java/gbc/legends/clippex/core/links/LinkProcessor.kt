package gbc.legends.clippex.core.links

import android.content.Context
import kotlinx.coroutines.channels.Channel

interface LinkProcessor {
    fun canProcess(url: String): Boolean

    fun getFilename(renameInput: String, urlString: String): String

    fun getMime(filename: String): String

    suspend fun _subprocessLink(context: Context, filename: String, mime: String, channel: Channel<Int>): DownloadResult
}