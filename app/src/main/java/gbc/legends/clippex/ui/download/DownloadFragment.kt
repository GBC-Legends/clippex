package gbc.legends.clippex.ui.download

import android.os.Build
import android.os.Bundle
import android.util.Log
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
import gbc.legends.clippex.core.links.GenericLinkProcessor
import gbc.legends.clippex.core.links.LinkProcessor
import gbc.legends.clippex.core.links.LinkProcessorFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private fun startDownload() {
        val urlString = fileUrl ?: return
        val context = requireContext()

        val linkProcessor: LinkProcessor = LinkProcessorFactory.getProcessor(urlString);
        Log.d("DownloadFragment", "Link processor: $linkProcessor")


        val progressChannel = Channel<Int>(capacity = Channel.CONFLATED)

        val fileName = linkProcessor.getFilename(renameInput.text.toString(), urlString)

        val mimeType = linkProcessor.getMime(fileName)


        progressBar.progress = 0
        progressText.text = "Starting download..."

        lifecycleScope.launch {
            for (progress in progressChannel) {
                progressBar.progress = progress
                progressText.text = "Downloading... $progress%"
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val resultUri = linkProcessor._subprocessLink(context, fileName, mimeType, progressChannel)
                Log.d("DownloadFragment", "Download result: $resultUri")



                // saving to the database
                val db = AppDatabase.getDatabase(context)
                db.downloadedFileDao().insert(
                    DownloadedFile(
                        fileName = fileName,
                        filePath = resultUri.toString(),
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