package com.example.sencsu.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sencsu.data.repository.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppNavigationViewModel @Inject constructor(
     val sessionManager: SessionManager
) : ViewModel() {

    /**
     * Flow de l'agentId courant
     */
    val agentId: StateFlow<Long?> = sessionManager.agentIdFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /**
     * Flow de l'utilisateur courant
     */
    val currentUser = sessionManager.userFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /**
     * Flow pour vérifier si l'utilisateur est authentifié
     */
    val isAuthenticated: StateFlow<Boolean> = sessionManager.isAuthenticatedFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    /**
     * Événement de déconnexion pour rediriger vers l'écran de login
     */
    val logoutEvent: SharedFlow<Unit> = sessionManager.logoutEvent

    /**
     * Fonction de déconnexion
     */
    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSessionAndNotify()
        }
    }

    /**
     * Récupère l'agentId de manière suspendue
     */
    suspend fun getAgentId(): Long? = sessionManager.getAgentId()
}