package com.example.sencsu.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun PaymentsScreen(adherentId: Int?, montantTotal: Int?) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Écran de Paiement", style = MaterialTheme.typography.headlineMedium)
        adherentId?.let {
            Text("ID de l'adhérent: $it")
        }
        montantTotal?.let {
            Text("Montant Total: $it FCFA")
        }
    }
}
