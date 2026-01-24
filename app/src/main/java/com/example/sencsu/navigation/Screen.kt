package com.example.sencsu.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Home)
    object Members : Screen("members", "Members", Icons.Default.Person)
    object AddMember : Screen("add_member", "Add", Icons.Default.Add)
    object Payments : Screen("payments", "Payments", Icons.Default.AddCircle)
    object Profile : Screen("profile", "Profile", Icons.Default.AccountCircle)
        object Payment {
            fun createRoute(adherentId: Long, montant: Int) =
                "payments/$adherentId/$montant"
        }


}