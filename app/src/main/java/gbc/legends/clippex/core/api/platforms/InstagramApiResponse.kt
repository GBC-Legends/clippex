package gbc.legends.clippex.core.api.platforms

import gbc.legends.clippex.core.api.MediaOption
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InstagramApiResponse(
    override val result: InstagramApiResult
) : ApiResponse

@Serializable
data class InstagramApiResult(
    override val author: String,
    override val title: String,
    override val medias: List<InstagramMediaItem>,

    val duration: Double? = null,
    val error: Boolean = false,
    @SerialName("like_count")
    val likeCount: Int? = null,
    val location: String? = null,
    val thumbnail: String? = null,
    val source: String? = null,
    @SerialName("time_end")
    val timeEnd: Int? = null,
    @SerialName("view_count")
    val viewCount: Int? = null,
    val shortcode: String? = null,
    val url: String? = null,
    val type: String? = null,
    @SerialName("music_attribution_info")
    val musicInfo: InstagramMusicInfo? = null,
    val owner: InstagramOwnerInfo? = null
) : ApiResult

@Serializable
data class InstagramMediaItem(
    override val url: String,

    val id: String? = null,
    val duration: Double? = null,
    @SerialName("is_audio")
    val isAudio: Boolean? = null,
    val extension: String? = null,
    val quality: String? = null,
    val resolution: String? = null,
    val type: String? = null,
    val codec: String? = null,
    val bandwidth: Long? = null,
    @SerialName("mimeType")
    val mimeType: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val frameRate: Double? = null
) : MediaItem {

    override fun toMediaOption(author: String, title: String): MediaOption {
        val safeName = sanitizeFileName("$author - $title")

        val ext = extension ?: if (type == "audio") "m4a" else "mp4"

        val info = when (type) {
            "video" -> "video $ext"
            "audio" -> "audio $ext"
            else -> "$type $ext"
        }

        val label = quality ?: resolution ?: type ?: ext

        return MediaOption(
            label = label,
            info = info,
            url = url,
            fileName = "$safeName.$ext"
        )
    }
}

@Serializable
data class InstagramMusicInfo(
    @SerialName("artist_name") val artistName: String? = null,
    @SerialName("audio_id") val audioId: String? = null,
    @SerialName("should_mute_audio") val shouldMuteAudio: Boolean? = null,
    @SerialName("should_mute_audio_reason") val muteReason: String? = null,
    @SerialName("song_name") val songName: String? = null,
    @SerialName("uses_original_audio") val usesOriginalAudio: Boolean? = null
)

@Serializable
data class OwnerStats(
    val count: Int? = null
)

@Serializable
data class InstagramOwnerInfo(
    val id: String? = null,
    @SerialName("full_name")
    val fullName: String? = null,
    val username: String? = null,
    @SerialName("profile_pic_url")
    val profilePicUrl: String? = null,
    @SerialName("is_private")
    val isPrivate: Boolean? = null,
    @SerialName("is_verified")
    val isVerified: Boolean? = null,
    @SerialName("edge_followed_by")
    val followedBy: OwnerStats? = null,
    @SerialName("edge_owner_to_timeline_media")
    val timelineMedia: OwnerStats? = null
)