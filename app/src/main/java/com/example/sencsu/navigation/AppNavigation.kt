package com.example.sencsu.navigation

import AddAdherentScreen
import ListCardDisponible
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sencsu.data.repository.SessionManager
import com.example.sencsu.domain.viewmodel.AppNavigationViewModel
import com.example.sencsu.screen.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(

    viewModel: AppNavigationViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val sessionManager = viewModel.sessionManager



    // Récupération de l'agentId depuis le ViewModel
    val agentId by viewModel.agentId.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.logoutEvent
            .onEach {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true } // Efface toute la pile de navigation
                }
            }
            .launchIn(this)
    }

    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate("dashboard") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(navController = navController)
        }

        composable(
            route = "add_adherent",
        ) {
            AddAdherentScreen(
                onBack = { navController.popBackStack() },
                onNavigateToPayment = { adherentId, montantTotal ->
                    navController.navigate("payments/${adherentId}/$montantTotal")
                },
                agentId = agentId
            )
        }

        composable("liste_adherents") {
            ListeAdherentScreen(
                onNavigateBack = { navController.popBackStack() },
                onAdherentClick = { adherentId ->
                    navController.navigate("adherent_details/$adherentId")},
                sessionManager = sessionManager
            )
        }
        composable("liste_cartes") {
            ListCardDisponible(
                onNavigateBack = { navController.popBackStack() },
                onAdherentClick = { adherentId ->
                    navController.navigate("adherent_details/$adherentId")},
                sessionManager = sessionManager
            )
        }

        composable("search") {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onAdherentClick = { adherentId ->
                    navController.navigate("adherent_details/$adherentId")
                }
            )
        }

        composable(
            route = "payments/{adherentId}/{montantTotal}",
            arguments = listOf(
                navArgument("adherentId") { type = NavType.StringType },
                navArgument("montantTotal") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val adherentId = backStackEntry.arguments?.getString("adherentId")?.toLongOrNull()
            val montantTotal = backStackEntry.arguments?.getString("montantTotal")?.toDoubleOrNull()
            Paiement(adherentId = adherentId, montantTotal = montantTotal,navController = navController)
        }

        composable(
            route = "adherent_details/{id}",
            arguments = listOf(
                navArgument("id") { type = NavType.StringType }
            )
        ) {
            AdherentDetailsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
