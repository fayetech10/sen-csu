package com.example.sencsu.components.modals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sencsu.components.forms.DatePickerField
import com.example.sencsu.components.forms.DropdownSelector
import com.example.sencsu.components.forms.FormTextField
import com.example.sencsu.components.forms.ImagePickerComponent
import com.example.sencsu.components.forms.SegmentedSelector
import com.example.sencsu.data.remote.dto.FormConstants
import com.example.sencsu.domain.viewmodel.AddAdherentViewModel
import com.example.sencsu.utils.Formatters

@Composable
fun AddDependantModalContent(
    viewModel: AddAdherentViewModel,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    // 1. Observation de l'état UI du ViewModel
    val state = viewModel.uiState.collectAsState()
    val currentDep = state.value.currentDependant
    val isEditing = state.value.editingIndex != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Titre du Modal
        Text(
            text = if (isEditing) "Modifier un Bénéficiaire" else "Ajouter un Bénéficiaire",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = FormConstants.Colors.primaryDark,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // --- CHAMPS DE TEXTE ---

        FormTextField(
            value = currentDep.prenoms,
            onValueChange = { newValue ->
                viewModel.updateCurrentDependant(currentDep.copy(prenoms = newValue))
            },
            label = "Prénoms",
            placeholder = "Prénoms*",
        )

        FormTextField(
            value = currentDep.nom,
            onValueChange = { newValue ->
                viewModel.updateCurrentDependant(currentDep.copy(nom = newValue))
            },
            label = "Nom",
            placeholder = "Nom*",
        )

        DatePickerField(
            label = "Date de naissance",
            value = currentDep.dateNaissance,
            onDateSelected = { selectedDate ->
                viewModel.updateCurrentDependant(
                    currentDep.copy(dateNaissance = selectedDate)
                )
            },
            isError = currentDep.dateNaissance == null
        )

        FormTextField(
            value = currentDep.adresse ?: "",
            onValueChange = { newValue ->
                viewModel.updateCurrentDependant(currentDep.copy(adresse = newValue))
            },
            label = "Adresse",
            placeholder = "Adresse",
        )

        FormTextField(
            value = currentDep.lieuNaissance ?: "",
            onValueChange = { newValue ->
                // CORRECTION : Mettre à jour `lieuNaissance` au lieu de `adresse`
                viewModel.updateCurrentDependant(currentDep.copy(lieuNaissance = newValue))
            },
            label = "Lieu de Naissance",
            placeholder = "Lieu de Naissance",
        )

        FormTextField(
            value = currentDep.whatsapp ?: "",
            onValueChange = { newValue ->
                viewModel.updateCurrentDependant(currentDep.copy(
                    whatsapp = Formatters.formatPhoneNumber(newValue)
                ))
            },
            label = "WhatsApp",
            placeholder = "77 123 45 67",
            keyboardType = KeyboardType.Number,
            maxLength = 20,
        )

        // --- SÉLECTEURS ---

        DropdownSelector(
            title = "Lien de parenté",
            options = FormConstants.LIENS_PARENTE,
            selected = currentDep.lienParent ?: "",
            onSelect = { newValue ->
                viewModel.updateCurrentDependant(currentDep.copy(lienParent = newValue))
            },
            placeholder = "Sélectionner le lien"
        )

        DropdownSelector(
            title = "Situation matrimoniale",
            options = FormConstants.SITUATIONS,
            selected = currentDep.situationM ?: "",
            onSelect = { newValue ->
                viewModel.updateCurrentDependant(currentDep.copy(situationM = newValue))
            }
        )

        SegmentedSelector(
            title = "Sexe",
            options = listOf("Masculin", "Féminin"),
            selected = if (currentDep.sexe == "M") "Masculin" else "Féminin",
            onSelect = { newValue ->
                viewModel.updateCurrentDependant(currentDep.copy(
                    sexe = if (newValue == "Masculin") "M" else "F"
                ))
            }
        )

        SegmentedSelector(
            title = "Type de pièce",
            options = FormConstants.TYPES_PIECE,
            selected = currentDep.typePiece ?: "CNI",
            onSelect = { newValue ->
                viewModel.updateCurrentDependant(currentDep.copy(typePiece = newValue))
            }
        )

        // Affichage conditionnel CNI ou Extrait
        if (currentDep.typePiece == "CNI") {
            FormTextField(
                value = currentDep.numeroCNi ?: "",
                onValueChange = { newValue ->
                    viewModel.updateCurrentDependant(currentDep.copy(numeroCNi = newValue))
                },
                label = "Numéro CNI",
                placeholder = "Numéro CNI",
                keyboardType = KeyboardType.Number,
            )
        } else {
            FormTextField(
                value = currentDep.numeroExtrait ?: "",
                onValueChange = { newValue ->
                    viewModel.updateCurrentDependant(currentDep.copy(numeroExtrait = newValue))
                },
                label = "Numéro Extrait",
                placeholder = "Numéro Extrait",
                keyboardType = KeyboardType.Number,
            )
        }

        ImagePickerComponent(
            label = "Photo d'identité",
            imageUri =currentDep.photo,
            onImageSelected = { viewModel.updateDependantPhotoUri( it ) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        ImagePickerComponent(
            label = "Pièce - Recto",
            imageUri = currentDep.photoRecto,
            onImageSelected = { viewModel.updateDependantRectoUri(it) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        ImagePickerComponent(
            label = "Pièce - Verso",
            imageUri = currentDep.photoVerso,
            onImageSelected = { viewModel.updateDependantVersoUri(it) }
        )

        // --- BOUTONS D'ACTION ---

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FormConstants.Colors.inputBorder,
                    contentColor = FormConstants.Colors.textDark
                ),
                shape = RoundedCornerShape(12.dp) // CORRECTION de la forme du bouton
            ) {
                Text(
                    text = "Annuler",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FormConstants.Colors.primary
                ),
                shape = RoundedCornerShape(12.dp) // CORRECTION de la forme du bouton
            ) {
                Text(
                    text = if (isEditing) "Modifier" else "Ajouter",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FormConstants.Colors.white
                )
            }
        }

        // Espace final pour le scroll
        Spacer(modifier = Modifier.height(20.dp))
    }
}