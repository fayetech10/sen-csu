package com.example.sencsu.utils

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

object FileUtils {

    fun uriToMultipart(
        context: Context,
        uri: Uri,
        partName: String = "file"
    ): MultipartBody.Part {

        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "image/*"

        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Impossible de lire le fichier")

        val bytes = inputStream.readBytes()

        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())

        return MultipartBody.Part.createFormData(
            partName,
            "image_${System.currentTimeMillis()}.jpg",
            requestBody
        )
    }
}
