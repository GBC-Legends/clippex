package gbc.legends.clippex.core.links

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.channels.Channel

class InstagramLinkProcessor() : GenericLinkProcessor() {
    private val instagramRegex = Regex(
        pattern = "^(https?://)?(www\\.|m\\.)?instagram\\.com/.*",
        option = RegexOption.IGNORE_CASE
    )

    private val API_ENDPOINT = ""

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun processLink(context: Context, url: String, filename: String, mime: String, channel: Channel<Int>): DownloadResult {
        Log.d("InstagramLinkProcessor", "Processing link: $url")
        return super.subprocessLink(context, url, filename, mime, channel)
    }

    override fun canProcess(url: String): Boolean = instagramRegex.matches(url)
}