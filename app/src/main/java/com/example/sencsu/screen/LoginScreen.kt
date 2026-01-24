package com.example.sencsu.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sencsu.domain.viewmodel.LoginUiEvent
import com.example.sencsu.domain.viewmodel.LoginViewModel

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val viewModel: LoginViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // On écoute les événements uniques du ViewModel
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect {
            when (it) {
                is LoginUiEvent.NavigateToDashboard -> onLoginSuccess()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Connexion", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") }
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mot de passe") },
                visualTransformation = PasswordVisualTransformation()
            )

            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(onClick = { viewModel.login(email, password) }) {
                    Text("Se connecter")
                }
            }

            state.error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}