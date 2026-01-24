package com.example.sencsu.domain.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.PersonneChargeDto
import com.example.sencsu.data.repository.AdherentRepository
import com.example.sencsu.data.repository.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdherentDetailsState(
    val isLoading: Boolean = false,
    val adherent: AdherentDto? = null,
    val error: String? = null,
    val showDeleteAdherentDialog: Boolean = false,
    val showDeletePersonneDialog: PersonneChargeDto? = null,
    val showAddPersonneModal: Boolean = false,
    val newPersonne: PersonneChargeDto = PersonneChargeDto(),
    val showImageViewerDialog: Boolean = false,
    val imageToViewUrl: String? = null
)

sealed class DetailsUiEvent {
    data class ShowSnackbar(val message: String) : DetailsUiEvent()
    object AdherentDeleted : DetailsUiEvent()
}

@HiltViewModel
class AdherentDetailsViewModel @Inject constructor(
    private val adherentRepository: AdherentRepository,
    savedStateHandle: SavedStateHandle,
    val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(AdherentDetailsState())
    val state: StateFlow<AdherentDetailsState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<DetailsUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val adherentIdStr: String? = savedStateHandle["id"]

    init {
        val id = adherentIdStr?.toLongOrNull()
        if (id != null) {
            fetchAdherentDetails(id)
        } else {
            _state.update { it.copy(error = "Identifiant d'adhérent invalide.") }
        }
    }

    fun fetchAdherentDetails(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            adherentRepository.getAdherentById(id)
                .fold(
                    onSuccess = { adherent ->
                        _state.update { it.copy(isLoading = false, adherent = adherent) }
                    },
                    onFailure = { error ->
                        _state.update { it.copy(isLoading = false, error = error.message ?: "Erreur de chargement") }
                    }
                )
        }
    }

    fun confirmDeleteAdherent() {
        val id = adherentIdStr?.toLongOrNull() ?: return
        viewModelScope.launch {
            try {
                adherentRepository.deleteAdherent(id)
                _uiEvent.emit(DetailsUiEvent.AdherentDeleted)
                _uiEvent.emit(DetailsUiEvent.ShowSnackbar("Adhérent supprimé"))
            } catch (e: Exception) {
                _uiEvent.emit(DetailsUiEvent.ShowSnackbar("Erreur: ${e.message}"))
            }
        }
    }

    fun onSaveNewPersonne() {
        val id = adherentIdStr?.toLongOrNull() ?: return
        viewModelScope.launch {
            try {
                adherentRepository.addPersonneCharge(id, _state.value.newPersonne)
                _uiEvent.emit(DetailsUiEvent.ShowSnackbar("Ajout réussi"))
                fetchAdherentDetails(id)
                onDismissAddPersonneModal()
            } catch (e: Exception) {
                _uiEvent.emit(DetailsUiEvent.ShowSnackbar("Erreur: ${e.message}"))
            }
        }
    }

    fun confirmDeletePersonne() {
        val parentId = adherentIdStr?.toLongOrNull() ?: return
        val personneId = _state.value.showDeletePersonneDialog?.id?.toLongOrNull() ?: return
        viewModelScope.launch {
            try {
                adherentRepository.deletePersonneCharge(personneId)
                _uiEvent.emit(DetailsUiEvent.ShowSnackbar("Supprimé"))
                fetchAdherentDetails(parentId)
            } catch (e: Exception) {
                _uiEvent.emit(DetailsUiEvent.ShowSnackbar("Erreur"))
            }
            onDismissDialog()
        }
    }

    fun onAddPersonneClicked() = _state.update { it.copy(showAddPersonneModal = true, newPersonne = PersonneChargeDto()) }
    fun onDismissAddPersonneModal() = _state.update { it.copy(showAddPersonneModal = false) }
    fun onDeleteAdherentClicked() = _state.update { it.copy(showDeleteAdherentDialog = true) }
    fun onDeletePersonneClicked(p: PersonneChargeDto) = _state.update { it.copy(showDeletePersonneDialog = p) }
    fun onDismissDialog() = _state.update { it.copy(showDeleteAdherentDialog = false, showDeletePersonneDialog = null) }
    fun onNewPersonneChange(p: PersonneChargeDto) = _state.update { it.copy(newPersonne = p) }

    fun openImagePreview(url: String) = _state.update { it.copy(showImageViewerDialog = true, imageToViewUrl = url) }
    fun closeImagePreview() = _state.update { it.copy(showImageViewerDialog = false, imageToViewUrl = null) }
    fun downloadImage(url: String) { /* Logique à venir */ }
}
