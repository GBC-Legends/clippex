package gbc.legends.clippex.core.api.platforms

import gbc.legends.clippex.core.api.MediaOption
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YouTubeApiResponse(
    override val result: YouTubeApiResult
) : ApiResponse

@Serializable
data class YouTubeApiResult(
    override val author: String,
    val duration: Double? = null,
    val error: Boolean,
    override val medias: List<YouTubeMediaItem>,
    override val title: String
) : ApiResult

@Serializable
data class YouTubeMediaItem(
    val audioQuality: String? = null,
    val audioSampleRate: String? = null,
    val bitrate: Int? = null,
    val duration: Int? = null,

    @SerialName("ext")
    val ext: String? = null,

    val extension: String? = null,
    val formatId: Int? = null,
    val fps: Int? = null,
    val height: Int? = null,
    val is_audio: Boolean? = null,
    val label: String? = null,
    val mimeType: String? = null,
    val quality: String? = null,
    override val url: String,
    val type: String,
    val width: Int? = null
) : MediaItem {

    override fun toMediaOption(author: String, title: String): MediaOption {
        val safeExt = extension ?: ext ?: "mp4"
        val fileName = sanitizeFileName("$author - $title") + ".$safeExt"
        val labelName = quality ?: label ?: type

        return MediaOption(
            label = labelName,
            info = "$type $safeExt ${height ?: ""}p",
            url = this.url,
            fileName = fileName
        )
    }
}

