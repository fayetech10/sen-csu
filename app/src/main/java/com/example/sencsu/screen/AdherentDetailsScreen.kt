package com.example.sencsu.screen

import HealthInsuranceCard
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sencsu.components.ServerImage
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.CotisationDto
import com.example.sencsu.data.remote.dto.PaiementDto
import com.example.sencsu.data.remote.dto.PersonneChargeDto
import com.example.sencsu.data.repository.SessionManager
import com.example.sencsu.domain.viewmodel.AdherentDetailsViewModel
import com.example.sencsu.domain.viewmodel.DetailsUiEvent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.healthcard.ui.components.HealthInsuranceCardWithFlip
import com.example.sencsu.components.cartes.CarteSanteUniverselle
import com.example.sencsu.configs.ApiConfig
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


// 🎨 Nouvelle palette de couleurs moderne
private val AppBackground = Color(0xFFF8FAFC)
private val SurfaceColor = Color(0xFFFFFFFF)
private val BrandPrimary = Color(0xFF4F46E5)
private val BrandSecondary = Color(0xFF8B5CF6)
private val SuccessColor = Color(0xFF10B981)
private val WarningColor = Color(0xFFF59E0B)
private val ErrorColor = Color(0xFFEF4444)
private val InfoColor = Color(0xFF3B82F6)
private val BorderLight = Color(0xFFE2E8F0)
private val TextPrimary = Color(0xFF1E293B)
private val TextSecondary = Color(0xFF64748B)
private val TextTertiary = Color(0xFF94A3B8)

// Gradient moderne
private val GradientPrimary = listOf(BrandPrimary, BrandSecondary)
private val GradientSuccess = listOf(SuccessColor, Color(0xFF34D399))
private val GradientWarning = listOf(WarningColor, Color(0xFFFBBF24))

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdherentDetailsScreen(
    viewModel: AdherentDetailsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,

) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // États pour les modales
    var showPersonneDetailsModal by remember { mutableStateOf<PersonneChargeDto?>(null) }
    var showAddPersonneModal by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is DetailsUiEvent.AdherentDeleted -> onNavigateBack()
                is DetailsUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        containerColor = AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ModernTopBar(
                adherent = state.adherent,
                onBackClick = onNavigateBack,
                onDeleteClick = { viewModel.showDeleteAdherentConfirmation() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddPersonneModal = true },
                containerColor = BrandPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Rounded.PersonAdd, contentDescription = "Ajouter bénéficiaire")
            }
        }
    ) { padding ->

        // Dialogues de confirmation
        if (state.showDeleteAdherentDialog) {
            ConfirmationDialog(
                title = "Supprimer l'adhérent",
                message = "Cette action est irréversible. Voulez-vous continuer ?",
                confirmText = "Supprimer",
                onConfirm = { viewModel.confirmDeleteAdherent() },
                onDismiss = { viewModel.cancelDeleteAdherent() }
            )
        }

        state.personToDelete?.let { personne ->
            ConfirmationDialog(
                title = "Supprimer le bénéficiaire",
                message = "Supprimer ${personne.prenoms} ${personne.nom} ?",
                onConfirm = { viewModel.confirmDeletePersonne() },
                onDismiss = { viewModel.cancelDeletePersonne() }
            )
        }

        // Modal de détails personne
        showPersonneDetailsModal?.let { personne ->
            PersonneDetailsModal(
                personne = personne,
                sessionManager = viewModel.sessionManager,
                onEdit = { /* TODO: Implémenter l'édition */ },
                onDelete = {
                    showPersonneDetailsModal = null
                    viewModel.showDeletePersonneConfirmation(personne)
                },
                onDismiss = { showPersonneDetailsModal = null }
            )
        }

        // Modal d'ajout personne
        if (showAddPersonneModal) {
            AddPersonneModal(
                onSave = { newPersonne ->
                    viewModel.onSaveNewPersonne()
                    showAddPersonneModal = false
                },
                onDismiss = { showAddPersonneModal = false }
            )
        }
        // Preview image
        state.selectedImageUrl?.let { url ->
            ImagePreviewDialog(
                imageUrl = url,
                onDismiss = { viewModel.closeImagePreview() },
                sessionManager = viewModel.sessionManager
            )
        }

        // Contenu principal
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.padding(padding)
        ) {
            when {
                state.error != null -> ErrorStates(message = state.error ?: "Erreur", onRetry = { viewModel.refresh() })
                state.adherent == null -> LoadingState()
                else -> AdherentContent(
                    adherent = state.adherent!!,
                    paiements = state.paiements,
                    cotisations = state.cotisations,
                    sessionManager = viewModel.sessionManager,
                    viewModel = viewModel,
                    onPersonneClick = { showPersonneDetailsModal = it }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernTopBar(
    adherent: AdherentDto?,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    "Dossier Patient",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    adherent?.id.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Retour")
            }
        },
        actions = {
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Outlined.Delete, contentDescription = "Supprimer", tint = ErrorColor)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SurfaceColor,
            scrolledContainerColor = SurfaceColor
        )
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun AdherentContent(
    adherent: AdherentDto,
    paiements: List<PaiementDto>,
    cotisations: List<CotisationDto>,
    sessionManager: SessionManager,
    viewModel: AdherentDetailsViewModel,
    onPersonneClick: (PersonneChargeDto) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { ProfileHeader(adherent, sessionManager, viewModel) }
//        item { HealthInsuranceCardWithFlip(adherent) }
        item {
            HealthInsuranceCard(
                data = adherent,
                sessionManager = sessionManager)
            }
        item { QuickStatsRow(adherent, paiements) }
        item { PersonalInfoCard(adherent) }
        item { DocumentsGrid(adherent, sessionManager, viewModel) }
        item { PaymentStatusCard(adherent, paiements) }
        item { CotisationTimelineCard(cotisations) }
        item { BeneficiariesSection(adherent, sessionManager, onPersonneClick) }
    }
}

