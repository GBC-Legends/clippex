package com.example.clippex.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_files")
data class DownloadedFile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fileName: String,
    val filePath: String,
    val mimeType: String
)