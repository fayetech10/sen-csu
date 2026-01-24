package com.example.sencsu.domain.viewmodel

import android.util.Log
import androidx.datastore.preferences.protobuf.LazyStringArrayList.emptyList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sencsu.data.remote.dto.DashboardResponseDto
import com.example.sencsu.data.repository.DashboardRepository
import com.example.sencsu.data.repository.SessionManager
import com.example.sencsu.domain.model.AuthState
import com.example.sencsu.domain.model.DashboardState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.emptyList

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository,
    val sessionManager: SessionManager // Rendre sessionManager public
) : ViewModel() {

    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Exposition de l'agentId pour une utilisation externe si nécessaire
    val agentId: StateFlow<Long?> = sessionManager.agentIdFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        observeUser()
        observeAgentIdAndFetchData()
    }

    /**
     * Observe l'agentId et déclenche le chargement des données
     */
    private fun observeAgentIdAndFetchData() {
        viewModelScope.launch {
            sessionManager.agentIdFlow
                .filterNotNull()
                .distinctUntilChanged() // Évite les rechargements inutiles
                .collect { agentId ->
                    fetchDashboardData(agentId)
                }
        }
    }

    /**
     * Observe les changements d'utilisateur
     */
    private fun observeUser() {
        viewModelScope.launch {
            sessionManager.userFlow
                .catch { e ->
                    _authState.update {
                        it.copy(isLoading = false, error = e.message)
                    }
                }
                .collect { user ->
                    _authState.update {
                        it.copy(isLoading = false, user = user, error = null)
                    }
                }
        }
    }

    /**
     * Charge les données du dashboard pour un agent spécifique
     */
    private fun fetchDashboardData(agentId: Long) {
        viewModelScope.launch {
            _dashboardState.update {
                it.copy(isLoading = true, error = null, isSuccess = false)
            }

            repository.getAdherentsByAgentId(agentId)
                .fold(
                    onSuccess = { adherent ->
                        _dashboardState.update {
                            Log.d("DashboardViewModel", "Adhérent reçu : $adherent")
                            it.copy(
                                isLoading = false,
                                data = DashboardResponseDto(
                                    adherents = adherent,
                                    success = true,
                                    message = "Liste des adhérents"
                                ),
                                isSuccess = true,
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        Log.e("DashboardViewModel", "Erreur lors de la récupération des données", error)
                        _dashboardState.update {
                            it.copy(
                                isLoading = false,
                                // Si c'est un 404, on pourrait choisir de ne pas mettre d'erreur
                                // mais de passer à un état "Empty"
                                error = if (error.message?.contains("404") == true) null else error.message,
//                                data = if (error.message?.contains("404") == true) DashboardResponseDto("Adherents", error.message, ) else null,
                                isSuccess = error.message?.contains("404") == true
                            )
                        }
                    }
                )
        }
    }

    /**
     * Rafraîchir manuellement les données
     */
    fun refresh() {
        viewModelScope.launch {
            agentId.value?.let { id ->
                fetchDashboardData(id)
            }
        }
    }

    /**
     * Déconnexion de l'utilisateur
     */
    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSessionAndNotify()
            _dashboardState.value = DashboardState()
            _authState.value = AuthState()
        }
    }
}