
package com.example.sencsu.domain.viewmodel

import android.net.Uri
import com.example.sencsu.data.remote.dto.FormConstants
import com.example.sencsu.data.remote.dto.PersonneChargeDto


data class AddAdherentUiState(
    // Champs Adhérent
    val prenoms: String = "",
    val nom: String = "",
    val adresse: String = "",
    val lieuNaissance: String = "",
    val sexe: String = "M",
    val dateNaissance: String = "",
    val situationMatrimoniale: String = FormConstants.SITUATIONS.firstOrNull() ?: "",
    val whatsapp: String = "",
    val secteurActivite: String = "",
    val typePiece: String = FormConstants.TYPES_PIECE.firstOrNull() ?: "CNI",
    val numeroCNI: String = "",
    val numeroExtrait: String = "",
    val departement: String = FormConstants.DEPARTEMENTS.firstOrNull() ?: "",
    val commune: String = "",
    val totalCost: Int = 4500,

    // Photos Adhérent
    val photoUri: Uri? = null,
    val rectoUri: Uri? = null,
    val versoUri: Uri? = null,
    val uploadProgress: Float = 0f,
    // Liste des dépendants validés
    val dependants: List<PersonneChargeDto> = emptyList(),

    // État du Modal (Ajout/Edition Dépendant)
    val isModalVisible: Boolean = false,
    val editingIndex: Int? = null, // null = création, Int = modification
    val currentDependant: PersonneChargeDto = PersonneChargeDto(), // Assurez-vous d'avoir un constructeur par défaut ou vide

    // Photos temporaires pour le modal
    var photo: String? = "",
    var photoRecto: String? = "",
    var photoVerso: String? = "",


    val isLoading: Boolean = false
) {
    // Propriété calculée : Est-ce que le formulaire principal est valide ?
    val isFormValid: Boolean
        get() {
            val pieceValide = if (typePiece == "CNI") numeroCNI.isNotBlank() else numeroExtrait.isNotBlank()
            return prenoms.isNotBlank() &&
                    nom.isNotBlank() &&
                    adresse.isNotBlank() &&
                    lieuNaissance.isNotBlank() &&
                    commune.isNotBlank() &&
                    secteurActivite.isNotBlank() &&
                    dateNaissance.isNotBlank() &&
                    pieceValide &&
                    photoUri != null
        }

    // Propriété calculée : Coût totalval totalCost: Int = 4500



}