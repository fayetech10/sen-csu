package com.example.sencsu.domain.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.FormConstants
import com.example.sencsu.data.remote.dto.PersonneChargeDto
import com.example.sencsu.data.repository.DashboardRepository
import com.example.sencsu.data.repository.FileRepository

import com.example.sencsu.utils.Formatters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

// Définition des événements pour l'UI

// Dans un fichier dédié ou dans le ViewModel correspondant
sealed class AdherentListUiEvent {
    data class ShowSnackbar(val message: String) : AdherentListUiEvent()
    object AdherentDeleted : AdherentListUiEvent()
}

sealed class AddAdherentUiEvent {
    data class ShowSnackbar(val message: String) : AddAdherentUiEvent()
    data class NavigateToPayment(val formData: AdherentDto, val montantTotal: Int) : AddAdherentUiEvent()
}
@HiltViewModel
class AddAdherentViewModel @Inject constructor(
    private val adherentRepository: DashboardRepository,
    private val fileRepository: FileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddAdherentUiState())

    val uiState: StateFlow<AddAdherentUiState> = _uiState.asStateFlow()

    // Canal pour les événements (Navigation / Erreurs)
    private val _uiEvent = Channel<AddAdherentUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()


    // --- Mises à jour des champs ---
    fun updatePrenoms(value: String) = _uiState.update { it.copy(prenoms = value) }
    fun updateNom(value: String) = _uiState.update { it.copy(nom = value) }
    fun updateAdresse(value: String) = _uiState.update { it.copy(adresse = value) }
    fun updateLieuNaissance(value: String) = _uiState.update { it.copy(lieuNaissance = value) }
    fun updateSexe(value: String) = _uiState.update { it.copy(sexe = value) }

    fun updateDateNaissance(date: String) {
        _uiState.update { it.copy(dateNaissance = date) }
    }

    fun updateSituationMatrimoniale(value: String) = _uiState.update { it.copy(situationMatrimoniale = value) }

    fun updateWhatsapp(value: String) {
        _uiState.update { it.copy(whatsapp = Formatters.formatPhoneNumber(value)) }
    }

    fun updateSecteurActivite(value: String) = _uiState.update { it.copy(secteurActivite = value) }
    fun updateTypePiece(value: String) = _uiState.update { it.copy(typePiece = value) }
    fun updateNumeroCNI(value: String) = _uiState.update { it.copy(numeroCNI = value) }
    fun updateNumeroExtrait(value: String) = _uiState.update { it.copy(numeroExtrait = value) }
    fun updateDepartement(value: String) = _uiState.update { it.copy(departement = value) }
    fun updateCommune(value: String) = _uiState.update { it.copy(commune = value) }

    // --- Photos Adhérent (conservé pour compatibilité) ---
    fun updatePhotoUri(uri: Uri?) = _uiState.update { it.copy(photoUri = uri) }
    fun updateRectoUri(uri: Uri?) = _uiState.update { it.copy(rectoUri = uri) }
    fun updateVersoUri(uri: Uri?) = _uiState.update { it.copy(versoUri = uri) }

    // --- Soumission du formulaire (MODIFIÉ pour accepter les URLs) ---

    fun submitWithUpload(context: Context, agentId: Long?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, uploadProgress = 0f) }

            try {
                val state = _uiState.value

                // Validation
                if (state.photoUri == null || state.rectoUri == null || state.versoUri == null) {
                    _uiEvent.send(
                        AddAdherentUiEvent.ShowSnackbar("Veuillez ajouter toutes les photos requises")
                    )
                    return@launch
                }

                // Upload avec progression
                _uiState.update { it.copy(uploadProgress = 0.1f) }
                val photoUrl = fileRepository.uploadImage(context, state.photoUri).getOrThrow()

                _uiState.update { it.copy(uploadProgress = 0.4f) }
                val rectoUrl = fileRepository.uploadImage(context, state.rectoUri).getOrThrow()

                _uiState.update { it.copy(uploadProgress = 0.7f) }
                val versoUrl = fileRepository.uploadImage(context, state.versoUri).getOrThrow()

                _uiState.update { it.copy(uploadProgress = 0.9f) }

                // Soumettre
                onSubmitForm(
                    agentId = agentId,
                    rectoUrl = rectoUrl,
                    versoUrl = versoUrl,
                    photoUrl = photoUrl
                )

                _uiState.update { it.copy(uploadProgress = 1f) }
                _uiEvent.send(AddAdherentUiEvent.ShowSnackbar("Enregistrement réussi"))

            } catch (e: Exception) {
                Log.e("UploadError", "Erreur lors de l'upload", e)

                val errorMessage = when (e) {
                    is IOException -> "Erreur de connexion. Vérifiez votre internet."
                    is java.util.concurrent.TimeoutException -> "Le délai d'attente est dépassé."
                    else -> "Erreur: ${e.message ?: "Inconnue"}"
                }

                _uiEvent.send(AddAdherentUiEvent.ShowSnackbar(errorMessage))

            } finally {
                _uiState.update { it.copy(isLoading = false, uploadProgress = 0f) }
            }
        }
    }

    fun onSubmitForm(
        agentId: Long?,
        rectoUrl: String?,
        versoUrl: String?,
        photoUrl: String?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            runCatching {
                getFormData(rectoUrl, versoUrl, photoUrl)
            }.fold(
                onSuccess = { adherent ->
                    adherentRepository.ajouterAdherent(agentId!!, adherent)
                        .onSuccess { newAdherent ->
                            _uiEvent.send(AddAdherentUiEvent.ShowSnackbar("Adhérent ajouté avec succès !"))
                            resetForm()
                            _uiEvent.send(
                                AddAdherentUiEvent.NavigateToPayment(
                                    newAdherent,
                                    uiState.value.totalCost
                                )
                            )
                        }
                        .onFailure { sendError(it as Exception) }
                },
                onFailure = { sendError(it as Exception) }
            )

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun sendError(exception: Exception) {
        val errorMessage = parseBackendError(exception)
        _uiEvent.send(AddAdherentUiEvent.ShowSnackbar(errorMessage))
    }

    private fun parseBackendError(e: Exception): String {
        return when (e) {
            is HttpException -> {
                try {
                    val errorBody = e.message
                    errorBody ?: "Erreur serveur (${e.message})"
                } catch (e: Exception) {
                    "Une erreur serveur est survenue."
                }
            }
            is IOException -> "Vérifiez votre connexion internet."
            else -> e.localizedMessage ?: "Une erreur inconnue est survenue."
        }
    }

    // --- Gestion du Modal et Dépendants ---
    fun showAddDependantModal() {
        resetDependantForm()
        _uiState.update { it.copy(isModalVisible = true, editingIndex = null) }
    }

    fun showEditDependantModal(index: Int, dependant: PersonneChargeDto) {
        _uiState.update {
            it.copy(
                isModalVisible = true,
                editingIndex = index,
                currentDependant = dependant,
                photo = null,
                photoRecto = null,
                photoVerso= null,

            )
        }
    }

    fun hideModal() {
        _uiState.update { it.copy(isModalVisible = false) }
    }

    fun updateCurrentDependant(dependant: PersonneChargeDto) {
        _uiState.update { it.copy(currentDependant = dependant) }
    }

    // Mises à jour photos Modal
    fun updateDependantPhotoUri(uri: Uri?) = _uiState.update { it.copy(photo = uri) }
    fun updateDependantRectoUri(uri: Uri?) = _uiState.update { it.copy(photoRecto = uri) }
    fun updateDependantVersoUri(uri: Uri?) = _uiState.update { it.copy(photoVerso = uri) }

    fun saveDependant() {
        _uiState.update { state ->
            val newList = state.dependants.toMutableList()
            val newDependant = state.currentDependant

            if (state.editingIndex != null) {
                newList[state.editingIndex] = newDependant
            } else {
                newList.add(newDependant)
            }

            state.copy(dependants = newList, isModalVisible = false)
        }
        resetDependantForm()
    }

    fun removeDependant(index: Int) {
        _uiState.update { state ->
            val newList = state.dependants.toMutableList()
            newList.removeAt(index)
            state.copy(dependants = newList)
        }
    }

    // --- Resets ---
    fun resetForm() {
        _uiState.value = AddAdherentUiState()
    }

    private fun resetDependantForm() {
        _uiState.update {
            it.copy(
                currentDependant = PersonneChargeDto(
                    id = "",
                    prenoms = "",
                    nom = "",
                    dateNaissance = "",
                    sexe = "M",
                    situationM = FormConstants.SITUATIONS.firstOrNull() ?: "",
                    typePiece = "CNI"
                ),
                photo = null,
                photoRecto = null,
                photoVerso = null
            )
        }
    }

    // --- Construction de l'objet de données (MODIFIÉ) ---
    private fun getFormData(
        rectoUrl: String?,
        versoUrl: String?,
        photoUrl: String?
    ): AdherentDto {
        val state = _uiState.value
        val numeroPieceFinale = if (state.typePiece == "CNI") state.numeroCNI else state.numeroExtrait

        return AdherentDto(
            prenoms = state.prenoms,
            nom = state.nom,
            adresse = state.adresse,
            lieuNaissance = state.lieuNaissance,
            sexe = state.sexe,
            dateNaissance = Formatters.formatDateForApi(state.dateNaissance),
            situationM = state.situationMatrimoniale,
            whatsapp = state.whatsapp.replace(" ", ""),
            secteurActivite = state.secteurActivite,
            typePiece = state.typePiece,
            numeroPiece = numeroPieceFinale,
            numeroCNi = numeroPieceFinale,
            departement = state.departement,
            commune = state.commune,
            region = "Thiès",
            // URLs des images uploadées
            photo = photoUrl,
            photoRecto = rectoUrl,
            photoVerso = versoUrl,
            clientUUID = "${System.currentTimeMillis()}-${(0..1000).random()}",
            personnesCharge = state.dependants.map { dep ->
                dep.copy(
                    dateNaissance = if (!dep.dateNaissance.isNullOrBlank()) {
                        Formatters.formatDateForApi(dep.dateNaissance)
                    } else {
                        dep.dateNaissance
                    }
                )
            }
        )
    }
}