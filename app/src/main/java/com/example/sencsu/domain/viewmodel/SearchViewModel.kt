package com.example.sencsu.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.repository.DashboardRepository
import com.example.sencsu.data.repository.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: DashboardRepository,
    val  sessionManager: SessionManager
) : ViewModel() {

    // --- SOURCES DE VÉRITÉ ---
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)
    private val _allAdherents = MutableStateFlow<List<AdherentDto>>(emptyList())

    // --- CRITÈRES DE FILTRE MODIFIABLES PAR L'UI ---
    private val _searchQuery = MutableStateFlow("")
    private val _selectedRegion = MutableStateFlow<String?>(null)
    private val _selectedDepartement = MutableStateFlow<String?>(null)
    private val _selectedCommune = MutableStateFlow<String?>(null)

    // --- ÉTAT EXPOSÉ À L'UI (COMBINÉ ET DÉRIVÉ) ---

    // État de base de l'UI
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val error: StateFlow<String?> = _error.asStateFlow()

    // Valeurs actuelles des filtres
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val selectedRegion: StateFlow<String?> = _selectedRegion.asStateFlow()
    val selectedDepartement: StateFlow<String?> = _selectedDepartement.asStateFlow()
    val selectedCommune: StateFlow<String?> = _selectedCommune.asStateFlow()

    // Listes pour les menus déroulants (dynamiques)
    val availableRegions: StateFlow<List<String>> = _allAdherents
        .map { it.mapNotNull { adherent -> adherent.region }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableDepartements: StateFlow<List<String>> = combine(_allAdherents, _selectedRegion) { adherents, region ->
        adherents
            .filter { region == null || it.region == region }
            .mapNotNull { it.departement }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableCommunes: StateFlow<List<String>> = combine(_allAdherents, _selectedDepartement) { adherents, departement ->
        adherents
            .filter { departement == null || it.departement == departement }
            .mapNotNull { it.commune }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // La liste finale, filtrée et prête pour l'affichage
    val filteredAdherents: StateFlow<List<AdherentDto>> = combine(
        _allAdherents, _searchQuery, _selectedRegion, _selectedDepartement, _selectedCommune
    ) { adherents, query, region, departement, commune ->
        adherents.filter { adherent ->
            val matchesSearch = query.isBlank() ||
                    adherent.nom!!.contains(query, true) ||
                    adherent.prenoms!!.contains(query, true) ||
                    adherent.numeroCNi?.contains(query, true) == true

            val matchesRegion = region?.let { it == adherent.region } ?: true
            val matchesDepartement = departement?.let { it == adherent.departement } ?: true
            val matchesCommune = commune?.let { it == adherent.commune } ?: true

            matchesSearch && matchesRegion && matchesDepartement && matchesCommune
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadAllAdherents()
    }

    // --- ACTIONS PUBLIQUES ---
    fun onSearchQueryChange(query: String) { _searchQuery.value = query }

    fun onRegionChange(region: String?) {
        _selectedRegion.value = region
        _selectedDepartement.value = null // Réinitialise les filtres dépendants
        _selectedCommune.value = null
    }

    fun onDepartementChange(departement: String?) {
        _selectedDepartement.value = departement
        _selectedCommune.value = null // Réinitialise le filtre dépendant
    }

    fun onCommuneChange(commune: String?) { _selectedCommune.value = commune }

    private fun loadAllAdherents() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getDashboardData()
                _allAdherents.value = response.data
            } catch (e: Exception) {
                _error.value = "Impossible de charger les adhérents."
            } finally {
                _isLoading.value = false
            }
        }
    }
}