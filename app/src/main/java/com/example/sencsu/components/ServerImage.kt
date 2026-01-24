package com.example.sencsu.components

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sencsu.R
import com.example.sencsu.configs.ApiConfig
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.sencsu.data.repository.SessionManager // Assurez-vous d'importer SessionManager
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ServerImage(
    filename: String?,
    contentDescription: String? = null,
    sessionManager: SessionManager, // Ajout du SessionManager en paramètre
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    onClick: (() -> Unit)? = null // Nouveau paramètre pour le clic
) {
    val context = LocalContext.current
    val token by sessionManager.tokenFlow.collectAsState(initial = null)

    val imageUrl = ApiConfig.getImageUrl(filename)

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .apply {
                token?.let {
                    addHeader("Authorization", "Bearer $it")
                }
            }
            .crossfade(true)
            .build(),
        contentDescription = contentDescription, // Description générique, peut être améliorée
        modifier = modifier.clickable(enabled = onClick != null) { onClick?.invoke() }, // Ajout du clic
        contentScale = contentScale,
        placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
        error = painterResource(id = R.drawable.ic_launcher_foreground)
    )
}
