package com.example.sencsu.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.sencsu.R
import com.example.sencsu.data.repository.SessionManager

@Composable
fun ImageViewerDialog(
    imageUrl: String?,
    sessionManager: SessionManager,
    onDismiss: () -> Unit,
    onDownloadClick: (String) -> Unit // Callback pour le téléchargement
) {
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imageUrl ?: "") // Utilise une chaîne vide si imageUrl est null
            .apply {
                // Ajout du token si disponible
                sessionManager.tokenFlow.collectAsState(initial = null).value?.let {
                    addHeader("Authorization", "Bearer $it")
                }
            }
            .crossfade(true)
            .build()
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Affichage de l'image avec Coil
            when (val result = painter.state) {
                is AsyncImagePainter.State.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is AsyncImagePainter.State.Success -> {
                    val imagePainter: Painter = result.painter
                    Image(
                        painter = imagePainter,
                        contentDescription = "Image agrandie",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                is AsyncImagePainter.State.Error -> {
                    // Afficher une image par défaut en cas d'erreur
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground), // Image par défaut
                        contentDescription = "Erreur de chargement",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                else -> {
                    // Autre état non géré
                }
            }

            // Bouton Fermer (en haut à gauche)
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(40.dp),
                enabled = imageUrl != null // Activer seulement si on a une URL
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fermer",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Bouton Télécharger (en bas à droite)
            if (imageUrl != null) {
                IconButton(
                    onClick = { onDownloadClick(imageUrl) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(40.dp),
                    enabled = imageUrl != null && painter.state is AsyncImagePainter.State.Success
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Télécharger l'image",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
