package com.example.sencsu.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sencsu.components.ModernAdherentRow
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.repository.SessionManager
import com.example.sencsu.domain.viewmodel.ListeAdherentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListeAdherentScreen(
    viewModel: ListeAdherentViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onAdherentClick: (Long) -> Unit,
    sessionManager: SessionManager
) {
    val state by viewModel.state.collectAsState()
    
    // Déclenche le rafraîchissement à chaque fois que l'écran est composé/revient au premier plan
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Liste des Adhérents") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Rechercher par nom, prénom ou CNI") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                }
            } else {
                LazyColumn {
                    items(state.filteredAdherents) { adherent ->
                        ModernAdherentRow(
                            adherent = adherent,
                            onClick = { onAdherentClick(adherent.id?.toLong() ?: 0L) },
                            sessionManager = sessionManager
                        )
                    }
                }
            }
        }
    }
}
