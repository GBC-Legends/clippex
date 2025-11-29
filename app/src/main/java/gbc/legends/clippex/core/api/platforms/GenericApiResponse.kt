package gbc.legends.clippex.core.api.platforms

import gbc.legends.clippex.core.api.MediaOption
import kotlinx.serialization.Serializable

@Serializable
sealed interface ApiResponse {
    val result: ApiResult
}

interface ApiResult {
    val medias: List<MediaItem>
    val title: String
    val author: String
}

interface MediaItem {
    val url: String

    fun toMediaOption(author: String, title: String): MediaOption

    fun sanitizeFileName(fileName: String, maxLen: Int = 30): String {
        val cleaned = fileName
            .replace("\n", " ")
            .replace(Regex("[\\\\/:*?\"<>|]"), "")
            .trim()

        return if (cleaned.length <= maxLen) cleaned else cleaned.take(maxLen)
    }
}
