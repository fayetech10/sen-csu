package com.example.sencsu.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sencsu.components.ServerImage
import com.example.sencsu.components.modals.AddPersonneChargeModal
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.PersonneChargeDto
import com.example.sencsu.data.repository.SessionManager
import com.example.sencsu.domain.viewmodel.AdherentDetailsViewModel
import com.example.sencsu.domain.viewmodel.DetailsUiEvent

// Couleurs (à adapter selon votre FormConstants si besoin)
private val BackgroundColor = Color(0xFFF1F5F9)
private val PrimaryColor = Color(0xFF2563EB)
private val TextDark = Color(0xFF1E293B)
private val TextLight = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdherentDetailsScreen(
    viewModel: AdherentDetailsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Gestion des événements UI (Navigation, Snackbar)
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is DetailsUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is DetailsUiEvent.AdherentDeleted -> onNavigateBack()
            }
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Détails Dossier", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showDeleteAdherentConfirmation() }) {
                        Icon(Icons.Rounded.Delete, "Supprimer", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundColor)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAddPersonneClicked() },
                containerColor = PrimaryColor,
                contentColor = Color.White
            ) {
                Icon(Icons.Rounded.PersonAdd, "Ajouter personne")
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (state.error != null) {
                ErrorView(error = state.error!!) { viewModel.refresh() }
            } else if (state.adherent != null) {
                AdherentContent(
                    adherent = state.adherent!!,
                    sessionManager = viewModel.sessionManager,
                    viewModel = viewModel
                )
            }
        }
    }

    // --- MODALES ET DIALOGUES ---

    // 1. Modale d'ajout
    if (state.showAddPersonneModal) {
        AddPersonneChargeModal(
            personne = state.newPersonne,
            onPersonneChange = viewModel::onNewPersonneChange,
            onSave = viewModel::onSaveNewPersonne,
            onDismiss = viewModel::onDismissAddPersonneModal
        )
    }

    // 2. Visionneuse d'image plein écran
    state.selectedImageUrl?.let { url ->
        FullScreenImageViewer(
            imageUrl = url,
            sessionManager = viewModel.sessionManager,
            onDismiss = { viewModel.closeImagePreview() }
        )
    }

    // 3. Confirmation Suppression Adhérent
    if (state.showDeleteAdherentDialog) {
        DeleteConfirmationDialog(
            title = "Supprimer l'adhérent ?",
            text = "Cette action est irréversible. Toutes les données associées seront perdues.",
            onConfirm = { viewModel.confirmDeleteAdherent() },
            onDismiss = { viewModel.cancelDeleteAdherent() }
        )
    }

    // 4. Confirmation Suppression Personne
    state.personToDelete?.let { personne ->
        DeleteConfirmationDialog(
            title = "Supprimer le bénéficiaire ?",
            text = "Voulez-vous vraiment retirer ${personne.prenoms} ${personne.nom} ?",
            onConfirm = { viewModel.confirmDeletePersonne() },
            onDismiss = { viewModel.cancelDeletePersonne() }
        )
    }
}

@Composable
fun AdherentContent(
    adherent: AdherentDto,
    sessionManager: SessionManager,
    viewModel: AdherentDetailsViewModel
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp, top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // En-tête Profil
        item {
            ProfileHeaderCard(adherent, sessionManager, viewModel)
        }

        // Infos Personnelles
        item {
            SectionCard(title = "Informations Civiles", icon = Icons.Rounded.Badge) {
                InfoRow("CNI", adherent.numeroCNi)
                InfoRow("Né(e) le", adherent.dateNaissance)
                InfoRow("À", adherent.lieuNaissance)
                InfoRow("Adresse", adherent.adresse)
                InfoRow("Téléphone", adherent.whatsapp) // Ou telephone si dispo
            }
        }

        // Pièces jointes
        if (adherent.photoRecto != null || adherent.photoVerso != null) {
            item {
                SectionCard(title = "Documents", icon = Icons.Rounded.FolderShared) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (adherent.photoRecto != null)
                            DocumentThumbnail(
                                "Recto",
                                adherent.photoRecto,
                                sessionManager,
                                Modifier.weight(1f)
                            ) { viewModel.openImagePreview(adherent.photoRecto) }

                        if (adherent.photoVerso != null)
                            DocumentThumbnail(
                                "Verso",
                                adherent.photoVerso,
                                sessionManager,
                                Modifier.weight(1f)
                            ) { viewModel.openImagePreview(adherent.photoVerso) }
                    }
                }
            }
        }

        // Liste des personnes à charge
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Bénéficiaires (${adherent.personnesCharge.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
        }

        if (adherent.personnesCharge.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp), contentAlignment = Alignment.Center
                ) {
                    Text("Aucun bénéficiaire enregistré", color = TextLight, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(adherent.personnesCharge) { personne ->
                PersonneChargeItem(
                    personne = personne,
                    sessionManager = sessionManager,
                    onImageClick = { viewModel.openImagePreview(personne.photo) },
                    onDeleteClick = { viewModel.showDeletePersonneConfirmation(personne) }
                )
            }
        }
    }
}

@Composable
fun ProfileHeaderCard(
    adherent: AdherentDto,
    sessionManager: SessionManager,
    viewModel: AdherentDetailsViewModel
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                ServerImage(
                    filename = adherent.photo,
                    sessionManager = sessionManager,
                    contentDescription = "Photo de profil",
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2E8F0))
                        .clickable { viewModel.openImagePreview(adherent.photo) },
                    contentScale = ContentScale.Crop,
                )
                // Petite icône loupe pour indiquer que c'est cliquable
                Icon(
                    Icons.Rounded.ZoomIn,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.BottomEnd).background(PrimaryColor, CircleShape).padding(4.dp).size(16.dp),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "${adherent.prenoms} ${adherent.nom}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                adherent.secteurActivite ?: "Secteur inconnu",
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                Icon(icon, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.SemiBold, color = TextDark)
            }
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextLight, style = MaterialTheme.typography.bodyMedium)
        Text(
            value ?: "N/A",
            color = TextDark,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f).padding(start = 16.dp)
        )
    }
}

@Composable
fun DocumentThumbnail(
    title: String,
    filename: String,
    sessionManager: SessionManager,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        ServerImage(
            filename = filename,
            sessionManager = sessionManager,
            contentDescription = title,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE2E8F0))
                .clickable(onClick = onClick),
            contentScale = ContentScale.Crop
        )
        Text(title, style = MaterialTheme.typography.labelSmall, color = TextLight, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun PersonneChargeItem(
    personne: PersonneChargeDto,
    sessionManager: SessionManager,
    onImageClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ServerImage(
                filename = personne.photo,
                sessionManager = sessionManager,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE2E8F0))
                    .clickable { onImageClick() },
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${personne.prenoms} ${personne.nom}",
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    personne.lienParent ?: "Autre",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextLight
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Rounded.DeleteOutline, null, tint = Color(0xFFEF4444))
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Supprimer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
fun ErrorView(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.CloudOff, null, Modifier.size(64.dp), tint = TextLight)
        Text(error, Modifier.padding(16.dp), textAlign = TextAlign.Center, color = TextLight)
        Button(onClick = onRetry) { Text("Réessayer") }
    }
}

@Composable
fun FullScreenImageViewer(
    imageUrl: String,
    sessionManager: SessionManager,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            ServerImage(
                filename = imageUrl,
                sessionManager = sessionManager,
                contentDescription = "Image Fullscreen",
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                contentScale = ContentScale.Fit
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(0.5f), CircleShape)
            ) {
                Icon(Icons.Rounded.Close, null, tint = Color.White)
            }
        }
    }
}