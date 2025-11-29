package gbc.legends.clippex.core.links

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.channels.Channel

class XLinkProcessor(override val url: String) : GenericLinkProcessor(url=url) {
    private val xRegex = Regex(
        pattern = "^(https?://)?(www\\.)?(twitter\\.com|x\\.com)/.+",
        option = RegexOption.IGNORE_CASE
    )

    private val API_ENDPOINT = ""

    override fun canProcess(url: String): Boolean = xRegex.matches(url)

    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun processLink(context: Context, filename: String, mime: String, channel: Channel<Int>) {
        Log.d("XLinkProcessor", "Processing link: $url")
        super._subprocessLink(context, filename, mime, channel)
    }
}