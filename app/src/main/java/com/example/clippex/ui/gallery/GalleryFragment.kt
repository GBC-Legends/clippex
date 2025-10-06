package com.example.clippex.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.example.clippex.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource


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
    val sampleFiles = generateFiles()


    LazyColumn (
        modifier = Modifier
            .fillMaxSize().padding(top=16.dp,bottom=54.dp).windowInsetsPadding(WindowInsets.safeDrawing) ,
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


        items(sampleFiles.size) { ind ->
            FileRow(sampleFiles[ind])
        }
    }
}

fun generateFiles(): List<String> {
    val files = listOf(
        "movie1.mp4", "clip1.mkv", "scene1.mov", "video1.webm", "record1.avi",
        "movie2.mp4", "clip2.mkv", "scene2.mov", "video2.webm", "record2.avi",

        "track1.mp3", "track2.wav", "track3.ogg", "music1.flac", "music2.aac",
        "song1.m4a", "melody1.mid", "recording1.wma", "audio1.mp3", "podcast1.mp3",

        "photo1.jpg", "photo2.jpeg", "photo3.png", "image1.webp", "wallpaper1.bmp",
        "icon1.svg", "graphic1.tiff", "picture1.heic", "avatar1.gif", "background1.png",

        "report1.pdf", "contract1.doc", "resume1.docx", "summary1.txt", "notes1.md",
        "invoice1.csv", "outline1.rtf", "manual1.odt", "changelog1.log", "book1.pdf",

        "finance1.xls", "finance2.xlsx", "budget1.ods", "report2.numbers", "plan1.xls",
        "balance1.xlsx", "sales1.ods", "table1.csv", "data1.tsv", "list1.xlsx",

        "presentation1.ppt", "presentation2asdasdas.pptx", "deck1.odp", "slides1.key", "talk1.pptx",
        "seminar1.ppt", "lesson1.pptx", "briefing1.odp", "training1.key", "show1.pptx",

        "archive1.zip", "backup1.rar", "package1.7z", "bundle1.tar", "files1.gz",
        "compressed1.bz2", "disk1.iso", "content1.zip", "data1.rar", "update1.7z",

        "script1.py", "main1.kt", "app1.apk", "program1.exe", "driver1.dll",
        "settings1.yaml", "config1.ini", "build1.gradle", "index1.html", "style1.css"
    )

    return files.shuffled()
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


@Composable
fun FileRow(
    fileName: String,
) {
    val icon = getIconForFileName(fileName)
    val displayableFileName = getDisplayableFileName(fileName, 15)
    val context = LocalContext.current

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
                text = displayableFileName,
                fontSize = 16.sp
            )
        }

        Row {
            OutlinedButton(
                onClick = { /* TODO: saving this file to the file explorer */
                    Toast.makeText(context, "Fake saving $displayableFileName", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.height(36.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colorResource(R.color.blue),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Save")
            }

            Spacer(Modifier.width(8.dp))

            OutlinedButton(
                onClick = { /* TODO: deleting this file from database */
                    Toast.makeText(context, "Fake deleting $displayableFileName", Toast.LENGTH_SHORT).show()
                },
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