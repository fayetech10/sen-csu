package com.example.sencsu.data.repository

import android.util.Log
import com.example.sencsu.data.remote.ApiService
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.AdherentIdResponse
import com.example.sencsu.data.remote.dto.DashboardResponseDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class DashboardRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getDashboardData(): DashboardResponseDto {
        return apiService.getDashboardData()
    }


    suspend fun getAdherentsByAgentId(agentId: Long): Result<List<AdherentDto>> {
        return try {
            val response = apiService.getAdherentsByAgentId(agentId)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }




    suspend fun ajouterAdherent(agentId: Long, adherent: AdherentDto): Result<AdherentIdResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createAdherent(agentId, adherent)
                if (response.success) {
                    val adherentCree = response.data // Ceci est un AdherentIdResponse
                    Result.success(adherentCree)
                } else {
                    Log.e("DashboardRepository", "Erreur lors de la création de l'adhérent")
                    Result.failure(Exception(response.message ?: "Erreur inconnue"))
                }
            } catch (e: HttpException) {
                Log.e("DashboardRepository", "Erreur HTTP: ${e.message}")
                Result.failure(Exception("Erreur HTTP: ${e.code()} - ${e.message}"))
            } catch (e: IOException) {
                Log.e("DashboardRepository", "Erreur de connexion: ${e.message}")
                Result.failure(Exception("Erreur de connexion: ${e.message}"))
            } catch (e: Exception) {
                Log.e("DashboardRepository", "Erreur inattendue: ${e.message}")
                Result.failure(Exception("Erreur inattendue: ${e.message}"))
            }
        }
    }
}
