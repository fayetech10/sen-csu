package com.example.sencsu.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO pour la requête de connexion.
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

/**
 * DTO pour la réponse de connexion.
 * Contient le token et les informations de l'utilisateur.
 */
data class LoginResponse(
    @SerializedName("accessToken")
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Long,

    @SerializedName("user")
    val user: AgentDto

)
