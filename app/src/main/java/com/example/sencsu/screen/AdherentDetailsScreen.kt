package com.example.sencsu.screen

import android.R
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sencsu.components.ServerImage
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.CotisationDto
import com.example.sencsu.data.remote.dto.PaiementDto
import com.example.sencsu.data.remote.dto.PersonneChargeDto
import com.example.sencsu.data.repository.SessionManager
import com.example.sencsu.domain.viewmodel.AdherentDetailsViewModel
import com.example.sencsu.domain.viewmodel.DetailsUiEvent

// 🎨 Palette de couleurs rafraîchie
private val AppBackground = Color(0xFFFBFDFF)
private val BrandBlue = Color(0xFF0052CC)
private val StatusGreen = Color(0xFF34D399)
private val StatusOrange = Color(0xFFFB923C)
private val BorderColor = Color(0xFFE2E8F0)
private val TextMain = Color(0xFF0F172A)
private val TextSub = Color(0xFF64748B)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdherentDetailsScreen(
    viewModel: AdherentDetailsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
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
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Dossier Patient", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("ID: ${state.adherent?.numeroCNi ?: "..."}", style = MaterialTheme.typography.labelSmall, color = TextSub)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, tint = TextMain)
                    }
                },
                actions = {
                    IconButton(
                        enabled = !state.isLoading,
                        onClick = { viewModel.showDeleteAdherentConfirmation() }
                    ) {
                        Icon(
                            Icons.Rounded.DeleteOutline,
                            contentDescription = "Supprimer",
                            tint = Color.Red.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AppBackground)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.onAddPersonneClicked() },
                containerColor = BrandBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Rounded.Add, null) },
                text = { Text("Bénéficiaire", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->

        // --- Dialogues de confirmation (Hors du flux TopAppBar) ---
        if (state.showDeleteAdherentDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelDeleteAdherent() },
                title = { Text("Confirmation") },
                text = { Text("Voulez-vous vraiment supprimer cet adhérent ?") },
                confirmButton = {
                    TextButton(onClick = { viewModel.confirmDeleteAdherent() }) {
                        Text("Supprimer", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelDeleteAdherent() }) {
                        Text("Annuler")
                    }
                }
            )
        }

        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.padding(padding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                state.adherent?.let { adherent ->
                    item { ProfileSection(adherent, viewModel.sessionManager, viewModel) }
                    item { AdherentInfoCard(adherent) }
                    item { DocumentsSection(adherent, viewModel.sessionManager, viewModel) }
                    item { FinanceCard(adherent, state.paiements) }
                    item { CotisationValidityCard(state.cotisations) }
                    item { BeneficiariesList(adherent, viewModel.sessionManager, viewModel) }
                }
            }
        }
    }
}

@Composable
fun ProfileSection(adherent: AdherentDto, sessionManager: SessionManager, viewModel: AdherentDetailsViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            ServerImage(
                filename = adherent.photo,
                sessionManager = sessionManager,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(BrandBlue.copy(0.1f))
                    .clickable { viewModel.openImagePreview(adherent.photo) },
                contentScale = ContentScale.Crop
            )
            Surface(
                color = BrandBlue,
                shape = CircleShape,
                border = BorderStroke(2.dp, Color.White),
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.padding(6.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("${adherent.prenoms} ${adherent.nom}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = TextMain)
        Text(adherent.secteurActivite?.uppercase() ?: "PARTICULIER", style = MaterialTheme.typography.labelLarge, color = BrandBlue, letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun QuickInfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.padding(12.dp))
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSub, modifier = Modifier.padding(top = 4.dp))
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FinanceCard(adherent: AdherentDto, paiements: List<PaiementDto>) {
    // 1. Calculs dynamiques
    val montantTotalAttendu = adherent.montantTotal ?: 0.0
    val totalPaye = paiements.sumOf { it.montant }
    val resteAPayer = (montantTotalAttendu - totalPaye).coerceAtLeast(0.0)

    // Calcul de la progression (ex: 0.7f pour 70%)
    val progress = if (montantTotalAttendu > 0) (totalPaye / montantTotalAttendu).toFloat() else 0f

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("État des Cotisations", fontWeight = FontWeight.Bold, color = TextMain)
                Spacer(modifier = Modifier.weight(1f))
                StatusBadge(progress)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Barre de progression
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                color = if (progress >= 1f) StatusGreen else BrandBlue,
                trackColor = AppBackground
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Statistiques principales
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                FinanceStat("Total Attendu", "${montantTotalAttendu.toInt()} F", color = TextSub)
                FinanceStat("Payé", "${totalPaye.toInt()} F", color = BrandBlue)
                FinanceStat("Restant", "${resteAPayer.toInt()} F", color = if(resteAPayer > 0) StatusOrange else StatusGreen)
            }

            // 2. Affichage de l'historique des paiements (si existants)
            if (paiements.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Derniers versements",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSub,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // On affiche les 3 derniers paiements par exemple
                paiements.take(3).forEach { paiement ->
                    MiniPaiementRow(paiement)
                }
            }
        }
    }
}

