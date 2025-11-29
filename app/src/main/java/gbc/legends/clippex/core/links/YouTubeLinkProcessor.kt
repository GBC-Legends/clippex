package gbc.legends.clippex.core.links

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.channels.Channel

class YouTubeLinkProcessor() : GenericLinkProcessor() {
    private val youTubeRegex = Regex(
        pattern = "^(https?://)?(www\\.|m\\.)?(youtube\\.com/(watch\\?v=[\\w-]+|shorts/[\\w-]+)|youtu\\.be/[\\w-]+).*$",
        option = RegexOption.IGNORE_CASE
    )
    private val API_ENDPOINT = ""

    override fun canProcess(url: String): Boolean = youTubeRegex.matches(url)

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun processLink(context: Context, url: String, filename: String, mime: String, channel: Channel<Int>) : DownloadResult {
        Log.d("YoutubeLinkProcessor", "Processing link: $url")
        return super.subprocessLink(context, url, filename, mime, channel)
    }
}