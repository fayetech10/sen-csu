package com.example.sencsu.screen

import AdherentChartCard
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sencsu.components.RecentActivitiesSection
import com.example.sencsu.data.remote.dto.AgentDto
import com.example.sencsu.data.remote.dto.FormConstants
import com.example.sencsu.data.repository.SessionManager // Import de SessionManager
import com.example.sencsu.domain.viewmodel.DashboardViewModel
import java.util.Locale

private val NeutralDark = Color(0xFF1E293B)
private val NeutralMedium = Color(0xFF64748B)

enum class NavScreen(val icon: ImageVector, val label: String) {
    Home(Icons.Rounded.Home, "Accueil"),
    Search(Icons.Rounded.Search, "Recherche"),
    Stats(Icons.Rounded.PieChart, "Stats"),
    Profile(Icons.Rounded.AccountCircle, "Profil")
}

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val dashboardState by viewModel.dashboardState.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val sessionManager = viewModel.sessionManager // Récupération du SessionManager
    var selectedScreen by remember { mutableStateOf(NavScreen.Home) }

    // Gérer les événements de déconnexion
    LaunchedEffect(Unit) {
        viewModel.authState.collect { state ->
            if (state.user == null && !state.isLoading) {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    Scaffold(
        containerColor = FormConstants.Colors.background,
        bottomBar = {
            ModernBottomNav(
                selectedScreen = selectedScreen,
                onScreenSelected = { screen ->
                    selectedScreen = screen
                    when (screen) {
                        NavScreen.Search -> navController.navigate("search")
                        NavScreen.Stats -> navController.navigate("stats")
                        NavScreen.Profile -> navController.navigate("profile")
                        NavScreen.Home -> { /* Déjà sur l'accueil */ }
                    }
                },
                onAddClick = { navController.navigate("add_adherent") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                dashboardState.isLoading || authState.isLoading -> LoadingState()

                dashboardState.error != null -> ErrorState(
                    error = dashboardState.error!!,
                    onRetry = { viewModel.refresh() }
                )

                authState.error != null -> ErrorState(
                    error = authState.error!!,
                    onRetry = { viewModel.refresh() }
                )

                dashboardState.data != null && authState.user != null -> {
                    DashboardContent(
                        data = dashboardState.data!!,
                        agent = authState.user,
                        navController = navController,
                        sessionManager = sessionManager // Passage du SessionManager
                    )
                }

                else -> EmptyState()
            }
        }
    }
}

@Composable
private fun DashboardContent(
    data: com.example.sencsu.data.remote.dto.DashboardResponseDto,
    agent: AgentDto?,
    navController: NavController,
    sessionManager: SessionManager // Ajout de SessionManager en paramètre
) {
    val adherents = data.adherents ?: emptyList()
    val totalDependents = adherents.sumOf { it.personnesCharge?.size ?: 0 }
    val totalAmount = adherents.sumOf { it.montantTotal ?: 0 }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            HeaderSection(agent)
        }

        item {
            BentoStatsGrid(
                totalMembers = adherents.size,
                totalAmount = totalAmount,
                totalDependents = totalDependents,
                onMembersClick = { navController.navigate("liste_adherents") }
            )
        }

        item {
            RecentActivitiesSection(
                adherents = adherents.take(5),
                onAdherentClick = { id -> navController.navigate("adherent_details/$id") },
                onSeeAllClick = { navController.navigate("liste_adherents") },
                sessionManager = sessionManager // Passage du SessionManager
            )
        }

        item {
            AdherentChartCard(adherents = adherents)
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun HeaderSection(agent: AgentDto?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Content de vous revoir,",
                color = FormConstants.Colors.textGrey,
                fontSize = 14.sp
            )
            Text(
                text = "${agent?.prenoms ?: "Agent"} 👋",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = FormConstants.Colors.textDark
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(FormConstants.Colors.white)
                .clickable { /* TODO: Gérer les notifications */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Notifications,
                contentDescription = "Notifications",
                tint = FormConstants.Colors.textDark
            )
        }
    }
}

@Composable
fun BentoStatsGrid(
    totalMembers: Int,
    totalAmount: Int,
    totalDependents: Int,
    onMembersClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)
    ) {
        StatCard(
            modifier = Modifier.weight(1.5f),
            label = "Total Encaissé",
            value = "${String.format(Locale.FRANCE, "%,d", totalAmount)} FCFA",
            icon = Icons.Rounded.MonetizationOn,
            color = FormConstants.Colors.primary,
            isPrimary = true
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                modifier = Modifier.fillMaxWidth(),
                label = "Membres",
                value = totalMembers.toString(),
                icon = Icons.Rounded.Group,
                color = FormConstants.Colors.primary,
                onClick = onMembersClick
            )
            StatCard(
                modifier = Modifier.fillMaxWidth(),
                label = "En Charge",
                value = totalDependents.toString(),
                icon = Icons.Rounded.SupervisorAccount,
                color = FormConstants.Colors.secondary
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    isPrimary: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val backgroundColor = if (isPrimary) color else FormConstants.Colors.white
    val contentColor = if (isPrimary) FormConstants.Colors.white else FormConstants.Colors.textDark
    val labelColor = if (isPrimary) FormConstants.Colors.white.copy(alpha = 0.7f) else FormConstants.Colors.textGrey

    Surface(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        ),
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor,
        shadowElevation = if (isPrimary) 4.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = label,
                fontSize = 13.sp,
                color = labelColor
            )
        }
    }
}