@Composable
private fun ProfileHeader(
    adherent: AdherentDto,
    sessionManager: SessionManager,
    viewModel: AdherentDetailsViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, BorderLight),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Photo de profil avec badge de statut
            Box(
                contentAlignment = Alignment.BottomEnd
            ) {
                ServerImage(
                    filename = adherent.photo,
                    sessionManager = sessionManager,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .clip(CircleShape)
                        .background(BrandPrimary.copy(alpha = 0.1f))
                        .clickable { viewModel.openImagePreview(adherent.photo) },
                    contentScale = ContentScale.Crop
                )


                // Badge de statut
                Surface(
                    color = SuccessColor,
                    shape = CircleShape,
                    border = BorderStroke(2.dp, SurfaceColor),
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = "Actif",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nom et secteur
            Text(
                "${adherent.prenoms?.uppercase()} ${adherent.nom?.uppercase()}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Badge(
                containerColor = BrandPrimary.copy(alpha = 0.1f),
                contentColor = BrandPrimary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    adherent.secteurActivite?.uppercase() ?: "PARTICULIER",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Boutons d'action rapide
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton(
                    icon = Icons.Outlined.Phone,
                    label = "Appeler",
                    color = SuccessColor,
                    onClick = { /* TODO: Appeler */ },
                    adherent = adherent
                )
                ActionButton(
                    icon = Icons.Outlined.Message,
                    label = "SMS",
                    color = InfoColor,
                    onClick = { /* TODO: Envoyer SMS */ },
                    adherent = adherent
                )
                ActionButton(
                    icon = Icons.Outlined.Email,
                    label = "Email",
                    color = WarningColor,
                    onClick = { /* TODO: Envoyer email */ },
                    adherent = adherent
                )
                ActionButton(
                    icon = Icons.Outlined.Whatsapp,
                    label = "WhatsApp",
                    color = SuccessColor,
                    onClick = { /* TODO: Envoyer email */ },
                    adherent = adherent
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    adherent: AdherentDto,
    onClick: (() -> Unit)? = null // optionnel si tu veux override
) {
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable {
            if (label == "WhatsApp") {
                // Vérifie si le numéro existe
                val phone = adherent.whatsapp?.filter { it.isDigit() } ?: return@clickable
                val uri = Uri.parse("https://wa.me/$phone")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
            } else {
                onClick?.invoke()
            }
        }
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}


@Composable
private fun QuickStatsRow(
    adherent: AdherentDto,
    paiements: List<PaiementDto>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Cotisation",
            value = "${adherent.montantTotal?.toInt() ?: 0} F",
            icon = Icons.Rounded.AttachMoney,
            color = BrandPrimary,
            modifier = Modifier.weight(1f)
        )

        StatCard(
            title = "Payé",
            value = "${paiements.sumOf { it.montant }.toInt()} F",
            icon = Icons.Rounded.CheckCircle,
            color = SuccessColor,
            modifier = Modifier.weight(1f)
        )

        StatCard(
            title = "Bénéficiaires",
            value = adherent.personnesCharge.size.toString(),
            icon = Icons.Rounded.People,
            color = InfoColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun PersonalInfoCard(adherent: AdherentDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = "Informations",
                    tint = BrandPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Informations personnelles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            InfoRow(
                icon = Icons.Outlined.Fingerprint,
                label = "CNI",
                value = adherent.numeroCNi.toString()
            )
            InfoRow(
                icon = Icons.Outlined.Cake,
                label = "Naissance",
                value = adherent.dateNaissance.toString()
            )
            InfoRow(
                icon = Icons.Outlined.Work,
                label = "Secteur",
                value = adherent.secteurActivite ?: "Non spécifié"
            )
            InfoRow(
                icon = Icons.Outlined.LocationOn,
                label = "Adresse",
                value = adherent.adresse.toString()
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun DocumentsGrid(
    adherent: AdherentDto,
    sessionManager: SessionManager,
    viewModel: AdherentDetailsViewModel
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                Icons.Outlined.Description,
                contentDescription = "Documents",
                tint = BrandPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Documents",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DocumentCard(
                title = "CNI Recto",
                imageUrl = adherent.photoRecto,
                sessionManager = sessionManager,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.openImagePreview(adherent.photoRecto) }
            )
            DocumentCard(
                title = "CNI Verso",
                imageUrl = adherent.photoVerso,
                sessionManager = sessionManager,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.openImagePreview(adherent.photoVerso) }
            )
        }
    }
}

@Composable
private fun DocumentCard(
    title: String,
    imageUrl: String?,
    sessionManager: SessionManager,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderLight),
        onClick = onClick
    ) {
        Box {
            ServerImage(
                filename = imageUrl,
                sessionManager = sessionManager,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Overlay avec titre
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                            startY = 300f
                        )
                    )
            )

            Text(
                title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            )
        }
    }
}

