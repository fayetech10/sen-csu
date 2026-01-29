package com.example.sencsu.domain.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.repository.DashboardRepository
import com.example.sencsu.data.repository.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListeAdherentState(
    val isLoading: Boolean = false,
    val adherents: List<AdherentDto> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
) {
    val filteredAdherents: List<AdherentDto>
        get() {
            if (searchQuery.isBlank()) {
                return adherents
            }
            return adherents.filter {
                it.prenoms!!.contains(searchQuery, ignoreCase = true) ||
                        it.nom!!.contains(searchQuery, ignoreCase = true) ||
                        it.numeroCNi?.contains(searchQuery, ignoreCase = true) == true
            }
        }
}

@HiltViewModel
class ListeAdherentViewModel @Inject constructor(
    private val repository: DashboardRepository,
     val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(ListeAdherentState())
    val state: StateFlow<ListeAdherentState> = _state.asStateFlow()

    init {
        fetchAdherents()
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    private fun fetchAdherents() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                Log.d("ListeAdherentVM", "Début de la récupération des adhérents...")
                val data = repository.getDashboardData()
                Log.d("ListeAdherentVM", "Adhérents récupérés: ${data.data.size}")
                _state.update {
                    it.copy(isLoading = false, adherents = data.data)
                }
            } catch (e: Exception) {
                Log.e("ListeAdherentVM", "Erreur: ${e.message}", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Une erreur est survenue"
                    )
                }
            }
        }
    }

    fun refresh() {
        fetchAdherents()
    }
}
