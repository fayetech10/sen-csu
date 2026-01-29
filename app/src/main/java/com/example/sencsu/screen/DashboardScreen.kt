package com.example.sencsu.screen

import AdherentChartCard
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sencsu.components.RecentActivitiesSection
import com.example.sencsu.data.remote.dto.AgentDto
import com.example.sencsu.data.remote.dto.FormConstants
import com.example.sencsu.data.repository.SessionManager
import com.example.sencsu.domain.viewmodel.DashboardViewModel

// --- COULEURS ET STYLE ---
private val GradientPrimary = Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF3B82F6)))
private val SurfaceLight = Color(0xFFF8FAFC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val dashboardState by viewModel.dashboardState.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // Redirection si déconnexion
    LaunchedEffect(authState.user) {
        if (authState.user == null && !authState.isLoading) {
            navController.navigate("login") { popUpTo(0) { inclusive = true } }
        }
    }

// Option A : Rafraîchir quand on arrive sur l'écran
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = SurfaceLight,
        topBar = {
            DashboardTopBar(
                agent = authState.user,
                scrollBehavior = scrollBehavior,
                onProfileClick = { navController.navigate("profile") }
            )
        },
        bottomBar = { ModernBottomNav(navController = navController) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("add_adherent") },
                containerColor = FormConstants.Colors.primary,
                contentColor = Color.White,
                shape = CircleShape,
                icon = { Icon(Icons.Rounded.Add, "Ajouter") },
                text = { Text("Nouvel Adhérent") }
            )
        }
    ) { padding ->
        // Utilisation du composant officiel Material 3 pour le refresh
        PullToRefreshBox(
            isRefreshing = dashboardState.isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Crossfade(targetState = dashboardState, label = "dashboard_fade") { state ->
                when {
                    state.isLoading && state.data == null -> LoadingDashboardSkeleton()
                    state.error != null -> ErrorState(state.error!!) { viewModel.refresh() }
                    state.data != null -> {

                        DashboardContent(
                            data = state.data!!,
                            navController = navController,
                            sessionManager = viewModel.sessionManager
                        )
                    }
                    else -> EmptyState()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(
    agent: AgentDto?,
    scrollBehavior: TopAppBarScrollBehavior,
    onProfileClick: () -> Unit
) {
    LargeTopAppBar(
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = SurfaceLight,
            scrolledContainerColor = Color.White
        ),
        title = {
            Column {
                Text("Tableau de bord", style = MaterialTheme.typography.titleSmall, color = Color.Gray)
                Text(
                    text = if (agent != null) "Salut, ${agent.prenom} 👋" else "Chargement...",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        actions = {
            IconButton(onClick = { /* Notifs */ }) {
                Icon(Icons.Rounded.NotificationsNone, null)
            }
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(FormConstants.Colors.primary.copy(alpha = 0.1f))
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    agent?.prenom?.take(1) ?: "?",
                    color = FormConstants.Colors.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
private fun DashboardContent(
    data: com.example.sencsu.data.remote.dto.DashboardResponseDto,
    navController: NavController,
    sessionManager: SessionManager
) {
    val adherents = data.data
    val totalAmount = adherents.sumOf { it.montantTotal!!}
    val totalDeps = adherents.sumOf { it.personnesCharge.size }

    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { QuickSearchBar(onClick = { navController.navigate("search") }) }

        item {
            BentoStatsGrid(
                totalMembers = adherents.size,
                totalAmount = totalAmount,
                totalDependents = totalDeps
            )
        }

        item { QuickActionsRow(navController) }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Vue d'ensemble", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    AdherentChartCard(adherents = adherents)
                }
            }
        }

        item {
            RecentActivitiesSection(
                adherents = adherents.take(5),
                onAdherentClick = { id -> navController.navigate("adherent_details/$id") },
                onSeeAllClick = { navController.navigate("liste_adherents") },
                sessionManager = sessionManager
            )
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun BentoStatsGrid(totalMembers: Int, totalAmount: Double, totalDependents: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Card Principale (Recettes) avec Correction du Brush
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)) // On clip avant le background
                .background(GradientPrimary),    // Le dégradé se met ici
            color = Color.Transparent            // On rend la Surface transparente pour voir le dégradé
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Encaissé", color = Color.White.copy(0.8f), fontSize = 14.sp)
                    Text(
                        String.format("Total : %.0f FCFA", totalAmount),
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Icon(
                    Icons.Rounded.Payments,
                    null,
                    tint = Color.White.copy(0.3f),
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatMiniCard(
                modifier = Modifier.weight(1f),
                label = "Adhérents",
                value = totalMembers.toString(),
                icon = Icons.Rounded.People,
                color = Color(0xFF10B981)
            )
            StatMiniCard(
                modifier = Modifier.weight(1f),
                label = "Bénéficiaires",
                value = totalDependents.toString(),
                icon = Icons.Rounded.FamilyRestroom,
                color = Color(0xFF6366F1)
            )
        }
    }
}
@Composable
fun StatMiniCard(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun QuickSearchBar(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(56.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Search, null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Rechercher un dossier...", color = Color.Gray)
        }
    }
}

@Composable
fun QuickActionsRow(navController: NavController) {
    val items = listOf(
        Triple("Stats", Icons.Rounded.BarChart, Color(0xFFF59E0B)),
        Triple("Cartes", Icons.Rounded.ContactPage, Color(0xFF3B82F6)),
        Triple("Aide", Icons.Rounded.HelpOutline, Color(0xFF64748B))
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEach { (label, icon, color) ->
            Column(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                    .background(color.copy(0.1f)).clickable { }.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(icon, null, tint = color)
                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
            }
        }
    }
}

@Composable
fun ModernBottomNav(navController: NavController) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Rounded.GridView, null) },
            label = { Text("Accueil") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("liste_adherents") },
            icon = { Icon(Icons.Rounded.FormatListBulleted, null) },
            label = { Text("Liste") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("profile") },
            icon = { Icon(Icons.Rounded.PersonOutline, null) },
            label = { Text("Profil") }
        )
    }
}

@Composable
fun LoadingDashboardSkeleton() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(24.dp)).background(Color.LightGray.copy(0.2f)))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.weight(1f).height(100.dp).clip(RoundedCornerShape(20.dp)).background(Color.LightGray.copy(0.2f)))
            Box(Modifier.weight(1f).height(100.dp).clip(RoundedCornerShape(20.dp)).background(Color.LightGray.copy(0.2f)))
        }
    }
}

@Composable
fun ErrorState(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(error, textAlign = TextAlign.Center, color = Color.Gray)
        Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
            Text("Réessayer")
        }
    }
}

@Composable
fun EmptyState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Aucune donnée disponible", color = Color.Gray)
    }
}