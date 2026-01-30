package com.example.sencsu.screen

import AdherentChartCard
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sencsu.components.RecentActivitiesSection
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.AgentDto
import com.example.sencsu.data.remote.dto.FormConstants
import com.example.sencsu.data.repository.SessionManager
import com.example.sencsu.domain.viewmodel.DashboardViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter



// --- DESIGN SYSTEM ---
private object DesignSystem {
    object Colors {
        val Background = Color(0xFFFAFAFA)
        val Surface = Color.White
        val SurfaceVariant = Color(0xFFF5F5F7)

        val Primary = Color(0xFF0A84FF)
        val PrimaryLight = Color(0xFFE3F2FF)

        val Success = Color(0xFF34C759)
        val SuccessLight = Color(0xFFE8F8EC)

        val Warning = Color(0xFFFF9500)
        val WarningLight = Color(0xFFFFF3E0)

        val Purple = Color(0xFF5E5CE6)
        val PurpleLight = Color(0xFFF0EFFF)

        val Pink = Color(0xFFFF2D55)
        val PinkLight = Color(0xFFFFE5EA)

        val TextPrimary = Color(0xFF1C1C1E)
        val TextSecondary = Color(0xFF8E8E93)
        val TextTertiary = Color(0xFFC7C7CC)

        val Divider = Color(0xFFE5E5EA)
    }

    object Shapes {
        val Small = RoundedCornerShape(12.dp)
        val Medium = RoundedCornerShape(16.dp)
        val Large = RoundedCornerShape(24.dp)
        val ExtraLarge = RoundedCornerShape(32.dp)
    }

    object Spacing {
        val ExtraSmall = 4.dp
        val Small = 8.dp
        val Medium = 16.dp
        val Large = 24.dp
        val ExtraLarge = 32.dp
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val dashboardState by viewModel.dashboardState.collectAsState()
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState.user) {
        if (authState.user == null && !authState.isLoading) {
            navController.navigate("login") { popUpTo(0) { inclusive = true } }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        containerColor = DesignSystem.Colors.Background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_adherent") },
                containerColor = DesignSystem.Colors.Primary,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(Icons.Rounded.Add, "Ajouter", modifier = Modifier.size(28.dp))
            }
        },
        bottomBar = { MinimalBottomNav(navController = navController) }
    ) { padding ->
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
                    state.isLoading && state.data == null -> MinimalLoadingSkeleton()
                    state.error != null -> ModernErrorState(state.error!!) { viewModel.refresh() }
                    state.data != null -> {
                        ModernDashboardContent(
                            data = state.data!!,
                            agent = authState.user,
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ModernDashboardContent(
    data: com.example.sencsu.data.remote.dto.DashboardResponseDto,
    agent: AgentDto?,
    navController: NavController,
    sessionManager: SessionManager
) {
    val adherents = data.data
    val totalAmount = adherents.sumOf { it.montantTotal ?: 0.0 }
    val totalDeps = adherents.sumOf { it.personnesCharge.size }
    val activeMembers = adherents.filter { it.actif == true }.size

    // Calculs pour les insights
    val recentJoiners = adherents.filter {
        // Adhérents des 30 derniers jours
        true // À adapter selon votre logique
    }.size

    val avgContribution = if (adherents.isNotEmpty()) totalAmount / adherents.size else 0.0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium)
    ) {
        // Header avec salutation
        item {
            GreetingHeader(
                agent = agent,
                onProfileClick = { navController.navigate("profile") }
            )
        }

        // Search Bar
        item {
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Small))
            SearchBarMinimal(onClick = { navController.navigate("search") })
        }

        // Stats principales avec animation
        item {
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Large))
            PrimaryStatsCards(
                totalMembers = adherents.size,
                totalAmount = totalAmount,
                activeMembers = activeMembers
            )
        }

        // Quick Actions Horizontal
        item {
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Large))
            QuickActionsHorizontal(navController)
        }

        // Insights Section
        item {
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Large))
            InsightsSection(
                recentJoiners = recentJoiners,
                avgContribution = avgContribution,
                totalDependents = totalDeps
            )
        }

        // Chart Overview
        item {
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Large))
            ChartOverviewCard(adherents = adherents)
        }

        // Activities Timeline
        item {
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Large))
            ActivitiesTimeline(
                adherents = adherents.sortedByDescending { it.createdAt }.take(5),
                onAdherentClick = { id -> navController.navigate("adherent_details/$id") },
                onSeeAllClick = { navController.navigate("liste_adherents") },
                sessionManager = sessionManager
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun GreetingHeader(
    agent: AgentDto?,
    onProfileClick: () -> Unit
) {
    val greeting = remember {
        val hour = java.time.LocalTime.now().hour
        when {
            hour < 12 -> "Bonjour"
            hour < 18 -> "Bon après-midi"
            else -> "Bonsoir"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignSystem.Spacing.Medium)
            .padding(top = DesignSystem.Spacing.Large),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = greeting,
                style = MaterialTheme.typography.titleSmall,
                color = DesignSystem.Colors.TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = agent?.prenom ?: "Utilisateur",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = DesignSystem.Colors.TextPrimary
            )
        }

        // Avatar moderne
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            DesignSystem.Colors.Primary,
                            DesignSystem.Colors.Purple
                        )
                    )
                )
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = agent?.prenom?.take(1) ?: "?",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SearchBarMinimal(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignSystem.Spacing.Medium)
            .height(52.dp)
            .clickable { onClick() },
        shape = DesignSystem.Shapes.Medium,
        color = DesignSystem.Colors.Surface,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, DesignSystem.Colors.Divider)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Search,
                contentDescription = null,
                tint = DesignSystem.Colors.TextTertiary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Rechercher un adhérent...",
                color = DesignSystem.Colors.TextTertiary,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun PrimaryStatsCards(
    totalMembers: Int,
    totalAmount: Double,
    activeMembers: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignSystem.Spacing.Medium),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card principale - Encaissements
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = DesignSystem.Shapes.Large,
            color = DesignSystem.Colors.Primary,
            shadowElevation = 4.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Pattern décoratif
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 50.dp, y = (-50).dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(DesignSystem.Spacing.Large),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Total Encaissé",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.TrendingUp,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Column {
                        Text(
                            String.format("%.0f", totalAmount),
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            "FCFA",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Mini cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MiniStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.People,
                label = "Adhérents",
                value = totalMembers.toString(),
                color = DesignSystem.Colors.Success,
                backgroundColor = DesignSystem.Colors.SuccessLight
            )

            MiniStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.CheckCircle,
                label = "Actifs",
                value = activeMembers.toString(),
                color = DesignSystem.Colors.Purple,
                backgroundColor = DesignSystem.Colors.PurpleLight
            )
        }
    }
}

