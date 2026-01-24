package com.example.sencsu.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.* 
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sencsu.R
import com.example.sencsu.components.ImageViewerDialog
import com.example.sencsu.components.ServerImage
import com.example.sencsu.components.modals.AddPersonneChargeModal
import com.example.sencsu.configs.ApiConfig
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.PersonneChargeDto
import com.example.sencsu.data.repository.SessionManager
import com.example.sencsu.domain.viewmodel.AdherentDetailsViewModel
import com.example.sencsu.domain.viewmodel.DetailsUiEvent

private val TextMain = Color(0xFF1F2937)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdherentDetailsScreen(
    viewModel: AdherentDetailsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val sessionManager = viewModel.sessionManager // Récupération du SessionManager via le ViewModel

    var selectedPersonne by remember { mutableStateOf<PersonneChargeDto?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is DetailsUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is DetailsUiEvent.AdherentDeleted -> onNavigateBack()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Détails de l'Adhérent", fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = TextMain)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextMain)
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Ajouter une personne") },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.onAddPersonneClicked()
                                },
                                leadingIcon = { Icon(Icons.Default.Add, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Supprimer l'adhérent", color = Color.Red) },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.confirmDeleteAdherent()
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },

        containerColor = Color(0xFFF5F7FA)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF5D6D7E)
                    )
                }
                state.error != null -> {
                    Text(
                        text = state.error!!,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        color = Color(0xFF95A5A6),
                        fontSize = 14.sp
                    )
                }
                state.adherent != null -> {
                    AdherentProfile(
                        adherent = state.adherent!!,
                        onPersonneClick = { selectedPersonne = it },
                        sessionManager = sessionManager,
                        viewModel = viewModel // Passage du ViewModel
                    )
                }
            }
        }
    }

    if (state.showAddPersonneModal) {
        AddPersonneChargeModal(
            personne = state.newPersonne,
            onPersonneChange = viewModel::onNewPersonneChange,
            onSave = viewModel::onSaveNewPersonne,
            onDismiss = viewModel::onDismissAddPersonneModal
        )
    }

    selectedPersonne?.let { personne ->
        PersonneChargeModal(
            personne = personne,
            onDismiss = { selectedPersonne = null },
            sessionManager = sessionManager // Passage du SessionManager
        )
    }

    // Affichage du dialogue d'aperçu d'image
    if (state.showImageViewerDialog && state.imageToViewUrl != null) {
        ImageViewerDialog(
            imageUrl = state.imageToViewUrl,
            sessionManager = sessionManager,
            onDismiss = { viewModel.closeImagePreview() },
            onDownloadClick = { url -> viewModel.downloadImage(url) }
        )
    }
}

