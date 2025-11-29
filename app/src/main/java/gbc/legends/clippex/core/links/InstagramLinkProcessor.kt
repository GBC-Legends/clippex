package gbc.legends.clippex.core.links

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.channels.Channel

class InstagramLinkProcessor(url: String) : GenericLinkProcessor(url=url) {
    private val instagramRegex = Regex(
        pattern = "^(https?://)?(www\\.|m\\.)?instagram\\.com/.*",
        option = RegexOption.IGNORE_CASE
    )

    private val API_ENDPOINT = ""

    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun processLink(context: Context, filename: String, mime: String, channel: Channel<Int>) {
        Log.d("InstagramLinkProcessor", "Processing link: $url")
        super._subprocessLink(context, filename, mime, channel)
    }

    override fun canProcess(url: String): Boolean = instagramRegex.matches(url)
}