@Composable
@RequiresApi(Build.VERSION_CODES.O)
private fun PaymentStatusCard(adherent: AdherentDto, paiements: List<PaiementDto>) {
    val montantTotal = adherent.montantTotal ?: 0.0
    val totalPaye = paiements.sumOf { it.montant }
    val progress = if (montantTotal > 0) (totalPaye / montantTotal).toFloat() else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Outlined.Payments,
                    contentDescription = "Paiements",
                    tint = BrandPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "État des paiements",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Barre de progression
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = if (progress >= 1f) SuccessColor else BrandPrimary,
                    trackColor = BorderLight
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (progress >= 1f) SuccessColor else BrandPrimary
                    )

                    Text(
                        "${totalPaye.toInt()} / ${montantTotal.toInt()} F",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
            }

            // Derniers paiements
            if (paiements.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Derniers paiements",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                paiements.take(3).forEach { paiement ->
                    PaymentItem(paiement = paiement)
                }
            }
        }
    }
}

@Composable
private fun PaymentItem(paiement: PaiementDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = SuccessColor.copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                Icons.Rounded.Check,
                contentDescription = "Paiement effectué",
                tint = SuccessColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                paiement.reference,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                paiement.modePaiement,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        Text(
            "+${paiement.montant.toInt()} F",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = SuccessColor
        )
    }
}

