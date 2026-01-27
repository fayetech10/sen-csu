package com.example.sencsu.data.repository

import com.example.sencsu.data.remote.ApiService
import com.example.sencsu.data.remote.dto.PaiementDto
import javax.inject.Inject

class PaiementRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun addPaiement(paiement: PaiementDto): Result<Unit> {

        return try {

            val response = apiService.addPaiement(paiement)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception("Erreur serveur : ${response.code()}")
                )
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