@Composable
private fun MiniStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    backgroundColor: Color
) {
    Surface(
        modifier = modifier.height(100.dp),
        shape = DesignSystem.Shapes.Medium,
        color = DesignSystem.Colors.Surface,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, DesignSystem.Colors.Divider)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignSystem.Spacing.Medium),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = CircleShape,
                color = backgroundColor,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column {
                Text(
                    value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = DesignSystem.Colors.TextPrimary
                )
                Text(
                    label,
                    fontSize = 12.sp,
                    color = DesignSystem.Colors.TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun QuickActionsHorizontal(navController: NavController) {
    Column(
        modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Medium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Actions rapides",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DesignSystem.Colors.TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                QuickActionCard(
                    icon = Icons.Rounded.PersonAdd,
                    label = "Nouvel\nAdhérent",
                    color = DesignSystem.Colors.Primary,
                    backgroundColor = DesignSystem.Colors.PrimaryLight,
                    onClick = { navController.navigate("add_adherent") }
                )
            }
            item {
                QuickActionCard(
                    icon = Icons.Rounded.FormatListBulleted,
                    label = "Liste\nComplète",
                    color = DesignSystem.Colors.Success,
                    backgroundColor = DesignSystem.Colors.SuccessLight,
                    onClick = { navController.navigate("liste_adherents") }
                )
            }
            item {
                QuickActionCard(
                    icon = Icons.Rounded.BarChart,
                    label = "Statistiques",
                    color = DesignSystem.Colors.Purple,
                    backgroundColor = DesignSystem.Colors.PurpleLight,
                    onClick = { /* TODO */ }
                )
            }
            item {
                QuickActionCard(
                    icon = Icons.Rounded.Badge,
                    label = "Cartes\nMembres",
                    color = DesignSystem.Colors.Warning,
                    backgroundColor = DesignSystem.Colors.WarningLight,
                    onClick = { navController.navigate("liste_cartes") }
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    color: Color,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(120.dp)
            .height(100.dp)
            .clickable { onClick() },
        shape = DesignSystem.Shapes.Medium,
        color = backgroundColor,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignSystem.Spacing.Medium),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.2f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = color,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun InsightsSection(
    recentJoiners: Int,
    avgContribution: Double,
    totalDependents: Int
) {
    Column(
        modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Medium)
    ) {
        Text(
            "Aperçu",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DesignSystem.Colors.TextPrimary
        )

        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = DesignSystem.Shapes.Medium,
            color = DesignSystem.Colors.Surface,
            shadowElevation = 0.dp,
            border = BorderStroke(1.dp, DesignSystem.Colors.Divider)
        ) {
            Column(
                modifier = Modifier.padding(DesignSystem.Spacing.Medium)
            ) {
                InsightRow(
                    icon = Icons.Rounded.TrendingUp,
                    label = "Nouveaux adhérents",
                    value = "$recentJoiners ce mois",
                    color = DesignSystem.Colors.Success
                )

                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = DesignSystem.Colors.Divider
                )

                InsightRow(
                    icon = Icons.Rounded.Calculate,
                    label = "Cotisation moyenne",
                    value = String.format("%.0f FCFA", avgContribution),
                    color = DesignSystem.Colors.Primary
                )

                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = DesignSystem.Colors.Divider
                )

                InsightRow(
                    icon = Icons.Rounded.FamilyRestroom,
                    label = "Total bénéficiaires",
                    value = "$totalDependents personnes",
                    color = DesignSystem.Colors.Purple
                )
            }
        }
    }
}

