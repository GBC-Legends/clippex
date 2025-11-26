package gbc.legends.clippex.core.links

import java.io.File

open class DownloadResult

data class Success(val file: File, val mimeType: String) : DownloadResult()

data class Failure(val errorMessage: String, val exception: Exception? = null) : DownloadResult()