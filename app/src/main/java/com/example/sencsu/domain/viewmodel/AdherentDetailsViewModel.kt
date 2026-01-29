package com.example.sencsu.domain.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.CotisationDto
import com.example.sencsu.data.remote.dto.PaiementDto
import com.example.sencsu.data.remote.dto.PersonneChargeDto
import com.example.sencsu.data.repository.AdherentRepository
import com.example.sencsu.data.repository.Cotisationepository
import com.example.sencsu.data.repository.DashboardRepository
import com.example.sencsu.data.repository.PaiementRepository
import com.example.sencsu.data.repository.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdherentDetailsState(
    val isLoading: Boolean = false,
    val adherent: AdherentDto? = null,
    val error: String? = null,
    val paiements: List<PaiementDto> = emptyList(),
    val cotisations: List<CotisationDto> = emptyList(),
    val showDeleteAdherentDialog: Boolean = false,
    val personToDelete: PersonneChargeDto? = null,
    val showAddPersonneModal: Boolean = false,
    val selectedImageUrl: String? = null,
    val newPersonne: PersonneChargeDto = PersonneChargeDto()
)

sealed class DetailsUiEvent {
    data class ShowSnackbar(val message: String) : DetailsUiEvent()
    data object AdherentDeleted : DetailsUiEvent()
}

@HiltViewModel
class AdherentDetailsViewModel @Inject constructor(
    private val adherentRepository: AdherentRepository,
    savedStateHandle: SavedStateHandle,
    val sessionManager: SessionManager,
    private val paiementRepository: PaiementRepository,
    private val cotisationRepository: Cotisationepository,
    private val dashdoardRepository: DashboardRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdherentDetailsState())
    val state: StateFlow<AdherentDetailsState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<DetailsUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val adherentIdStr: String? = savedStateHandle["id"]

    init {
        refresh()
    }

    fun refresh() {
        val id = adherentIdStr?.toLongOrNull()
        if (id != null) {
            fetchAdherentDetails(id)
        } else {
            _state.update { it.copy(error = "Identifiant invalide.") }
        }
    }

    private fun fetchAdherentDetails(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            adherentRepository.getAdherentById(id).fold(
                onSuccess = { adherent ->
                    _state.update { it.copy(isLoading = false, adherent = adherent) }
                    loadPaiementByIdadherent()
                    loadCotisationsByIdadherent()
                },
                onFailure = { error ->
                    _state.update { it.copy(isLoading = false, error = error.message ?: "Erreur réseau") }
                }
            )
        }
    }

    fun loadCotisationsByIdadherent() {
        val id = adherentIdStr?.toLongOrNull() ?: return
        viewModelScope.launch {
            try {
                val result = cotisationRepository.getCotisationByIdahderent(id)
                _state.update { it.copy(cotisations = result) }
            } catch (e: Exception) {
                Log.e("AdherentVM", "Erreur cotisations", e)
            }
        }
    }

    fun loadPaiementByIdadherent() {
        val id = adherentIdStr?.toLongOrNull() ?: return
        viewModelScope.launch {
            try {
                val result = paiementRepository.getPaiementsByAdherentId(id)
                _state.update { it.copy(paiements = result) }
            } catch (e: Exception) {
                Log.e("AdherentVM", "Erreur paiements", e)
            }
        }
    }

    // --- Actions ---
    fun showDeleteAdherentConfirmation() = _state.update { it.copy(showDeleteAdherentDialog = true) }
    fun cancelDeleteAdherent() = _state.update { it.copy(showDeleteAdherentDialog = false) }

    fun confirmDeleteAdherent() {
        val id = adherentIdStr?.toLongOrNull() ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showDeleteAdherentDialog = false) }
            try {
                adherentRepository.deleteAdherent(id)
                // Mise à jour silencieuse du dashboard en arrière-plan
                launch {
                   val  reuslt = dashdoardRepository.getDashboardData()
                    Log.d("Adherent", "Adherent : ${reuslt.data.size}")
                }

                _uiEvent.emit(DetailsUiEvent.ShowSnackbar("Adhérent supprimé"))
                _uiEvent.emit(DetailsUiEvent.AdherentDeleted)

            } catch (e: Exception) {
                Log.e("Adherent",e.message.toString())
                _state.update { it.copy(isLoading = false) }
                _uiEvent.emit(DetailsUiEvent.ShowSnackbar("Erreur: ${e.message}"))
            }
        }
    }

    fun showDeletePersonneConfirmation(personne: PersonneChargeDto) = _state.update { it.copy(personToDelete = personne) }
    fun cancelDeletePersonne() = _state.update { it.copy(personToDelete = null) }

    fun confirmDeletePersonne() {
        val personneId = _state.value.personToDelete?.id ?: return
        viewModelScope.launch {
            try {
                adherentRepository.deletePersonneCharge(personneId)
                _uiEvent.emit(DetailsUiEvent.ShowSnackbar("Bénéficiaire supprimé"))
                refresh()
            } catch (e: Exception) {
                _uiEvent.emit(DetailsUiEvent.ShowSnackbar("Erreur: ${e.message}"))
            }
            cancelDeletePersonne()
        }
    }

    fun openImagePreview(url: String?) {
        if (!url.isNullOrBlank()) _state.update { it.copy(selectedImageUrl = url) }
    }
    fun closeImagePreview() = _state.update { it.copy(selectedImageUrl = null) }

    fun onAddPersonneClicked() = _state.update { it.copy(showAddPersonneModal = true, newPersonne = PersonneChargeDto()) }
    fun onDismissAddPersonneModal() = _state.update { it.copy(showAddPersonneModal = false) }
    fun onNewPersonneChange(p: PersonneChargeDto) = _state.update { it.copy(newPersonne = p) }

    fun onSaveNewPersonne() {
        val id = adherentIdStr?.toLongOrNull() ?: return
        viewModelScope.launch {
            try {
                adherentRepository.addPersonneCharge(id, _state.value.newPersonne)
                _uiEvent.emit(DetailsUiEvent.ShowSnackbar("Ajout réussi"))
                onDismissAddPersonneModal()
                refresh()
            } catch (e: Exception) {
                _uiEvent.emit(DetailsUiEvent.ShowSnackbar("Erreur: ${e.message}"))
            }
        }
    }
}