@Composable
fun MiniPaiementRow(paiement: PaiementDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icône selon le mode de paiement
        Surface(
            shape = CircleShape,
            color = AppBackground,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (paiement.modePaiement.contains("Orange", true)) Icons.Rounded.PhoneAndroid else Icons.Rounded.AccountBalanceWallet,
                contentDescription = null,
                modifier = Modifier.padding(8.dp).size(16.dp),
                tint = TextSub
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(paiement.reference, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Text(paiement.modePaiement, style = MaterialTheme.typography.labelSmall, color = TextSub)
        }

        Text(
            "+ ${paiement.montant.toInt()} F",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.ExtraBold,
            color = StatusGreen
        )
    }
}

@Composable
fun StatusBadge(progress: Float) {
    val (text, color) = when {
        progress >= 1f -> "À jour" to StatusGreen
        progress > 0.5f -> "Partiel" to BrandBlue
        else -> "En retard" to StatusOrange
    }

    Surface(
        color = color.copy(0.1f),
        shape = CircleShape
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
@Composable
fun FinanceStat(label: String, value: String, color: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
        Text(value, fontWeight = FontWeight.ExtraBold, color = TextMain, fontSize = 16.sp)
    }
}

@Composable
fun DocumentsSection(adherent: AdherentDto, sessionManager: SessionManager, viewModel: AdherentDetailsViewModel) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text("Documents officiels", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DocCard("CNI Recto", adherent.photoRecto, Modifier.weight(1f), sessionManager) {
                viewModel.openImagePreview(adherent.photoRecto)
            }
            DocCard("CNI Verso", adherent.photoVerso, Modifier.weight(1f), sessionManager) {
                viewModel.openImagePreview(adherent.photoVerso)
            }
        }
    }
}

@Composable
fun DocCard(label: String, url: String?, modifier: Modifier, sessionManager: SessionManager, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderColor),
        onClick = onClick
    ) {
        Box {
            ServerImage(
                filename = url,
                sessionManager = sessionManager,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.6f)))
                )
            )
            Text(
                label,
                color = Color.White,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
    fun BeneficiariesList(adherent: AdherentDto, sessionManager: SessionManager, viewModel: AdherentDetailsViewModel) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Bénéficiaires", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Surface(shape = CircleShape, color = BorderColor) {
                Text(
                    adherent.personnesCharge.size.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (adherent.personnesCharge.isEmpty()) {
            EmptyStacte()
        } else {
            adherent.personnesCharge.forEach { personne ->
                BeneficiaryItem(personne, sessionManager) {
                    // Action au clic
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun BeneficiaryItem(personne: PersonneChargeDto, sessionManager: SessionManager, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, BorderColor),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ServerImage(
                filename = personne.photo,
                sessionManager = sessionManager,
                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text("${personne.prenoms} ${personne.nom}", fontWeight = FontWeight.Bold, color = TextMain)
                Text(personne.lienParent ?: "Bénéficiaire", style = MaterialTheme.typography.labelSmall, color = TextSub)
            }

            Icon(Icons.Rounded.ChevronRight, null, tint = BorderColor)
        }
    }
}

@Composable
private fun EmptyStacte() { // Ajout de private ici
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.PeopleOutline, null, modifier = Modifier.size(48.dp), tint = BorderColor)
        Text("Aucun bénéficiaire", color = TextSub, fontSize = 14.sp)
    }
}
@Composable
fun AdherentInfoCard(adherent: AdherentDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Informations Personnelles",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = TextMain
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Grille d'informations
            InfoGridItem(Icons.Rounded.Fingerprint, "Numéro CNI", adherent.numeroCNi)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = AppBackground)

            InfoGridItem(Icons.Rounded.Cake, "Date de naissance", adherent.dateNaissance)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = AppBackground)

            InfoGridItem(Icons.Rounded.Badge, "Secteur", adherent.secteurActivite ?: "Non défini")
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = AppBackground)

            InfoGridItem(Icons.Rounded.LocationOn, "Adresse", adherent.adresse)
        }
    }
}

