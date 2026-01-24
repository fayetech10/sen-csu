package com.example.sencsu.components.forms

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.sencsu.data.remote.dto.FormConstants
import com.example.sencsu.data.remote.dto.PersonneChargeDto
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Section du formulaire
@Composable
fun FormSection(
    title: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = FormConstants.Colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = FormConstants.Colors.primaryDark
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    value: String?,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    var showDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Box(modifier = modifier) {
        if (value != null) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                label = {
                    Text(
                        text = label,
                        color = FormConstants.Colors.textDark
                    )
                },
                placeholder = { Text("JJ/MM/AAAA") },
                isError = isError,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Sélectionner une date",
                        tint = if (isError) FormConstants.Colors.error else FormConstants.Colors.primary
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = if (isError) FormConstants.Colors.error else FormConstants.Colors.primary,
                    unfocusedIndicatorColor = if (isError) FormConstants.Colors.error else FormConstants.Colors.inputBorder
                )
            )
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showDialog = true }
        )
    }

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
                                .format(Date(millis))
                            onDateSelected(formattedDate)
                        }
                        showDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Annuler")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// Input text standard
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormTextField(
    value: String?,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLength: Int? = null,
    modifier: Modifier = Modifier,
    isRequired: Boolean = false,
    singleLine: Boolean = true
) {
    Column(modifier = modifier) {
        if (value != null) {
            OutlinedTextField(
                value = value,
                onValueChange = { newValue ->
                    if (maxLength == null || newValue.length <= maxLength) {
                        onValueChange(newValue)
                    }
                },
                label = {
                    Text(
                        text = if (isRequired) "$label*" else label,
                        color = FormConstants.Colors.textDark
                    )
                },
                placeholder = { Text(text = placeholder, color = FormConstants.Colors.textGrey) },
                singleLine = singleLine,
                isError = isError,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = if (isError) FormConstants.Colors.error else FormConstants.Colors.primary,
                    unfocusedIndicatorColor = if (isError) FormConstants.Colors.error else FormConstants.Colors.inputBorder
                )
            )
        }

        if (isError && errorMessage != null) {
            Text(
                text = " $errorMessage",
                color = FormConstants.Colors.error,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// Sélecteur segmenté (boutons radio horizontaux)
@Composable
fun SegmentedSelector(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = FormConstants.Colors.textDark,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Surface(
            color = FormConstants.Colors.inputBorder,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                options.forEach { option ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selected == option) Color.White else Color.Transparent
                            )
                            .clickable { onSelect(option) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            fontSize = 14.sp,
                            color = if (selected == option) {
                                FormConstants.Colors.primaryDark
                            } else {
                                FormConstants.Colors.textGrey
                            },
                            fontWeight = if (selected == option) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// Sélecteur dropdown
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    placeholder: String = "",
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = FormConstants.Colors.textDark,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = if (selected.isNotEmpty()) selected else placeholder,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = FormConstants.Colors.primary,
                    focusedIndicatorColor = FormConstants.Colors.primary
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// Badge avec compteur
@Composable
fun CountBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(FormConstants.Colors.primary, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = count.toString(),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// Carte pour une personne à charge
@Composable
fun DependantCard(
    dependant: PersonneChargeDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        colors = CardDefaults.cardColors(
            containerColor = FormConstants.Colors.primaryLight.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${dependant.prenoms} ${dependant.nom}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = FormConstants.Colors.primaryDark,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        text = "${dependant.sexe}",
                        fontSize = 12.sp,
                        color = FormConstants.Colors.textGrey,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )


                        Text(
                            text = "Né(e) le: ${dependant.dateNaissance}",
                            fontSize = 12.sp,
                            color = FormConstants.Colors.textGrey
                        )

                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "Modifier",
                        tint = FormConstants.Colors.textGrey,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onEdit() }
                    )

                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = FormConstants.Colors.error,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onDelete() }
                    )
                }
            }
        }
    }
}

// Composant pour sélectionner une image
@Composable
fun ImagePickerComponent(
    label: String,
    imageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    required: Boolean = false,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    val context = LocalContext.current
    var showOptionsDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher pour la galerie
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        onImageSelected(uri)
    }

    // Launcher pour la caméra
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            onImageSelected(tempCameraUri)
        }
        tempCameraUri = null
    }

    Column(modifier = modifier) {
        Text(
            text = if (required) "$label*" else label,
            fontSize = 14.sp,
            color = if (isError) FormConstants.Colors.error else FormConstants.Colors.textDark,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 2.dp,
                    color = if (isError) {
                        FormConstants.Colors.error
                    } else if (imageUri != null) {
                        FormConstants.Colors.primary
                    } else {
                        FormConstants.Colors.inputBorder
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { showOptionsDialog = true }
        ) {
            if (imageUri != null) {
                // Affichage de l'image
                AsyncImage(
                    model = imageUri,
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Overlay avec icône de changement
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Changer l'image",
                            modifier = Modifier.size(40.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Cliquer pour changer",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // État vide
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ajouter une image",
                        tint = if (isError) FormConstants.Colors.error else FormConstants.Colors.textGrey,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Cliquer pour ajouter une photo",
                        color = if (isError) FormConstants.Colors.error else FormConstants.Colors.textGrey,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Galerie ou Appareil photo",
                        color = if (isError) FormConstants.Colors.error else FormConstants.Colors.textGrey.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (isError && imageUri == null) {
            Text(
                text = "Cette photo est requise",
                color = FormConstants.Colors.error,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    // Dialog de sélection
    if (showOptionsDialog) {
        ImageSourceDialog(
            onDismiss = { showOptionsDialog = false },
            onGalleryClick = {
                showOptionsDialog = false
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onCameraClick = {
                showOptionsDialog = false
                // Créer l'URI pour la caméra
                val uri = createImageUri(context)
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            }
        )
    }
}

@Composable
private fun ImageSourceDialog(
    onDismiss: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // En-tête
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Choisir une source",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = FormConstants.Colors.primaryDark
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = FormConstants.Colors.textGrey
                        )
                    }
                }

                Text(
                    text = "Sélectionnez comment ajouter votre photo",
                    fontSize = 13.sp,
                    color = FormConstants.Colors.textGrey,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                // Bouton Galerie
                OutlinedButton(
                    onClick = onGalleryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = FormConstants.Colors.primaryLight.copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(
                        2.dp,
                        FormConstants.Colors.primary
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = FormConstants.Colors.primary,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Galerie",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = FormConstants.Colors.primaryDark
                            )
                            Text(
                                text = "Choisir depuis la galerie",
                                fontSize = 12.sp,
                                color = FormConstants.Colors.textGrey
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bouton Appareil photo
                OutlinedButton(
                    onClick = onCameraClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = FormConstants.Colors.primaryLight.copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(
                        2.dp,
                        FormConstants.Colors.primary
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = FormConstants.Colors.primary,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Appareil photo",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = FormConstants.Colors.primaryDark
                            )
                            Text(
                                text = "Prendre une nouvelle photo",
                                fontSize = 12.sp,
                                color = FormConstants.Colors.textGrey
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bouton Annuler
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Annuler",
                        color = FormConstants.Colors.textGrey,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// Fonction utilitaire pour créer l'URI de la caméra
private fun createImageUri(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val imageFile = File.createTempFile(
        "PHOTO_${timeStamp}_",
        ".jpg",
        storageDir
    )

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}