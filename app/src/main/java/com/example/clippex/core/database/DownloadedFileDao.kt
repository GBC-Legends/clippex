package com.example.clippex.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DownloadedFileDao {
    @Query("SELECT * FROM downloaded_files ORDER BY id DESC")
    suspend fun getAll(): List<DownloadedFile>

    @Insert
    suspend fun insert(file: DownloadedFile)

    @Delete
    suspend fun delete(file: DownloadedFile)
}