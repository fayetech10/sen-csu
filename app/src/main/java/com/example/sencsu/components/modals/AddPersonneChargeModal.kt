package com.example.sencsu.components.modals

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sencsu.data.remote.dto.PersonneChargeDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPersonneChargeModal(
    personne: PersonneChargeDto,
    onPersonneChange: (PersonneChargeDto) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Ajouter une personne à charge")
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = personne.prenoms,
                onValueChange = { onPersonneChange(personne.copy(prenoms = it)) },
                label = { Text("Prénoms") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = personne.nom,
                onValueChange = { onPersonneChange(personne.copy(nom = it)) },
                label = { Text("Nom") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = personne.dateNaissance,
                onValueChange = { onPersonneChange(personne.copy(dateNaissance = it)) },
                label = { Text("Date de naissance") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onSave) {
                Text("Enregistrer")
            }
        }
    }
}