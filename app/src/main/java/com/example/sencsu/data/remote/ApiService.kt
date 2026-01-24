package com.example.sencsu.data.remote

import com.example.sencsu.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("adherents/all")
    suspend fun getDashboardData(): DashboardResponseDto
    @POST("adherents/create")
    suspend fun createAdherent(
        @Query("agentId") agentId: Long,
        @Body adherent: AdherentDto
    ): Response<AdherentDto>

    @Multipart
    @POST("files/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>

    @GET("adherents/{id}")
    suspend fun getAdherentById(
        @Path("id") id: Long
    ): ApiResponse<AdherentDto>

    @GET("adherents/by-agent/{agentId}")
    suspend fun getAdherentsByAgentId(
        @Path("agentId") agentId:  Long
    ): ApiResponse<List<AdherentDto>>

    @DELETE("adherents/{id}")
    suspend fun deleteAdherent(
        @Path("id") id: Long
    ) : Response<Unit>

    @DELETE("personnes-charge/{id}")
    suspend fun deletePersonneCharge(
        @Path("id") id: Long
    )

    @POST("adherents/{adherentId}/personnes-charge")
    suspend fun addPersonneCharge(
        @Path("adherentId") adherentId: Long,
        @Body personne: PersonneChargeDto
    )

}
