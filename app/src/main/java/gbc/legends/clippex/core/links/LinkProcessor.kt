package gbc.legends.clippex.core.links

import android.content.Context

interface LinkProcessor {
    fun canProcess(url: String): Boolean
    suspend fun processLink(context: Context, url: String): DownloadResult
}