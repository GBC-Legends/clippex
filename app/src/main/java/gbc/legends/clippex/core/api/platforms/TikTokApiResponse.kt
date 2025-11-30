package gbc.legends.clippex.core.api.platforms

import gbc.legends.clippex.core.api.MediaOption
import kotlinx.serialization.Serializable

@Serializable
data class TikTokApiResponse(
    override val result: TikTokApiResult
) : ApiResponse

@Serializable
data class TikTokApiResult(
    override val author: String = "Unknown",
    override val title: String,
    override val medias: List<TikTokMediaItem>,
    val thumbnail: String? = "",
    val duration: Int? = null,
    val source: String? = "tiktok",
    val id: String? = null,
    val error: Boolean = false,
    val unique_id: String? = null,
    val time_end: Int? = null,
    val type: String? = "multiple",
    val url: String? = null
) : ApiResult

@Serializable
data class TikTokMediaItem(
    override val url: String,
    val extension: String? = "mp4",
    val quality: String? = "hd_no_watermark",
    val width: Int? = null,
    val height: Int? = null,
    val type: String? = "video",
    val data_size: Long? = null,
    val duration: Int? = null
) : MediaItem {
    override fun toMediaOption(author: String, title: String): MediaOption {
        val fileName = sanitizeFileName("$author - $title") + "." + (extension ?: "mp4")
        return MediaOption(
            label = quality ?: "HD",
            info = "${type ?: "video"} ${extension ?: "mp4"}",
            url = url,
            fileName = fileName
        )
    }
}