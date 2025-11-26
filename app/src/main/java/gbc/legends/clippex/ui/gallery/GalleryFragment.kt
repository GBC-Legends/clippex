package gbc.legends.clippex.ui.gallery

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import gbc.legends.clippex.core.database.AppDatabase
import gbc.legends.clippex.core.database.DownloadedFile
import gbc.legends.clippex.R
import kotlinx.coroutines.launch

class GalleryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                GalleryScreen()
            }
        }
    }
}

@Composable
fun GalleryScreen() {
    val mainText = stringResource(R.string.gallery_main_text)
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    var files by remember { mutableStateOf<List<DownloadedFile>>(emptyList()) }
    val scope = rememberCoroutineScope()

    // load files from the database
    LaunchedEffect(Unit) {
        files = db.downloadedFileDao().getAll()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize().padding(top = 16.dp, bottom = 54.dp).windowInsetsPadding(WindowInsets.safeDrawing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = mainText,
                color = Color.Black,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp, top = 32.dp)
            )
        }

        // no files saved
        if (files.isEmpty()) {
            item {
                Text(
                    text = "No files saved yet.",
                    color = Color.Gray,
                    fontSize = 18.sp
                )
            }
        } else { // display the saved files
            items(files.size) { ind ->
                val file = files[ind]
                FileRow(
                    fileName = file.fileName,
                    onDelete = {
                        scope.launch {
                            db.downloadedFileDao().delete(file)
                            files = db.downloadedFileDao().getAll()
                            Toast.makeText(context, "Deleted ${file.fileName}", Toast.LENGTH_SHORT)
                                .show()
                        }
                    },
                    onSave = { // opens the file manager - give permission to the "clippex" folder inside "Download"
                        try {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Can't open file manager.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun FileRow(
    fileName: String,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    val icon = getIconForFileName(fileName)
    val displayableFileName = getDisplayableFileName(fileName, 15)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = displayableFileName, fontSize = 16.sp
            )
        }

        Row {
            // opening the folder
            OutlinedButton(
                onClick = onSave,
                modifier = Modifier.height(36.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colorResource(R.color.blue),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Folder, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Open")
            }

            Spacer(Modifier.width(8.dp))

            // deleting
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.height(36.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colorResource(R.color.blue),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Delete")
            }
        }
    }
}

fun getIconForFileName(fileName: String): ImageVector {
    val ext = fileName.substringAfterLast('.', "").lowercase()

    return when (ext) {

        "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "mpeg", "mpg", "3gp" ->
            Icons.Default.VideoFile

        "mp3", "wav", "ogg", "flac", "aac", "m4a", "mid", "midi", "wma" ->
            Icons.Default.AudioFile

        "jpg", "jpeg", "png", "gif", "bmp", "tiff", "tif", "svg", "webp", "heic", "ico" ->
            Icons.Default.Image

        "pdf" -> Icons.Default.PictureAsPdf
        "doc", "docx", "odt", "rtf" -> Icons.Default.Description
        "txt", "log", "md", "csv" -> Icons.AutoMirrored.Filled.Article

        "xls", "xlsx", "ods", "numbers" -> Icons.Default.TableChart

        "ppt", "pptx", "odp", "key" -> Icons.Default.Slideshow

        "zip", "rar", "7z", "tar", "gz", "bz2", "xz", "iso" -> Icons.Default.FolderZip

        "html", "htm", "xml" -> Icons.Default.Code
        "js", "ts", "jsx", "tsx", "json" -> Icons.Default.DataObject
        "css", "scss", "less" -> Icons.Default.Style
        "py", "rb", "php", "java", "kt", "go", "rs", "c", "cpp", "h", "hpp", "sh", "bat" ->
            Icons.Default.Code

        "exe", "dll", "bin", "dat", "apk", "app", "deb", "rpm", "msi" ->
            Icons.Default.Memory

        "ini", "cfg", "conf", "yaml", "yml", "toml", "env", "properties" ->
            Icons.Default.Settings

        "url", "webloc", "link" -> Icons.Default.Public

        "", "folder" -> Icons.Default.Folder
        else -> Icons.Default.FileOpen
    }
}

fun getDisplayableFileName(fileName: String, safeLength: Int = 15): String {
    return if (fileName.length > safeLength) {
        fileName.substring(0, safeLength-3) + "..."
    } else {
        fileName
    }
}