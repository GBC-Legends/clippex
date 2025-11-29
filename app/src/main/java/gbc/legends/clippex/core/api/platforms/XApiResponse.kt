package gbc.legends.clippex.core.api.platforms

import gbc.legends.clippex.core.api.MediaOption
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class XApiResponse(
    override val result: XApiResult
) : ApiResponse


@Serializable
data class XApiResult(
    override val author: String,
    val duration: Double? = null,
    val error: Boolean,
    override val medias: List<XMediaItem>,
    val source: String,
    val thumbnail: String? = null,
    @SerialName("time_end") val timeEnd: Int,
    override val title: String,
    val type: String,
    val url: String
) : ApiResult


@Serializable
data class XMediaItem(
    val duration: Double? = null,
    val extension: String? = null,
    val format: String? = null,
    val height: Int? = null,
    val quality: String? = null,
    val resolution: String? = null,
    val thumbnail: String? = null,
    val type: String,
    override val url: String,
    val width: Int? = null
) : MediaItem {
    override fun toMediaOption(author: String, title: String): MediaOption {
        val fileName = sanitizeFileName("$author - $title") + ".$extension"
        return MediaOption(label=this.quality ?: "MAX", info="$type $extension", url=this.url, fileName=fileName)
    }
}
