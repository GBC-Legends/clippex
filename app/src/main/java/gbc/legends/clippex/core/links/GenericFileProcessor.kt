package gbc.legends.clippex.core.links

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import kotlinx.coroutines.channels.Channel
import java.io.BufferedInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


open class GenericLinkProcessor(open val url: String) : LinkProcessor {

    override fun canProcess(url: String): Boolean = true

    override fun getFilename(renameInput: String, urlString: String): String {
        return renameInput.ifBlank { urlString.substringAfterLast('/', "downloaded_file.txt") }
    }

    override fun getMime(filename: String): String {
        val ext = filename.substringAfterLast('.', "").lowercase()

        return when (ext) {
            // ----- VIDEO -----
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "avi" -> "video/x-msvideo"
            "mov" -> "video/quicktime"
            "wmv" -> "video/x-ms-wmv"
            "flv" -> "video/x-flv"
            "webm" -> "video/webm"
            "mpeg", "mpg" -> "video/mpeg"
            "3gp" -> "video/3gpp"

            // ----- AUDIO -----
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            "flac" -> "audio/flac"
            "aac" -> "audio/aac"
            "m4a" -> "audio/mp4"
            "mid", "midi" -> "audio/midi"
            "wma" -> "audio/x-ms-wma"

            // ----- IMAGES -----
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "bmp" -> "image/bmp"
            "tiff", "tif" -> "image/tiff"
            "svg" -> "image/svg+xml"
            "webp" -> "image/webp"
            "heic" -> "image/heic"
            "ico" -> "image/x-icon"

            // ----- DOCUMENTS -----
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "odt" -> "application/vnd.oasis.opendocument.text"
            "rtf" -> "application/rtf"

            // ----- TEXT -----
            "txt" -> "text/plain"
            "log" -> "text/plain"
            "md" -> "text/markdown"
            "csv" -> "text/csv"

            // ----- SPREADSHEETS -----
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "ods" -> "application/vnd.oasis.opendocument.spreadsheet"
            "numbers" -> "application/x-iwork-numbers-sffnumbers"

            // ----- PRESENTATIONS -----
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "odp" -> "application/vnd.oasis.opendocument.presentation"
            "key" -> "application/x-iwork-keynote-sffkey"

            // ----- ARCHIVES -----
            "zip" -> "application/zip"
            "rar" -> "application/vnd.rar"
            "7z" -> "application/x-7z-compressed"
            "tar" -> "application/x-tar"
            "gz" -> "application/gzip"
            "bz2" -> "application/x-bzip2"
            "xz" -> "application/x-xz"
            "iso" -> "application/x-iso9660-image"

            // ----- WEB -----
            "html", "htm" -> "text/html"
            "xml" -> "application/xml"
            "js" -> "application/javascript"
            "ts" -> "application/typescript"
            "jsx", "tsx" -> "text/javascript"
            "json" -> "application/json"
            "css", "scss", "less" -> "text/css"

            // ----- CODE -----
            "py" -> "text/x-python"
            "rb" -> "text/x-ruby"
            "php" -> "application/x-httpd-php"
            "java" -> "text/x-java"
            "kt" -> "text/x-kotlin"
            "go" -> "text/x-go"
            "rs" -> "text/x-rust"
            "c" -> "text/x-c"
            "cpp" -> "text/x-c++"
            "h", "hpp" -> "text/x-c-header"
            "sh" -> "application/x-sh"
            "bat" -> "application/x-bat"

            // ----- EXECUTABLE -----
            "exe" -> "application/vnd.microsoft.portable-executable"
            "dll" -> "application/vnd.microsoft.portable-executable"
            "bin" -> "application/octet-stream"
            "dat" -> "application/octet-stream"
            "apk" -> "application/vnd.android.package-archive"
            "app" -> "application/x-mach-binary"
            "deb" -> "application/vnd.debian.binary-package"
            "rpm" -> "application/x-rpm"
            "msi" -> "application/x-msi"

            // ----- CONFIG FILES -----
            "ini" -> "text/plain"
            "cfg", "conf" -> "text/plain"
            "yaml", "yml" -> "application/x-yaml"
            "toml" -> "application/toml"
            "env" -> "text/plain"
            "properties" -> "text/x-java-properties"

            // ----- SHORTCUTS -----
            "url", "webloc", "link" -> "text/plain"

            // ----- NO EXTENSION -----
            "" -> "application/octet-stream"

            // ----- FALLBACK -----
            else -> "application/octet-stream"
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun _subprocessLink(context: Context, filename: String, mime: String, channel: Channel<Int>): DownloadResult {
        try {
            val url = URL(this.url)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("Error ${connection.responseCode}")
            }

            val totalSize = connection.contentLength
            val input = BufferedInputStream(connection.inputStream)
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, filename)
                put(MediaStore.Downloads.MIME_TYPE, mime)
                put(
                    MediaStore.Downloads.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + "/Clippex"
                )
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException("Failed to create file")

            resolver.openOutputStream(uri)?.use { output ->
                val buffer = ByteArray(4096)
                var bytesRead: Int
                var downloaded = 0L

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    downloaded += bytesRead
                    if (totalSize > 0) {
                        val progress = (downloaded * 100 / totalSize).toInt()
                        channel.send(progress)
                    }
                }
            }

            input.close()
            connection.disconnect()

            return Success(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            return Failure("Download processing failed: ${e.message}", e)
        }
    }
}