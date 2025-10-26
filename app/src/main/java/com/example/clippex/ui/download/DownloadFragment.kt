package com.example.clippex.ui.download

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.clippex.R
import com.example.clippex.core.database.AppDatabase
import com.example.clippex.core.database.DownloadedFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

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

        // ---------------- TEST ----------------
        if (fileUrl == null) {
            fileNameLabel.text = "Invalid link"
            btnDownload.isEnabled = false
        } else {
            val name = fileUrl!!.substringAfterLast('/', "downloaded_file.txt")
            val extension = name.substringAfterLast('.', "txt")
            // show file info
            fileNameLabel.text = "File Name: $name"
            fileTypeLabel.text = "File Type: $extension"
        }

        btnDownload.setOnClickListener {
            startDownload()
        }
        return view
    }

    private fun startDownload() {
        val url = fileUrl ?: return
        val context = requireContext()

        val fileName = if (renameInput.text.isNotBlank()) {
            renameInput.text.toString()
        } else {
            url.substringAfterLast('/', "downloaded_file.txt")
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

        lifecycleScope.launch {
            progressText.text = "Downloading..."

            // progress bar test
            for (i in 1..100 step 10) {
                progressBar.progress = i
                progressText.text = "Downloading... $i%"
                kotlinx.coroutines.delay(50)
            }

            // saving files to the device
            try {
                // creating /Download/clippex/ directory
                val clippexDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "clippex"
                )
                if (!clippexDir.exists()) {
                    clippexDir.mkdirs()
                }

                // test file
                val file = File(clippexDir, fileName)
                FileOutputStream(file).use { out ->
                    out.write("This is a test file saved from Clippex.\nURL: $url".toByteArray())
                }

                // save to the database
                val db = AppDatabase.getDatabase(context)
                db.downloadedFileDao().insert(
                    DownloadedFile(
                        fileName = fileName,
                        filePath = file.absolutePath,
                        mimeType = mimeType
                    )
                )

                withContext(Dispatchers.Main) {
                    progressBar.progress = 100
                    progressText.text = "Download Complete"
                    Toast.makeText(context, "Saved to: ${file.absolutePath}", Toast.LENGTH_LONG)
                        .show()
                    btnDownload.isEnabled = true
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressText.text = "Download Failed"
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    btnDownload.isEnabled = true
                }
            }
        }
    }
}