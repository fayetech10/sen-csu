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

private const val ADHERENT_PRICE = 4500
private const val DEPENDANT_PRICE = 3500

sealed class AddAdherentUiEvent {
    data class ShowSnackbar(val message: String) : AddAdherentUiEvent()
    data class NavigateToPayment(val adherentId: Long?, val montantTotal: Int) : AddAdherentUiEvent()
}

@HiltViewModel
class AddAdherentViewModel @Inject constructor(
    private val adherentRepository: DashboardRepository,
    private val fileRepository: FileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddAdherentUiState())
    val uiState: StateFlow<AddAdherentUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<AddAdherentUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    // --- Mises à jour des champs Adhérent ---
    fun updatePrenoms(value: String) = _uiState.update { it.copy(prenoms = value) }
    fun updateNom(value: String) = _uiState.update { it.copy(nom = value) }
    fun updateAdresse(value: String) = _uiState.update { it.copy(adresse = value) }
    fun updateLieuNaissance(value: String) = _uiState.update { it.copy(lieuNaissance = value) }
    fun updateSexe(value: String) = _uiState.update { it.copy(sexe = value) }
    fun updateDateNaissance(date: String) = _uiState.update { it.copy(dateNaissance = date) }
    fun updateSituationMatrimoniale(value: String) = _uiState.update { it.copy(situationMatrimoniale = value) }
    fun updateWhatsapp(value: String) = _uiState.update { it.copy(whatsapp = Formatters.formatPhoneNumber(value)) }
    fun updateSecteurActivite(value: String) = _uiState.update { it.copy(secteurActivite = value) }
    fun updateTypePiece(value: String) = _uiState.update { it.copy(typePiece = value) }
    fun updateNumeroCNI(value: String) = _uiState.update { it.copy(numeroCNI = value) }
    fun updateNumeroExtrait(value: String) = _uiState.update { it.copy(numeroExtrait = value) }
    fun updateDepartement(value: String) = _uiState.update { it.copy(departement = value) }
    fun updateCommune(value: String) = _uiState.update { it.copy(commune = value) }

    // --- Photos Adhérent Principal ---
    fun updatePhotoUri(uri: Uri?) = _uiState.update { it.copy(photoUri = uri) }
    fun updateRectoUri(uri: Uri?) = _uiState.update { it.copy(rectoUri = uri) }
    fun updateVersoUri(uri: Uri?) = _uiState.update { it.copy(versoUri = uri) }

    // --- Photos Personnes à Charge (Modal) ---
    fun updateDependantPhotoUri(uri: Uri?) = _uiState.update {
        it.copy(currentDependant = it.currentDependant.copy(photo = uri?.toString()))
    }
    fun updateDependantRectoUri(uri: Uri?) = _uiState.update {
        it.copy(currentDependant = it.currentDependant.copy(photoRecto = uri?.toString()))
    }
    fun updateDependantVersoUri(uri: Uri?) = _uiState.update {
        it.copy(currentDependant = it.currentDependant.copy(photoVerso = uri?.toString()))
    }

    // --- Logique d'Upload et Soumission ---
    // La fonction manquante qui corrige votre erreur
    fun updateCurrentDependant(dependant: PersonneChargeDto) {
        _uiState.update { it.copy(currentDependant = dependant) }
    }

    fun submitWithUpload(context: Context, agentId: Long?) {
        if (agentId == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val state = _uiState.value

                // 1. Upload images Adhérent Principal
                val photoUrl = state.photoUri?.let { fileRepository.uploadImage(context, it).getOrThrow() }
                val rectoUrl = state.rectoUri?.let { fileRepository.uploadImage(context, it).getOrThrow() }
                val versoUrl = state.versoUri?.let { fileRepository.uploadImage(context, it).getOrThrow() }

                // 2. Upload images Dépendants en parallèle
                val updatedDeps = uploadDependantsImages(context)

                // 3. Préparation de l'objet DTO final
                val adherentDto = getFormData(
                    photoUrl = photoUrl,
                    rectoUrl = rectoUrl,
                    versoUrl = versoUrl,
                    updatedDependants = updatedDeps
                )

                // 4. Envoi au serveur
                adherentRepository.ajouterAdherent(agentId, adherentDto)
                    .onSuccess { idResponse ->
                        val total = calculateTotalCost(state.dependants.size)
                        _uiEvent.send(AddAdherentUiEvent.ShowSnackbar("Adhérent ajouté avec succès !"))
                        _uiEvent.send(AddAdherentUiEvent.NavigateToPayment(idResponse.adherentId, total))
                        resetForm()
                    }
                    .onFailure { throw it }

            } catch (e: Exception) {
                Log.e("SubmitError", "Erreur lors de la soumission", e)
                sendError(e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun uploadDependantsImages(context: Context): List<PersonneChargeDto> {
        return _uiState.value.dependants.map { dep ->
            suspend fun uploadIfLocal(uriString: String?): String? {
                return if (uriString?.startsWith("content://") == true) {
                    fileRepository.uploadImage(context, Uri.parse(uriString)).getOrNull()
                } else uriString
            }

            dep.copy(
                photo = uploadIfLocal(dep.photo),
                photoRecto = uploadIfLocal(dep.photoRecto),
                photoVerso = uploadIfLocal(dep.photoVerso)
            )
        }
    }

    private fun getFormData(
        photoUrl: String?,
        rectoUrl: String?,
        versoUrl: String?,
        updatedDependants: List<PersonneChargeDto>
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
            whatsapp = state.whatsapp,
            secteurActivite = state.secteurActivite,
            typePiece = state.typePiece,
            numeroPiece = numeroPieceFinale,
            numeroCNi = numeroPieceFinale,
            departement = state.departement,
            commune = state.commune,
            region = "Thiès",
            regime = "Contributif",
            typeBenef = "Classique",
            typeAdhesion = "Familiale",
            photo = photoUrl,
            photoRecto = rectoUrl,
            photoVerso = versoUrl,
            clientUUID = "${System.currentTimeMillis()}-${(0..1000).random()}",
            personnesCharge = updatedDependants.map { dep ->
                dep.copy(
                    dateNaissance = if (!dep.dateNaissance.isNullOrBlank()) {
                        Formatters.formatDateForApi(dep.dateNaissance)
                    } else dep.dateNaissance
                )
            }
        )
    }

    // --- Gestion des erreurs ---
    private suspend fun sendError(exception: Exception) {

        val errorMessage = when (exception) {

            is HttpException -> {
                try {
                    exception.response()
                        ?.errorBody()
                        ?.string()
                        ?: "Erreur serveur (${exception.code()})"
                } catch (e: Exception) {
                    "Erreur serveur (${exception.code()})"
                }
            }

            is IOException -> "Problème de connexion internet."

            else -> exception.localizedMessage ?: "Une erreur est survenue."
        }

        _uiEvent.send(AddAdherentUiEvent.ShowSnackbar(errorMessage))
    }


    // --- Gestion du Modal Personne à Charge ---
    fun showAddDependantModal() {
        resetDependantForm()
        _uiState.update { it.copy(isModalVisible = true, editingIndex = null) }
    }

    fun showEditDependantModal(index: Int, dependant: PersonneChargeDto) {
        _uiState.update {
            it.copy(
                isModalVisible = true,
                editingIndex = index,
                currentDependant = dependant
            )
        }
    }

    fun hideModal() = _uiState.update { it.copy(isModalVisible = false) }

    fun saveDependant() {
        _uiState.update { state ->
            val newList = state.dependants.toMutableList()
            if (state.editingIndex != null) {
                newList[state.editingIndex] = state.currentDependant
            } else {
                newList.add(state.currentDependant)
            }
            state.copy(
                dependants = newList,
                totalCost = calculateTotalCost(newList.size),
                isModalVisible = false
            )
        }
        resetDependantForm()
    }

    fun removeDependant(index: Int) {
        _uiState.update { state ->
            val newList = state.dependants.toMutableList()
            newList.removeAt(index)
            state.copy(
                dependants = newList,
                totalCost = calculateTotalCost(newList.size)
            )
        }
    }

    // --- Resets et Calculs ---
    fun resetForm() {
        _uiState.value = AddAdherentUiState()
    }

    private fun resetDependantForm() {
        _uiState.update {
            it.copy(
                currentDependant = PersonneChargeDto(
                    prenoms = "",
                    nom = "",
                    dateNaissance = "",
                    sexe = "M",
                    situationM = FormConstants.SITUATIONS.firstOrNull() ?: "",
                    typePiece = "CNI"
                )
            )
        }
    }

    private fun calculateTotalCost(count: Int): Int = ADHERENT_PRICE + (count * DEPENDANT_PRICE)
}