@Composable
private fun InfoGridItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = BrandBlue.copy(alpha = 0.08f),
            modifier = Modifier.size(36.dp)
        ) {
            Icon(icon, null, tint = BrandBlue, modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSub)
            Text(value ?: "N/A", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextMain)
        }
    }
}
@Composable
fun PersonneDetailsModalModern(
    personne: PersonneChargeDto,
    sessionManager: SessionManager,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header de la modale
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd).background(AppBackground, CircleShape).size(32.dp)
                    ) {
                        Icon(Icons.Rounded.Close, null, modifier = Modifier.size(16.dp))
                    }
                }

                // Photo et Nom
                ServerImage(
                    filename = personne.photo,
                    sessionManager = sessionManager,
                    modifier = Modifier.size(100.dp).clip(RoundedCornerShape(28.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("${personne.prenoms} ${personne.nom}", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)

                Surface(
                    color = StatusOrange.copy(0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        personne.lienParent?.uppercase() ?: "BÉNÉFICIAIRE",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusOrange,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Détails en liste compacte
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    DetailRow(Icons.Rounded.Wc, "Genre", personne.sexe)
                    DetailRow(Icons.Rounded.Event, "Date de Naissance", personne.dateNaissance)
                    DetailRow(Icons.Rounded.Place, "Lieu de Naissance", personne.lieuNaissance)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Fermer la fiche", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppBackground, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TextSub, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = TextSub)
        Text(value ?: "N/A", fontWeight = FontWeight.Bold, color = TextMain)
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CotisationValidityCard(cotisations: List<CotisationDto>) {
    // On prend la cotisation la plus récente (la première de la liste si triée par date)
    val activeCotisation = cotisations.firstOrNull() ?: return

    val (progress, daysRemaining) = remember(activeCotisation) {
        try {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val start = java.time.LocalDate.parse(activeCotisation.dateDebut, formatter)
            val end = java.time.LocalDate.parse(activeCotisation.dateFin, formatter)
            val now = java.time.LocalDate.now()

            val totalDays = java.time.temporal.ChronoUnit.DAYS.between(start, end).toFloat()
            val elapsedDays = java.time.temporal.ChronoUnit.DAYS.between(start, now).toFloat()
            val remaining = java.time.temporal.ChronoUnit.DAYS.between(now, end)

            (elapsedDays / totalDays).coerceIn(0f, 1f) to remaining
        } catch (e: Exception) {
            0f to 0L
        }
    }

    // Couleur dynamique : Orange si expiration proche (moins de 30 jours)
    val progressColor = if (daysRemaining < 30) StatusOrange else StatusGreen

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.History, null, tint = BrandBlue, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Validité de l'Adhésion", fontWeight = FontWeight.Bold, color = TextMain)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Barre de progression du temps
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
                color = progressColor,
                trackColor = AppBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dates et Jours restants
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DateInfoColumn("Début", activeCotisation.dateDebut.toString(), Alignment.Start)

                // Badge jours restants au centre
                Surface(
                    color = progressColor.copy(0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "$daysRemaining jours restants",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = progressColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                DateInfoColumn("Échéance", activeCotisation.dateFin.toString(), Alignment.End)
            }
        }
    }
}

@Composable
fun DateInfoColumn(label: String, date: String, alignment: Alignment.Horizontal) {
    Column(horizontalAlignment = alignment) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSub)
        Text(
            date, // Tu peux utiliser ta fonction .formatToFrench() ici
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = TextMain
        )
    }
}
