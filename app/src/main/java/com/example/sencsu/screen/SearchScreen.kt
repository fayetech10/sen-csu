package com.example.sencsu.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sencsu.components.ModernAdherentRow
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.repository.SessionManager
import com.example.sencsu.domain.viewmodel.SearchViewModel
import com.example.sencsu.screen.SearchTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onAdherentClick: (String) -> Unit
) {
    // On collecte chaque état nécessaire depuis le ViewModel
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredAdherents by viewModel.filteredAdherents.collectAsState()

    Scaffold(
        topBar = {
            SearchTopBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            FilterControls(
                viewModel = viewModel,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(error!!, color = MaterialTheme.colorScheme.error)
                    }
                }
                else -> {
                    AdherentList(
                        adherents = filteredAdherents,
                        onAdherentClick = onAdherentClick as (String) -> Unit,
                        sessionManager = viewModel.sessionManager
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(query: String, onQueryChange: (String) -> Unit, onNavigateBack: () -> Unit) {
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Nom, prénom, CNI...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Vider")
                        }
                    }
                }
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
            }
        }
    )
}

@Composable
private fun FilterControls(
    viewModel: SearchViewModel,
    modifier: Modifier = Modifier
) {
    // On collecte les états pour les filtres
    val selectedRegion by viewModel.selectedRegion.collectAsState()
    val availableRegions by viewModel.availableRegions.collectAsState()
    val selectedDepartement by viewModel.selectedDepartement.collectAsState()
    val availableDepartements by viewModel.availableDepartements.collectAsState()
    val selectedCommune by viewModel.selectedCommune.collectAsState()
    val availableCommunes by viewModel.availableCommunes.collectAsState()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChipMenu(
            label = "Région",
            options = availableRegions,
            selected = selectedRegion,
            onSelected = viewModel::onRegionChange,
            modifier = Modifier.weight(1f)
        )
        FilterChipMenu(
            label = "Département",
            options = availableDepartements,
            selected = selectedDepartement,
            onSelected = viewModel::onDepartementChange,
            modifier = Modifier.weight(1f)
        )
        FilterChipMenu(
            label = "Commune",
            options = availableCommunes,
            selected = selectedCommune,
            onSelected = viewModel::onCommuneChange,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipMenu(
    label: String,
    options: List<String>,
    selected: String?,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        FilterChip(
            selected = selected != null,
            onClick = { expanded = true },
            label = { Text(selected ?: label) },
            Modifier.menuAnchor(
                type = MenuAnchorType.PrimaryNotEditable,
                enabled = true
            ),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (selected != null) { 
                DropdownMenuItem(text = { Text("Tout", fontWeight = FontWeight.Bold) }, onClick = { onSelected(null); expanded = false })
                HorizontalDivider()
            }
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelected(option); expanded = false })
            }
        }
    }
}

@Composable
private fun AdherentList(
     sessionManager: SessionManager,
    adherents: List<AdherentDto>, onAdherentClick: (String) -> Unit) {
    if (adherents.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("Aucun résultat trouvé.", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            items(adherents, key = { it.id!! }) { adherent ->
                ModernAdherentRow(
                    adherent = adherent,
                    onClick = { onAdherentClick(adherent.id ?: "") },
                    sessionManager = sessionManager
                )
            }
        }
    }
}
