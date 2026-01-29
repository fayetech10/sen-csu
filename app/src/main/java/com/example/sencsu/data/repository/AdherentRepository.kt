package com.example.sencsu.data.repository

import com.example.sencsu.data.remote.ApiService
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.PersonneChargeDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdherentRepository @Inject constructor(
    private val apiService: ApiService
) {




    suspend fun getAdherentById(id: Long): Result<AdherentDto> {
        return try {
            val response = apiService.getAdherentById(id)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun deleteAdherent(id: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                apiService.deleteAdherent(id)
                Result.success(Unit)
            } catch (e: IOException) {
                // Erreur réseau (pas d'internet)
                Result.failure(Exception("Problème de connexion internet"))
            } catch (e: HttpException) {
                // Erreur API (404, 500, etc.)
                Result.failure(Exception("Erreur serveur : ${e.code()}"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    suspend fun addPersonneCharge(adherentId: Long, personne: PersonneChargeDto) {

        apiService.addPersonneCharge(adherentId, personne)
    }

    suspend fun deletePersonneCharge(id: Long) {
        apiService.deletePersonneCharge(id)
    }
}
