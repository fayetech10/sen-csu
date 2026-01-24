package com.example.sencsu.data.repository

import android.util.Log
import com.example.sencsu.data.remote.ApiService
import com.example.sencsu.data.remote.dto.AdherentDto
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




    suspend fun ajouterAdherent(agentId: Long, adherent: AdherentDto): Result<AdherentDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createAdherent(agentId, adherent) // agentId à adapter
                if (response.isSuccessful) {
                    val adherentCree = response.body()
                    if (adherentCree != null) {
                        Result.success(adherentCree)
                    } else {
                        Result.failure(Exception("Réponse vide du serveur"))
                    }
                } else {
                    Log.e("AddAdherentVM", "Erreur submit")
                    val errorMessage = when (response.code()) {
                        400 -> "Requête invalide"
                        401 -> "Non autorisé"
                        403 -> "Accès refusé"
                        404 -> "Ressource non trouvée"
                        500 -> "Erreur serveur interne"
                        else -> "Erreur ${response.code()}: ${response.message()}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: HttpException) {
                Result.failure(Exception("Erreur HTTP: ${e.message}"))
            } catch (e: IOException) {
                Result.failure(Exception("Erreur de connexion: ${e.message}"))
            } catch (e: Exception) {
                Result.failure(Exception("Erreur inattendue: ${e.message}"))
            } as Result<AdherentDto>
        }
    }

}
