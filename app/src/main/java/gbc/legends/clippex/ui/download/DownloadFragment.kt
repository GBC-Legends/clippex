package gbc.legends.clippex.ui.download

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import gbc.legends.clippex.R
import gbc.legends.clippex.core.database.AppDatabase
import gbc.legends.clippex.core.database.DownloadedFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class DownloadFragment : Fragment() {
    private lateinit var fileNameLabel: TextView
    private lateinit var fileTypeLabel: TextView
    private lateinit var renameInput: EditText
    private lateinit var btnDownload: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private var fileUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // get the fileUrl from HomeFragment
        fileUrl = arguments?.getString("fileUrl")
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_download, container, false)

        fileNameLabel = view.findViewById(R.id.fileNameLabel)
        fileTypeLabel = view.findViewById(R.id.fileTypeLabel)
        renameInput = view.findViewById(R.id.renameInput)
        btnDownload = view.findViewById(R.id.btnDownload)
        progressBar = view.findViewById(R.id.progressBar)
        progressText = view.findViewById(R.id.progressText)

        if (fileUrl == null) {
            fileNameLabel.text = "Invalid link"
            btnDownload.isEnabled = false
        } else {
            val name = fileUrl!!.substringAfterLast('/', "downloaded_file.txt")
            val extension = name.substringAfterLast('.', "txt")
            // show file info
            fileNameLabel.text = "File Name: $name"
            fileTypeLabel.text = "File Type: $extension"
            renameInput.setText(name)
        }

        btnDownload.setOnClickListener {
            startDownload()
        }
        return view
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun startDownload() {
        val urlString = fileUrl ?: return
        val context = requireContext()

        val fileName = if (renameInput.text.isNotBlank()) {
            renameInput.text.toString()
        } else {
            urlString.substringAfterLast('/', "downloaded_file.txt")
        }

        val extension = fileName.substringAfterLast('.', "txt")
        // file types for the database
        val mimeType = when (extension.lowercase()) {
            "mp4", "mkv", "avi" -> "video/*"
            "mp3", "wav" -> "audio/*"
            "jpg", "png", "jpeg", "gif" -> "image/*"
            "pdf" -> "application/pdf"
            "txt", "csv", "md" -> "text/*"
            else -> "application/octet-stream"
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw IOException("Error ${connection.responseCode}")
                }

                val totalSize = connection.contentLength
                val input = BufferedInputStream(connection.inputStream)
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Clippex")
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
                            withContext(Dispatchers.Main) {
                                progressBar.progress = progress
                                progressText.text = "Downloading... $progress%"
                            }
                        }
                    }
                }

                input.close()
                connection.disconnect()

                // saving to the database
                val db = AppDatabase.getDatabase(context)
                db.downloadedFileDao().insert(
                    DownloadedFile(
                        fileName = fileName,
                        filePath = uri.toString(),
                        mimeType = mimeType
                    )
                )

                withContext(Dispatchers.Main) {
                    progressBar.progress = 100
                    progressText.text = "Download Complete"
                    Toast.makeText(context, "File saved to Downloads/Clippex", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressText.text = "Download Failed"
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}