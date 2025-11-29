package gbc.legends.clippex.ui.download

import android.content.Context
import android.content.Intent
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import gbc.legends.clippex.MainActivity
import gbc.legends.clippex.R
import gbc.legends.clippex.core.api.Failure
import gbc.legends.clippex.core.api.MediaOption
import gbc.legends.clippex.core.api.Success
import gbc.legends.clippex.core.database.AppDatabase
import gbc.legends.clippex.core.database.DownloadedFile
import gbc.legends.clippex.core.links.InstagramLinkProcessor
import gbc.legends.clippex.core.links.LinkProcessor
import gbc.legends.clippex.core.links.LinkProcessorFactory
import gbc.legends.clippex.core.links.TikTokLinkProcessor
import gbc.legends.clippex.core.links.XLinkProcessor
import gbc.legends.clippex.core.links.YouTubeLinkProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import kotlinx.coroutines.withContext


fun Context.showQualityDialog(
    options: List<MediaOption>,
    onSelected: (MediaOption) -> MediaOption
) {
    val items = options.map { "${it.label}  (${it.info})" }.toTypedArray()

    MaterialAlertDialogBuilder(this)
        .setTitle("Select Quality")
        .setItems(items) { dialog, which ->
            val selected = options[which]
            onSelected(selected)
            dialog.dismiss()
        }
        .setNegativeButton("Cancel", null)
        .show()
}



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


    private fun makeDialog(options: List<MediaOption>) : MediaOption {
        requireContext().showQualityDialog(options) {
                selected ->
            Log.d("Selector", selected.toString())
            return@showQualityDialog selected
        }

        return options.first()
    }

    fun isGeneric(linkProcessor: LinkProcessor): Boolean {
        return !(linkProcessor is YouTubeLinkProcessor || linkProcessor is InstagramLinkProcessor || linkProcessor is TikTokLinkProcessor || linkProcessor is XLinkProcessor)
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
        }

        var fileName: String
        var mimeType: String
        val urlString: String = fileUrl ?: return null
        Log.d("DownloadFragment", "URL: $urlString")

        val linkProcessor = LinkProcessorFactory.getProcessor(urlString)
        Log.d("DownloadFragment", "Link processor: $linkProcessor")


        viewLifecycleOwner.lifecycleScope.launch {
            var fileName: String
            var mimeType: String
            var downloadUrl: String = urlString

            if (isGeneric(linkProcessor)) {

                fileName = linkProcessor.getFilename(renameInput.text.toString(), urlString)
                mimeType = linkProcessor.getMime(fileName)

                val name = urlString.substringAfterLast('/', "downloaded_file.txt")
                val extension = name.substringAfterLast('.', "txt")

                fileNameLabel.text = "File Name: $name"
                fileTypeLabel.text = "File Type: $extension"
                renameInput.setText(name)

            } else {
                Log.d("DownloadFragment", "Requesting to clippex api: $urlString")
                val response = when (val req = linkProcessor.requestToClippexApi(requireContext(), urlString)) {
                    is Failure -> {
                        withContext(Dispatchers.Main) {
                            progressText.text = "API failed - ${req.errorMessage}"
                            Toast.makeText(context, "API error", Toast.LENGTH_LONG).show()
                        }

                        delay(1000)

                        requireActivity().finish()
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                        return@launch
                    }
                    is Success -> req.response
                }
                Log.d("DownloadFragment", "Response: $response")


                val options = response.result.medias.map {
                    it.toMediaOption(response.result.author, response.result.title)
                }

                val media = if (options.size == 1) {
                    options[0]
                } else {
                    makeDialog(options)
                }

                fileName = media.fileName
                mimeType = linkProcessor.getMime(fileName)
                downloadUrl = media.url

                val extension = fileName.substringAfterLast('.', "txt")

                fileNameLabel.text = "File Name: $fileName"
                fileTypeLabel.text = "File Type: $extension"
                renameInput.setText(fileName)
            }

            btnDownload.setOnClickListener {
                startDownload(downloadUrl, renameInput.text.toString(), mimeType)
            }
        }

        return view
    }


    private fun startDownload(urlString: String, fileName: String, mimeType: String) {
        val context = requireContext()

        val linkProcessor: LinkProcessor = LinkProcessorFactory.getProcessor(urlString);
        Log.d("DownloadFragment", "Link processor: $linkProcessor")

        val progressChannel = Channel<Int>(capacity = Channel.CONFLATED)


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
                val resultUri = linkProcessor.processLink(context, urlString, fileName, mimeType, progressChannel)
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