package com.example.sencsu.data.repository

import android.util.Log
import com.example.sencsu.data.remote.ApiService
import com.example.sencsu.data.remote.dto.ApiResponse
import com.example.sencsu.data.remote.dto.PaiementDto
import retrofit2.HttpException
import javax.inject.Inject

class PaiementRepository @Inject constructor(
    private val apiService: ApiService
) {


    suspend fun getPaiementsByAdherentId(adherentId: Long): List<PaiementDto> {
        return apiService.getPaiementsByAdherentId(adherentId)
    }


    suspend fun addPaiement(paiement: PaiementDto): Result<Unit> {
        return try {
            apiService.addPaiement(paiement) // Retrofit call
            Result.success(Unit)
        } catch (e: HttpException) {
            val errorMsg = e.response()?.errorBody()?.string()?.let { parseErrorMessage(it) }
                ?: e.message()
            Result.failure(Exception(errorMsg))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(json: String): String {
        // Suppose que ton backend renvoie {"message": "..."}
        return try {
            val jsonObj = org.json.JSONObject(json)
            jsonObj.getString("message")
        } catch (e: Exception) {
            "Erreur serveur"
        }
    }

}
