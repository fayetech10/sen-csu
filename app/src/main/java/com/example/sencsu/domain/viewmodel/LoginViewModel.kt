package com.example.sencsu.domain.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sencsu.data.repository.AuthRepository
import com.example.sencsu.data.repository.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginUiEvent {
    object NavigateToDashboard : LoginUiEvent()
}

data class LoginState(
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val _eventChannel = Channel<LoginUiEvent>()
    val uiEvent = _eventChannel.receiveAsFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = authRepository.login(email, password)
                
                // CORRECTION : On utilise "token" au lieu de "accessToken"

                // On sauvegarde le token dans DataStore. C'est la seule source de vérité.
                sessionManager.saveAuthToken(response.accessToken)
                sessionManager.saveUser(response.user)
                // On envoie l'événement de navigation SEULEMENT APRES la sauvegarde.
                _eventChannel.send(LoginUiEvent.NavigateToDashboard)
                _state.update { it.copy(isLoading = false) }

            } catch (e: Exception) {
                Log.e("LoginViewModel", "Erreur login: ${e.message}", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Une erreur est survenue"
                    )
                }
            }
        }
    }
}