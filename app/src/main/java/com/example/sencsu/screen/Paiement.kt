package com.example.sencsu.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import android.graphics.BitmapFactory
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sencsu.domain.viewmodel.PaiementViewModel
import java.io.File
import android.content.Context
import androidx.core.content.FileProvider

data class PaiementFormState(
    val reference: String = "",
    val photoPaiement: Uri? = null,
    val modePaiement: String = "Virement",
    val montantTotal: String = "",
    val adherentId: Long? = null,
    val isLoading: Boolean = false,
    val errorMessage: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Paiement(
    adherentId: Long?,
    montantTotal: Int?,
    viewModel: PaiementViewModel =  hiltViewModel ()
) {
    val context = LocalContext.current
    val state = viewModel.uiState
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.Builder().build()) }

    // Initialisation unique
    LaunchedEffect(Unit) {
        viewModel.updateMontant(montantTotal?.toString() ?: "")
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) state.photoPaiement?.let { viewModel.processImage(it, context, recognizer) }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewModel.setPhoto(it)
            viewModel.processImage(it, context, recognizer)
        }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Paiement Cotisation") }) }
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
            // Section Photo avec état vide/chargé
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

            // Formulaire
            OutlinedTextField(
                value = state.reference,
                onValueChange = viewModel::updateReference,
                label = { Text("Référence (détectée ou manuelle) ${state.reference.ifBlank { "(optionnel)" }}" ) },
                modifier = Modifier.fillMaxWidth(),
                prefix = { Icon(Icons.Default.Info, null) },
                isError = state.reference.isEmpty()
            )

            ModePaiementDropdown(
                selectedMode = state.modePaiement,
                onModeSelected = viewModel::updateMode
            )

            OutlinedTextField(
                value = state.montantTotal,
                onValueChange = viewModel::updateMontant,
                label = { Text("Montant (FCFA)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                readOnly = montantTotal != null
            )

            Button(
                onClick = { /* Appel API */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = state.reference.isNotBlank() && state.photoPaiement != null
            ) {
                Text("Confirmer le paiement")
            }
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Bouton pour supprimer la photo
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(MaterialTheme.colorScheme.errorContainer, CircleShape)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.onErrorContainer)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Camera, contentDescription = null, modifier = Modifier.size(48.dp))
                    Text("Aucun justificatif", style = MaterialTheme.typography.labelLarge)
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
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
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Camera, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Caméra")
        }
        FilledTonalButton(
            onClick = onGalleryClick,
            modifier = Modifier.weight(1f),
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
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth()
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
    // Crée un fichier temporaire dans le cache de l'application
    val tempFile = File.createTempFile(
        "payment_proof_",
        ".jpg",
        context.cacheDir
    ).apply {
        createNewFile()
        deleteOnExit() // Supprime le fichier quand l'app se ferme (optionnel)
    }

    // Retourne l'URI via le FileProvider pour que la caméra puisse écrire dedans
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile
    )
}