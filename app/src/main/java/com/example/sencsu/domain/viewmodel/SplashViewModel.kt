
package com.example.sencsu.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sencsu.data.repository.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    init {
        viewModelScope.launch {
            // On vérifie directement dans DataStore si un token existe.
            val authToken = sessionManager.tokenFlow.first()
            _isLoggedIn.value = !authToken.isNullOrBlank()
        }
    }
}
