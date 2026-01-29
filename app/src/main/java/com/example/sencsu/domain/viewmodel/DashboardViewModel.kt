package com.example.sencsu.domain.viewmodel

import android.util.Log
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

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository,
     val sessionManager: SessionManager
) : ViewModel() {

    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // 1. Observer l'utilisateur
        observeUser()
        // 2. Observer l'agentId de manière réactive
        observeAgentAndFetch()
    }

    private fun observeUser() {
        viewModelScope.launch {
            sessionManager.userFlow.collect { user ->
                _authState.update { it.copy(user = user) }
            }
        }
    }

    private fun observeAgentAndFetch() {
        viewModelScope.launch {
            sessionManager.agentIdFlow
                .filterNotNull()
                .distinctUntilChanged()
                .collect { id ->
                    fetchDashboardData(id)
                }
        }
    }

    fun fetchDashboardData(agentId: Long) {
        viewModelScope.launch {
            _dashboardState.update { it.copy(isLoading = true, error = null) }

            repository.getAdherentsByAgentId(agentId).fold(
                onSuccess = { adherents ->
                    _dashboardState.update {
                        it.copy(
                            isLoading = false,
                            data = DashboardResponseDto(data = adherents, success = true),
                            isSuccess = true
                        )
                    }
                },
                onFailure = { error ->
                    _dashboardState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message,
                            isSuccess = false
                        )
                    }
                }
            )
        }
    }

    // Version améliorée du refresh pour le "Pull to Refresh"
    fun refresh() {
        viewModelScope.launch {
            // On récupère la dernière valeur émise par le flow au lieu du .value risqué
            val currentId = sessionManager.agentIdFlow.firstOrNull()
            currentId?.let { fetchDashboardData(it) }
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