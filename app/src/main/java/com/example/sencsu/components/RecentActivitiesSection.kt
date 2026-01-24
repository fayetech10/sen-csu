package com.example.sencsu.components

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sencsu.components.ModernAdherentRow
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.FormConstants
import com.example.sencsu.data.repository.SessionManager // Import de SessionManager
import kotlinx.coroutines.delay

@Composable
fun RecentActivitiesSection(
    adherents: List<AdherentDto>,
    onAdherentClick: (Long) -> Unit,
    onSeeAllClick: () -> Unit,
    sessionManager: SessionManager, // Ajout de SessionManager en paramètre
    modifier: Modifier = Modifier
) {
    val itemsToShow = remember(adherents) { adherents.take(5) }
    val visibleStates = remember {
        mutableStateListOf<Boolean>().apply {
            repeat(itemsToShow.size) { add(false) }
        }
    }

    LaunchedEffect(itemsToShow) {
        visibleStates.fill(false)
        itemsToShow.indices.forEach { index ->
            delay(index * 100L) 
            if (index < visibleStates.size) {
                visibleStates[index] = true
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Activités récentes",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = FormConstants.Colors.textDark,
                    fontSize = 20.sp
                )
            )
            Text(
                text = "Voir tout",
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onSeeAllClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                color = FormConstants.Colors.primary,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 2.dp,
            tonalElevation = 1.dp
        ) {
            if (itemsToShow.isEmpty()) {
                Text("Pas de Donnée")
            } else {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    itemsToShow.forEachIndexed { index, adherent ->
                        AnimatedVisibility(
                            visible = visibleStates.getOrElse(index) { false },
                            enter = fadeIn(animationSpec = tween(300)) +
                                    slideInVertically(
                                        initialOffsetY = { 40 },
                                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                                    )
                        ) {
                            Column {
                                ModernAdherentRow(
                                    adherent = adherent,
                                    onClick = {
                                        adherent.id?.toLongOrNull()?.let {
                                            onAdherentClick(it)
                                        }
                                    },
                                    sessionManager = sessionManager, // Passage du SessionManager
                                )

                                if (index < itemsToShow.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 24.dp),
                                        thickness = 0.8.dp,
                                        color = FormConstants.Colors.background.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
