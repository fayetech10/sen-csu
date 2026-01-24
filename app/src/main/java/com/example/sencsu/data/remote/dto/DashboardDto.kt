package com.example.sencsu.data.remote.dto

import androidx.compose.ui.graphics.Color
import com.google.gson.annotations.SerializedName

// DTO pour la réponse complète
data class ApiResponse<T>(
    val success: Boolean,
    val data: T
)

data class DashboardResponseDto(
    @SerializedName("message")
    val message: String?, // Peut être null parfois
    val success: Boolean,
    @SerializedName("data")
    val adherents: List<AdherentDto>? = emptyList() // La liste elle-même peut être null
)

// DTO pour un adhérent
// CORRECTION : Passage en nullable (?) pour éviter le crash NullPointerException
data class AdherentDto(
    val id: String? = null,
    val prenoms: String? = "",
    val nom: String? = null,
    val adresse: String? = null,
    val lieuNaissance: String? = null,
    val statut: String? = "ACTIVE",
    val createdAt: String? = null,
    val sexe: String? = "M",
    val dateNaissance: String? = null,
    val situationM: String? = null,
    val whatsapp: String? = null,
    val secteurActivite: String? = null,
    val typePiece: String? = "CNI",
    val numeroPiece: String? = null,
    val numeroCNi: String? = null,
    val departement: String? = null,
    val commune: String? = null,
    val region: String? = "Thiès",
    val agentId: String? = null,
    val photo: String? = null,
    val typeAdhesion: String? = null,
    val montantTotal: Int? = null,
    val regime: String? = "CONTRIBUTIF",
    val photoRecto: String? = null,
    val photoVerso: String? = null,
    val clientUUID: String? = null,
    val personnesCharge: List<PersonneChargeDto> = emptyList(), // Gson gère bien les listes vides si configuré, sinon mettre ?
    val agent: AgentDto? = null
)

// DTO pour une personne à charge
data class PersonneChargeDto(
    val id: String? = null,
    val prenoms: String? = null,
    val nom: String? = null,
    val dateNaissance: String? = null,
    val sexe: String? = "M",
    val lieuNaissance: String? = null,
    val adresse: String? = null,
    val whatsapp: String? = null,
    val lienParent: String? = null,
    val situationM: String? = null,
    val numeroCNi: String? = null,
    val typePiece: String? = "CNI",
    val numeroExtrait: String? = null,
    val photo: String? = null,
    val photoRecto: String? = null,
    val photoVerso: String? = null
)

// DTO pour l'agent
data class AgentDto(
    @SerializedName("id")
    val id: Int? = 0,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("prenoms")
    val prenoms: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("role")
    val role: String? = null,

    @SerializedName("telephone")
    val telephone: String? = null
)

data class UploadResponse(
    @SerializedName("filename") val filename: String?,
    @SerializedName("url") val url: String?
)

object FormConstants {
    val SITUATIONS = listOf("Célibataire", "Marié(e)", "Divorcé(e)", "Veuf(ve)")
    val TYPES_PIECE = listOf("CNI", "Extrait de naissance")
    val DEPARTEMENTS = listOf("Thiès", "Mbour", "Tivaouane")
    val SEXES = listOf("M", "F")
    val LIENS_PARENTE = listOf("Conjoint(e)", "Enfant", "Parent", "Frère/Soeur", "Autre")

    object Colors {
        val primary = Color(0xFF121312)
        val primaryDark = Color(0xFF1B5E20)
        val primaryLight = Color(0xFFE8F5E9)
        val secondary = Color(0xFFF57C00)
        val error = Color(0xFFC62828)
        val textGrey = Color(0xFF78909C)
        val white = Color(0xFFFFFFFF)
        val background = Color(0xFFF7F9FA)
        val inputBorder = Color(0xFFE0E0E0)
        val textDark = Color(0xFF212121)
        val success = Color(0xFF4CAF50)
    }
}