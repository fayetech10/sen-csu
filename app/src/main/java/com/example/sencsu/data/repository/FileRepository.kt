package com.example.sencsu.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.sencsu.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject

class FileRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun uploadImage(
        context: Context,
        uri: Uri
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Impossible d’ouvrir l’URI: $uri"))

            val bytes = inputStream.readBytes()
            inputStream.close()

            val mediaType = contentResolver.getType(uri)
                ?.toMediaTypeOrNull()
                ?: "image/jpeg".toMediaType()

            val requestBody = bytes.toRequestBody(mediaType)

            val part = MultipartBody.Part.createFormData(
                name = "file",
                filename = "image_${System.currentTimeMillis()}.jpg",
                body = requestBody
            )

            // Appel à l'API
            val response = apiService.uploadFile(part)

            // Gestion améliorée de la réponse
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.url)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Réponse d'erreur vide"
                Result.failure(Exception("Échec de l'upload: ${response.code()} - $errorBody"))
            }

        } catch (e: IOException) {
            Log.e("FileRepository", "Erreur réseau lors de l'upload", e)
            Result.failure(Exception("Erreur réseau. Vérifiez votre connexion."))
        } catch (e: Exception) {
            Log.e("FileRepository", "Erreur inattendue lors de l'upload", e)
            Result.failure(e)
        } as Result<String>
    }
}