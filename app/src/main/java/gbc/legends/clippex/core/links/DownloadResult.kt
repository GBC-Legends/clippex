package gbc.legends.clippex.core.links

import android.net.Uri

open class DownloadResult

data class Success(val uri: Uri) : DownloadResult()

data class Failure(val errorMessage: String, val exception: Exception? = null) : DownloadResult()