@Composable
private fun InsightRow(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                label,
                fontSize = 14.sp,
                color = DesignSystem.Colors.TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = DesignSystem.Colors.TextPrimary
        )
    }
}

@Composable
private fun ChartOverviewCard(adherents: List<Any>) {
    Column(
        modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Medium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Vue d'ensemble",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DesignSystem.Colors.TextPrimary
            )

            TextButton(onClick = { /* TODO */ }) {
                Text(
                    "Détails",
                    color = DesignSystem.Colors.Primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = DesignSystem.Colors.Primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = DesignSystem.Shapes.Medium,
            color = DesignSystem.Colors.Surface,
            shadowElevation = 0.dp,
            border = BorderStroke(1.dp, DesignSystem.Colors.Divider)
        ) {
            Column(modifier = Modifier.padding(DesignSystem.Spacing.Medium)) {
                AdherentChartCard(adherents = adherents as List<AdherentDto>)
            }
        }
    }
}

@Composable
private fun ActivitiesTimeline(
    adherents: List<Any>,
    onAdherentClick: (String) -> Unit,
    onSeeAllClick: () -> Unit,
    sessionManager: SessionManager
) {
    Column(
        modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Medium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Activités récentes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DesignSystem.Colors.TextPrimary
            )

            TextButton(onClick = onSeeAllClick) {
                Text(
                    "Voir tout",
                    color = DesignSystem.Colors.Primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = DesignSystem.Colors.Primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))

        RecentActivitiesSection(
            adherents = adherents as List<AdherentDto>,
            onAdherentClick = onAdherentClick as (Long) -> Unit,
            onSeeAllClick = onSeeAllClick,
            sessionManager = sessionManager
        )
    }
}

@Composable
private fun MinimalBottomNav(navController: NavController) {
    NavigationBar(
        containerColor = DesignSystem.Colors.Surface,
        tonalElevation = 0.dp,
        modifier = Modifier.border(
            width = 1.dp,
            color = DesignSystem.Colors.Divider,
            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
        )
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = {
                Icon(
                    Icons.Rounded.Home,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Accueil", fontSize = 12.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DesignSystem.Colors.Primary,
                selectedTextColor = DesignSystem.Colors.Primary,
                indicatorColor = DesignSystem.Colors.PrimaryLight
            )
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("liste_adherents") },
            icon = {
                Icon(
                    Icons.Outlined.FormatListBulleted,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Liste", fontSize = 12.sp) }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("profile") },
            icon = {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Profil", fontSize = 12.sp) }
        )
    }
}

@Composable
private fun MinimalLoadingSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DesignSystem.Spacing.Medium),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium)
    ) {
        // Header skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Box(
                    Modifier
                        .width(100.dp)
                        .height(16.dp)
                        .clip(DesignSystem.Shapes.Small)
                        .background(DesignSystem.Colors.SurfaceVariant)
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    Modifier
                        .width(150.dp)
                        .height(28.dp)
                        .clip(DesignSystem.Shapes.Small)
                        .background(DesignSystem.Colors.SurfaceVariant)
                )
            }
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(DesignSystem.Colors.SurfaceVariant)
            )
        }

        Spacer(Modifier.height(DesignSystem.Spacing.Large))

        // Stats skeleton
        Box(
            Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(DesignSystem.Shapes.Large)
                .background(DesignSystem.Colors.SurfaceVariant)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier
                    .weight(1f)
                    .height(100.dp)
                    .clip(DesignSystem.Shapes.Medium)
                    .background(DesignSystem.Colors.SurfaceVariant)
            )
            Box(
                Modifier
                    .weight(1f)
                    .height(100.dp)
                    .clip(DesignSystem.Shapes.Medium)
                    .background(DesignSystem.Colors.SurfaceVariant)
            )
        }
    }
}

@Composable
private fun ModernErrorState(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DesignSystem.Spacing.ExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = DesignSystem.Colors.PinkLight,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    tint = DesignSystem.Colors.Pink,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(Modifier.height(DesignSystem.Spacing.Large))

        Text(
            "Oups !",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DesignSystem.Colors.TextPrimary
        )

        Spacer(Modifier.height(DesignSystem.Spacing.Small))

        Text(
            error,
            textAlign = TextAlign.Center,
            color = DesignSystem.Colors.TextSecondary,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(DesignSystem.Spacing.Large))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = DesignSystem.Colors.Primary
            ),
            shape = DesignSystem.Shapes.Medium
        ) {
            Icon(Icons.Rounded.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Réessayer")
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        Modifier
            .fillMaxSize()
            .padding(DesignSystem.Spacing.ExtraLarge),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Rounded.Inbox,
                contentDescription = null,
                tint = DesignSystem.Colors.TextTertiary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(DesignSystem.Spacing.Medium))
            Text(
                "Aucune donnée disponible",
                color = DesignSystem.Colors.TextSecondary,
                fontSize = 16.sp
            )
        }
    }
}