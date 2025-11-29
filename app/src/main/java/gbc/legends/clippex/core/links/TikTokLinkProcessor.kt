package gbc.legends.clippex.core.links

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.channels.Channel

class TikTokLinkProcessor() : GenericLinkProcessor() {
    private val tiktokRegex = Regex(
        pattern = "^(https?://)?(www\\.|vm\\.)?tiktok\\.com/.+",
        option = RegexOption.IGNORE_CASE
    )

    private val API_ENDPOINT = ""

    override fun canProcess(url: String): Boolean = tiktokRegex.matches(url)

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun processLink(context: Context, url: String, filename: String, mime: String, channel: Channel<Int>): DownloadResult {
        Log.d("TikTokLinkProcessor", "Processing link: $url")
        return super.subprocessLink(context, url, filename, mime, channel)
    }
}