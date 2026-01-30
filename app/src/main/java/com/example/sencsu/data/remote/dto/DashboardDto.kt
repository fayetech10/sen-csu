package com.example.sencsu.data.remote.dto

import androidx.compose.ui.graphics.Color
//import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.SerializedName
import java.time.LocalDate

// DTO pour la réponse complète
data class ApiResponse<T>(
    val success: Boolean,
    val data: T
)

data class CotisationDto(
    val id: Long? = null,
    val dateDebut: String? = "",
    val dateFin: String? = "",
    val dateSoumission: String? = "",
    val adherentId: Long? = null
)

data class DashboardResponseDto(
    val message: String = "",
    val success: Boolean = false,
    val data: List<AdherentDto> = emptyList()
)


data class CreateAdherentResponse(
    val success: Boolean,
    val message: String,
    val data: AdherentIdResponse
)

data class AdherentIdResponse(
    @SerializedName("adherent_id")
    val adherentId: Long
)

// DTO pour un adhérent
// CORRECTION : Passage en nullable (?) pour éviter le crash NullPointerException

data class AdherentDto(
    val id: Long? = null,

    @SerializedName("prenoms")
    val prenoms: String? = "", // Peut être null parfois

    val nom: String? = "",
    val flagUrl: String = "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fd/Flag_of_Senegal.svg/1200px-Flag_of_Senegal.svg.png",

    val logoUrl: String = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7e/Coat_of_arms_of_Senegal.svg/1024px-Coat_of_arms_of_Senegal.svg.png",

    val adresse: String? = "",
    val qrCodeUrl: String? = " \"https://upload.wikimedia.org/wikipedia/commons/thumb/f/fd/Flag_of_Senegal.svg/1200px-Flag_of_Senegal.svg.png\"",

    val lieuNaissance: String? = "",

    val statut: String? = "ACTIVE",

    val createdAt: String? = "",

    val sexe: String? = "M",
    val numeroCarte: String? = "",

    val dateNaissance: String? = "",
    val lieuDeNaissance: String? = "",
    val typeBenef: String? = "",

    // C'était la cause principale du crash car le JSON renvoie null
    @SerializedName("situationMatrimoniale")
    val situationM: String? = null,

    val whatsapp: String? = "",
    val codeBarres: String = "01443720",

    val secteurActivite: String? = null, // Souvent null ou vide

    val typePiece: String? = "CNI",

    val numeroPiece: String? = "",

    val numeroCNi: String? = "",

    val departement: String? = "",

    val commune: String? = "",

    val region: String? = "Thiès",

    val photo: String? = null,

    // Cause du crash : JSON renvoie null
    val typeAdhesion: String? = null,

    val montantTotal: Double? = 0.0,

    // Cause du crash : JSON renvoie null
    val regime: String? ="Contributif",

    val photoRecto: String? = null,
    val matricule: String? = "ZUYYYB",
    val codeBar: String? = "01443720",

    val photoVerso: String? = null,
    val actif: Boolean? = true,


    val clientUUID: String? = "",

    val personnesCharge: List<PersonneChargeDto> = emptyList(),

    val agent: AgentDto? = null
)


// DTO pour une personne à charge
data class PersonneChargeDto(
    val id: Long? = null,
    val prenoms: String? = "",
    val nom: String? = "",
    val dateNaissance: String? = "",
    val sexe: String? = "M",
    val lieuNaissance: String? = "",
    val adresse: String? = "",
    val whatsapp: String? = "",
    val lienParent: String? = "",

    @SerializedName("situationM")
    val situationM: String? = null, // Peut être null

    val numeroCNi: String? = null, // Important : null dans ton JSON pour l'enfant

    val typePiece: String? = "CNI",
    val numeroExtrait: String? = "",
    var photo: String? ="",
    var photoRecto: String? = "",
    var photoVerso: String? = ""
)



// DTO pour l'agent
data class AgentDto(
    val id: Long? = null,
    val actif: Boolean? = true,
    val name: String? = "",

    @SerializedName("prenom")
    val prenom: String? = "",

    val email: String? = "",

    val role: String? = "",

    val telephone: String? = ""
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