@Composable
fun ModernBottomNav(
    selectedScreen: NavScreen,
    onScreenSelected: (NavScreen) -> Unit,
    onAddClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(vertical = 10.dp)
            .fillMaxWidth(),
        shape = RectangleShape,
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavIconItem(
                screen = NavScreen.Home,
                isSelected = selectedScreen == NavScreen.Home,
                onClick = { onScreenSelected(NavScreen.Home) }
            )
            NavIconItem(
                screen = NavScreen.Search,
                isSelected = selectedScreen == NavScreen.Search,
                onClick = { onScreenSelected(NavScreen.Search) }
            )

            Surface(
                onClick = onAddClick,
                modifier = Modifier.size(50.dp),
                shape = CircleShape,
                color = NeutralDark,
                contentColor = Color.White,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Ajouter un adhérent",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            NavIconItem(
                screen = NavScreen.Stats,
                isSelected = selectedScreen == NavScreen.Stats,
                onClick = { onScreenSelected(NavScreen.Stats) }
            )
            NavIconItem(
                screen = NavScreen.Profile,
                isSelected = selectedScreen == NavScreen.Profile,
                onClick = { onScreenSelected(NavScreen.Profile) }
            )
        }
    }
}

@Composable
private fun NavIconItem(
    screen: NavScreen,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color by animateColorAsState(
        targetValue = if (isSelected) FormConstants.Colors.primary else FormConstants.Colors.textGrey,
        label = "nav_color"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        label = "nav_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Icon(
            imageVector = screen.icon,
            contentDescription = screen.label,
            tint = color,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
        )
    }
}

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = FormConstants.Colors.primary,
                strokeWidth = 3.dp
            )
            Text(
                text = "Chargement...",
                color = FormConstants.Colors.textGrey,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ErrorState(error: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = "Erreur",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "Une erreur s'est produite",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FormConstants.Colors.textDark
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = FormConstants.Colors.textGrey,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = FormConstants.Colors.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Réessayer")
            }
        }
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.FolderOff,
                contentDescription = "Vide",
                tint = FormConstants.Colors.textGrey,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "Aucune donnée disponible",
                style = MaterialTheme.typography.titleMedium,
                color = FormConstants.Colors.textDark
            )
        }
    }
}