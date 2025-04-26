package com.kyobi.createreel

import android.content.ContentValues
import android.content.Intent
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import android.content.Context
import java.io.File

object VideoUtils {
    fun saveVideoToGallery(context: Context, videoFile: File): String? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, "lookbook_${System.currentTimeMillis()}.mp4")
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
        }

        val uri = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
        return uri?.let {
            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                videoFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Toast.makeText(context, "Video saved to gallery", Toast.LENGTH_SHORT).show()
            videoFile.absolutePath
        }
    }

    fun shareVideo(context: Context, videoPath: String) {
        val videoFile = File(videoPath)
        val videoUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", videoFile)
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, videoUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Lookbook Video"))
    }
}