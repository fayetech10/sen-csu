package com.example.sencsu.screen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sencsu.domain.viewmodel.PaiementViewModel
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File

data class PaiementFormState(
    val reference: String = "",
    val photoPaiement: Uri? = null,
    val modePaiement: String = "Virement",
    val montantTotal: Double? = null,
    val adherentId: Long? = null,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val isSuccess: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Paiement(
    adherentId: Long?,
    montantTotal: Double?,
    navController: NavController,
    viewModel: PaiementViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state = viewModel.uiState
    val recognizer = remember {
        TextRecognition.getClient(TextRecognizerOptions.Builder().build())
    }

    // Initialiser les données du formulaire avec les paramètres reçus
    LaunchedEffect(adherentId, montantTotal) {
        viewModel.initializeFormData(adherentId, montantTotal)
    }
    LaunchedEffect(adherentId) {
        adherentId?.let {
            viewModel.loadAdherent(it)
        }
    }


    // Redirection après succès du paiement
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            navController.navigate("dashboard") {
                popUpTo("paiement") { inclusive = true }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            state.photoPaiement?.let { uri ->
                viewModel.processImage(uri, context, recognizer)
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.setPhoto(it)
            viewModel.processImage(it, context, recognizer)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Paiement Cotisation") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section Photo
            PaymentPhotoSection(
                photoUri = state.photoPaiement,
                isLoading = state.isLoading,
                onRemove = { viewModel.setPhoto(null) }
            )

            // Boutons d'action
            PhotoActionsRow(
                onCameraClick = {
                    val uri = createTempPictureUri(context)
                    viewModel.setPhoto(uri)
                    cameraLauncher.launch(uri)
                },
                onGalleryClick = { galleryLauncher.launch("image/*") }
            )

            // Affichage des erreurs
            if (state.errorMessage.isNotEmpty()) {
                ErrorMessageCard(message = state.errorMessage)
            }

            // Champ Référence
            OutlinedTextField(
                value = state.reference,
                onValueChange = viewModel::updateReference,
                label = {
                    Text(
                        "Référence ${if (state.reference.isBlank()) "(détectée ou manuelle)" else "(validée)"}"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                isError = state.reference.isBlank(),
                singleLine = true
            )

            // Dropdown Mode de paiement
            ModePaiementDropdown(
                selectedMode = state.modePaiement,
                onModeSelected = viewModel::updateMode
            )

            // Champ Montant (lecture seule, vient du formulaire d'ajout adhérent)
            OutlinedTextField(
                value = montantTotal.toString(),
                onValueChange = {},
                label = { Text("Montant (FCFA)") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // Bouton de confirmation
            Button(
                onClick = { viewModel.addPaiement() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = state.reference.isNotBlank() &&
                        state.photoPaiement != null &&
                        !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    if (state.isLoading) "Confirmation en cours..." else "Confirmer le paiement"
                )
            }
        }
    }
}

@Composable
private fun ErrorMessageCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = "Erreur",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun PaymentPhotoSection(
    photoUri: Uri?,
    isLoading: Boolean,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Photo de justificatif",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Supprimer la photo",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Camera,
                        contentDescription = "Caméra",
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Aucun justificatif",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

@Composable
fun PhotoActionsRow(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onCameraClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Camera, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Caméra")
        }
        FilledTonalButton(
            onClick = onGalleryClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Galerie")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModePaiementDropdown(
    selectedMode: String,
    onModeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val modes = listOf("Virement", "Wave", "Orange Money", "Espèces", "Chèque")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedMode,
            onValueChange = {},
            readOnly = true,
            label = { Text("Moyen de paiement") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            modes.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(text = mode) },
                    onClick = {
                        onModeSelected(mode)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

fun createTempPictureUri(context: Context): Uri {
    val tempFile = File.createTempFile(
        "payment_proof_",
        ".jpg",
        context.cacheDir
    ).apply {
        deleteOnExit()
    }

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile
    )
}