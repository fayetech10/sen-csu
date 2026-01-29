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

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("/api/adherents/all")
    suspend fun getDashboardData(): DashboardResponseDto
    @POST("/api/adherents/create")
    suspend fun createAdherent(
        @Query("agentId") agentId: Long,
        @Body adherent: AdherentDto
    ): CreateAdherentResponse

    @GET("/api/paiements/adherent/{id}")
    suspend fun getPaiementsByAdherentId(@Path("id") adherentId: Long): List<PaiementDto>

    @GET("/api/cotisation/adherent/{id}")
    suspend fun getCotisationByAdherentId(
        @Path("id")
        adherentId: Long
    ): List<CotisationDto>


    @Multipart
    @POST("/api/files/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>

    @POST("/api/paiements/add")
    suspend fun addPaiement(
        @Body paiement: PaiementDto
    ): Response<Unit>

    @GET("/api/adherents/{id}")
    suspend fun getAdherentById(
        @Path("id") id: Long
    ): ApiResponse<AdherentDto>

    @GET("/api/adherents/by-agent/{agentId}")
    suspend fun getAdherentsByAgentId(
        @Path("agentId") agentId:  Long
    ): ApiResponse<List<AdherentDto>>

    @DELETE("/api/adherents/{id}")
    suspend fun deleteAdherent(
        @Path("id") id: Long
    ) : Response<Unit>

    @DELETE("/api/personnes-charge/{id}")
    suspend fun deletePersonneCharge(
        @Path("id") id: Long
    )

    @POST("/api/adherents/{adherentId}/personnes-charge")
    suspend fun addPersonneCharge(
        @Path("adherentId") adherentId: Long,
        @Body personne: PersonneChargeDto
    )

}
