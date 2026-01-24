package com.example.sencsu.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sencsu.domain.viewmodel.SplashViewModel

@Composable
fun SplashScreen(onNavigateToLogin: () -> Unit, onNavigateToDashboard: () -> Unit) {
    val viewModel: SplashViewModel = hiltViewModel()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    LaunchedEffect(isLoggedIn) {
        isLoggedIn?.let {
            if (it) {
                onNavigateToDashboard()
            } else {
                onNavigateToLogin()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}