package gbc.legends.clippex.core.links

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.channels.Channel

class TikTokLinkProcessor(override val url: String) : GenericLinkProcessor(url=url) {
    private val tiktokRegex = Regex(
        pattern = "^(https?://)?(www\\.|vm\\.)?tiktok\\.com/.+",
        option = RegexOption.IGNORE_CASE
    )

    private val API_ENDPOINT = ""

    override fun canProcess(url: String): Boolean = tiktokRegex.matches(url)

    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun processLink(context: Context, filename: String, mime: String, channel: Channel<Int>) {
        Log.d("TiktokLinkProcessor", "Processing link: $url")
        super._subprocessLink(context, filename, mime, channel)
    }
}