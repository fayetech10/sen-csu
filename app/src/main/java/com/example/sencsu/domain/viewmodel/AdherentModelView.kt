package com.example.sencsu.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.repository.AdherentRepository
import com.example.sencsu.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// État de l'interface (UI State)
data class AdherentListUiState(
    val adherents: List<AdherentDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// Événements (Snackbars, Navigation)
sealed class AdherentUiEvent {
    data class NavigateToPayment(val formData: AdherentDto, val montantTotal: Int) : AdherentUiEvent()
    data class ShowSuccess(val message: String) : AdherentUiEvent()
    data class ShowError(val message: String) : AdherentUiEvent()
}

@HiltViewModel
class AdherentViewModel @Inject constructor(
    private val adherentRepository: AdherentRepository,
    private val dashboardRepository: DashboardRepository // Renommé pour plus de clarté
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdherentListUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AdherentUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        loadAdherents()
    }

    /**
     * Charge la liste des adhérents via le DashboardRepository
     */
    fun loadAdherents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // On suppose que getDashboardData() renvoie les données du tableau de bord
                // Adapte ici selon ce que renvoie réellement ton dashboardRepository
                val result = dashboardRepository.getDashboardData()

                // Si ton dashboard contient une liste d'adhérents (ex: result.adherents)
                _uiState.update {
                    it.copy(
                        adherents = result.data,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                _uiEvent.emit(AdherentUiEvent.ShowError("Erreur de chargement : ${e.message}"))
            }
        }
    }

    /**
     * Supprime un adhérent et rafraîchit la liste
     */
//    fun onDeleteAdherent(id: Long) {
//        viewModelScope.launch {
//            _uiState.update { it.copy(isLoading = true) }
//
//            val result = adherentRepository.deleteAdherent(id)
//
//            result.onSuccess {
//                _uiEvent.emit(AdherentUiEvent.ShowSuccess("Adhérent supprimé avec succès"))
//                loadAdherents() // Rafraîchir après suppression
//            }.onFailure { error ->
//                _uiState.update { it.copy(isLoading = false) }
//                _uiEvent.emit(
//                    AdherentUiEvent.ShowError(
//                        error.message ?: "Erreur lors de la suppression"
//                    )
//                )
//            }
//        }
//    }
}