@Composable
fun AdherentProfile(
    adherent: AdherentDto,
    onPersonneClick: (PersonneChargeDto) -> Unit,
    sessionManager: SessionManager, // Ajout de SessionManager en paramètre
    viewModel: AdherentDetailsViewModel // Ajout du ViewModel
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Carte de profil (Photo principale) ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    ServerImage(
                        filename = adherent.photo,
                        sessionManager = sessionManager,
                        contentDescription = "Photo de profil",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFECF0F1)),
                        contentScale = ContentScale.Crop,
                        onClick = { adherent.photo?.let { viewModel.openImagePreview(it) } }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "${adherent.prenoms} ${adherent.nom}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2C3E50)
                    )

                    adherent.secteurActivite?.let {
                        Text(
                            text = it,
                            fontSize = 14.sp,
                            color = Color(0xFF7F8C8D),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // --- 2. Carte des informations personnelles ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Informations Personnelles",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2C3E50),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    InfoRow("Numéro CNI", adherent.numeroCNi)
                    InfoRow("Date de Naissance", adherent.dateNaissance)
                    InfoRow("Lieu de Naissance", adherent.lieuNaissance)
                    InfoRow("Adresse", adherent.adresse)
                    InfoRow("WhatsApp", adherent.whatsapp)
                }
            }
        }

        // --- 3. Carte des pièces d'identité (Recto/Verso) ---
        if (adherent.photoRecto != null || adherent.photoVerso != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Pièces d'identité",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2C3E50),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            adherent.photoRecto?.let { filename ->
                                ImagePreviewCard(
                                    title = "Recto",
                                    filename = filename,
                                    sessionManager = sessionManager,
                                    viewModel = viewModel,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            adherent.photoVerso?.let { filename ->
                                ImagePreviewCard(
                                    title = "Verso",
                                    filename = filename,
                                    sessionManager = sessionManager,
                                    viewModel = viewModel,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }


        // --- 4. Section Personnes à Charge ---
        if (adherent.personnesCharge.isNotEmpty()) {
            item {
                Text(
                    text = "Personnes à Charge (${adherent.personnesCharge.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2C3E50),
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

            items(adherent.personnesCharge) { personne ->
                PersonneChargeCard(
                    personne = personne,
                    onClick = { onPersonneClick(personne) },
                    sessionManager = sessionManager,
                    viewModel = viewModel // Passage du ViewModel
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ImagePreviewCard(
    title: String,
    filename: String,
    sessionManager: SessionManager,
    viewModel: AdherentDetailsViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ServerImage(
            filename = filename,
            sessionManager = sessionManager,
            contentDescription = title,
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFECF0F1)),
            contentScale = ContentScale.Crop,
            onClick = { filename.let { viewModel.openImagePreview(it) } } // Clic pour ouvrir l'aperçu
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF7F8C8D)
        )
    }
}

@Composable
fun InfoRow(label: String, value: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF7F8C8D),
            modifier = Modifier.width(140.dp)
        )
        Text(
            text = value ?: "Non renseigné",
            fontSize = 13.sp,
            color = Color(0xFF2C3E50),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PersonneChargeCard(
    personne: PersonneChargeDto,
    onClick: () -> Unit,
    sessionManager: SessionManager, // Ajout de SessionManager en paramètre
    viewModel: AdherentDetailsViewModel // Ajout du ViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ServerImage(
                filename = personne.photo,
                sessionManager = sessionManager,
                contentDescription = "Photo ${personne.prenoms}",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFECF0F1)),
                contentScale = ContentScale.Crop,
                onClick = { personne.photo?.let { viewModel.openImagePreview(it) } } // Clic pour ouvrir l'aperçu
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${personne.prenoms} ${personne.nom}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2C3E50)
                )
                Text(
                    text = personne.lienParent ?: "Non renseigné",
                    fontSize = 13.sp,
                    color = Color(0xFF7F8C8D),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color(0xFFBDC3C7),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun PersonneChargeModal(
    personne: PersonneChargeDto,
    onDismiss: () -> Unit,
    sessionManager: SessionManager // Ajout de SessionManager en paramètre
) {
    // NOTE: Le modal PersonneChargeModal est une composition interne, il n'a pas accès direct au ViewModel.
    // L'agrandissement d'image devra être géré par l'écran parent via des callbacks si c'est nécessaire.
    // Pour l'instant, nous laissons le composant tel quel, mais la logique d'aperçu d'image au clic
    // doit être gérée dans l'écran principal (AdherentDetailsScreen).
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8F9FA))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Informations Détaillées",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2C3E50),
                        modifier = Modifier.align(Alignment.Center)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = Color(0xFF7F8C8D)
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Nous allons laisser le ServerImage ici sans onClick direct, car il n'a pas de ViewModel pour appeler openImagePreview
                    ServerImage(
                        filename = personne.photo,
                        sessionManager = sessionManager,
                        contentDescription = "Photo ${personne.prenoms}",
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFECF0F1)),
                        contentScale = ContentScale.Crop,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "${personne.prenoms} ${personne.nom}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2C3E50)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Divider(color = Color(0xFFECF0F1))

                    Spacer(modifier = Modifier.height(16.dp))

                    InfoRow("Prénom(s)", personne.prenoms)
                    InfoRow("Nom", personne.nom)
                    InfoRow("Lien de parenté", personne.lienParent)
                    InfoRow("Date de naissance", personne.dateNaissance)
                    personne.lieuNaissance?.let {
                        InfoRow("Lieu de naissance", it)
                    }
                    personne.numeroCNi?.let {
                        InfoRow("Numéro CNI", it)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}