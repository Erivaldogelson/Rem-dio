package com.erivaldogelson.remedios.media

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

class MedicationImageManager(private val context: Context) {

    fun createCaptureUri(): Uri {
        val file = createImageFile("capture_${UUID.randomUUID()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }

    suspend fun persistUri(sourceUri: Uri): String {
        val destination = createImageFile("med_${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            destination.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return destination.toURI().toString()
    }

    private fun createImageFile(name: String): File {
        val directory = File(context.cacheDir, "images").apply { mkdirs() }
        return File(directory, name)
    }
}