@Composable
@RequiresApi(Build.VERSION_CODES.O)
private fun CotisationTimelineCard(cotisations: List<CotisationDto>) {
    val activeCotisation = cotisations.firstOrNull() ?: return

    val (progress, daysRemaining) = remember(activeCotisation) {
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val start = LocalDate.parse(activeCotisation.dateDebut, formatter)
            val end = LocalDate.parse(activeCotisation.dateFin, formatter)
            val now = LocalDate.now()

            val totalDays = ChronoUnit.DAYS.between(start, end).toFloat()
            val elapsedDays = ChronoUnit.DAYS.between(start, now).toFloat()
            val remaining = ChronoUnit.DAYS.between(now, end)

            (elapsedDays / totalDays).coerceIn(0f, 1f) to remaining
        } catch (e: Exception) {
            0f to 0L
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Outlined.CalendarToday,
                    contentDescription = "Validité",
                    tint = BrandPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Validité de l'adhésion",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Timeline
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Début",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            activeCotisation.dateDebut.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Échéance",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            activeCotisation.dateFin.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = if (daysRemaining < 30) WarningColor else SuccessColor,
                    trackColor = BorderLight
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${(progress * 100).toInt()}% écoulé",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )

                    Badge(
                        containerColor = if (daysRemaining < 30) WarningColor.copy(alpha = 0.1f) else SuccessColor.copy(alpha = 0.1f),
                        contentColor = if (daysRemaining < 30) WarningColor else SuccessColor
                    ) {
                        Text(
                            "$daysRemaining jours restants",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BeneficiariesSection(
    adherent: AdherentDto,
    sessionManager: SessionManager,
    onPersonneClick: (PersonneChargeDto) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                Icons.Outlined.People,
                contentDescription = "Bénéficiaires",
                tint = BrandPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Bénéficiaires",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            Badge(
                containerColor = BrandPrimary.copy(alpha = 0.1f),
                contentColor = BrandPrimary
            ) {
                Text(adherent.personnesCharge.size.toString())
            }
        }

        if (adherent.personnesCharge.isEmpty()) {
            EmptyStatee()
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                adherent.personnesCharge.forEach { personne ->
                    BeneficiaryCard(
                        personne = personne,
                        sessionManager = sessionManager,
                        onClick = { onPersonneClick(personne) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BeneficiaryCard(
    personne: PersonneChargeDto,
    sessionManager: SessionManager,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val token by sessionManager.tokenFlow.collectAsState(initial = null)

    val imageUrl = ApiConfig.getImageUrl(personne.photo)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderLight),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
//            ServerImage(
//                filename = personne.photo,
//                sessionManager = sessionManager,
//                modifier = Modifier
//                    .size(50.dp)
//                    .clip(RoundedCornerShape(12.dp)),
//                contentScale = ContentScale.Crop
//            )
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .apply {
                        token?.let {
                            addHeader("Authorization", "Bearer $it")
                        }
                    }

                    .crossfade(true)
                    .build(),
                contentDescription = personne.nom,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${personne.prenoms} ${personne.nom}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                personne.lienParent?.let { lien ->
                    Text(
                        lien,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = "Voir détails",
                tint = TextTertiary
            )
        }
    }
}

@Composable
private fun EmptyStatee() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(width = 1.dp, color = BorderLight)

    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.PersonOff,
                contentDescription = "Aucun bénéficiaire",
                tint = TextTertiary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Aucun bénéficiaire",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ErrorStates(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Rounded.ErrorOutline,
            contentDescription = "Erreur",
            tint = ErrorColor,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Une erreur est survenue",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Réessayer")
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun PersonneDetailsModal(
    personne: PersonneChargeDto,
    sessionManager: SessionManager,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val token by sessionManager.tokenFlow.collectAsState(initial = null)

    val imageUrl = ApiConfig.getImageUrl(personne.photo)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // En-tête avec bouton fermer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Détails du bénéficiaire",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Photo et nom
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    ServerImage(
//                        filename = personne.photo,
//                        sessionManager = sessionManager,
//                        modifier = Modifier
//                            .size(120.dp)
//                            .clip(CircleShape),
//                        contentScale = ContentScale.Crop
//                    )
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .apply {
                                token?.let {
                                    addHeader("Authorization", "Bearer $it")
                                }
                            }

                            .crossfade(true)
                            .build(),
                        contentDescription = personne.nom,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "${personne.prenoms} ${personne.nom}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    personne.lienParent?.let { lien ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Badge(
                            containerColor = BrandPrimary.copy(alpha = 0.1f),
                            contentColor = BrandPrimary
                        ) {
                            Text(lien.uppercase())
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Informations
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoItem(
                        icon = Icons.Rounded.Wc,
                        label = "Sexe",
                        value = personne.sexe ?: "Non spécifié"
                    )
                    InfoItem(
                        icon = Icons.Rounded.Cake,
                        label = "Date de naissance",
                        value = personne.dateNaissance ?: "Non spécifié"
                    )
                    InfoItem(
                        icon = Icons.Rounded.Place,
                        label = "Lieu de naissance",
                        value = personne.lieuNaissance ?: "Non spécifié"
                    )
                    InfoItem(
                        icon = Icons.Rounded.Fingerprint,
                        label = "Numéro CNI",
                        value = personne.numeroCNi ?: "Non spécifié"
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Boutons d'action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandPrimary.copy(alpha = 0.1f),
                            contentColor = BrandPrimary
                        )
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Modifier", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Modifier")
                    }

                    Button(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ErrorColor.copy(alpha = 0.1f),
                            contentColor = ErrorColor
                        )
                    ) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Supprimer", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Supprimer")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirmer",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.textButtonColors(contentColor = ErrorColor)
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
private fun ImagePreviewDialog(imageUrl: String?, onDismiss: () -> Unit, sessionManager: SessionManager) {
    imageUrl?.let { url ->
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box {
                    ServerImage(
                        filename = url,
                        sessionManager = sessionManager,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Fit
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Fermer",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddPersonneModal(
    onSave: (PersonneChargeDto) -> Unit,
    onDismiss: () -> Unit
) {
    var newPersonne by remember { mutableStateOf(PersonneChargeDto()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouveau bénéficiaire", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = newPersonne.prenoms.toString(),
                    onValueChange = { newPersonne = newPersonne.copy(prenoms = it) },
                    label = { Text("Prénoms") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = newPersonne.nom.toString(),
                    onValueChange = { newPersonne = newPersonne.copy(nom = it) },
                    label = { Text("Nom") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = newPersonne.lienParent ?: "",
                    onValueChange = { newPersonne = newPersonne.copy(lienParent = it) },
                    label = { Text("Lien de parenté") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(newPersonne) },
                enabled = newPersonne.prenoms?.isNotBlank() == true && newPersonne.nom?.isNotBlank() == true
            ) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}