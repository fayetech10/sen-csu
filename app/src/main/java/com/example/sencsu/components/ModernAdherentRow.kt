package com.example.sencsu.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.repository.SessionManager

// Couleurs (Réutilisation de votre palette)
private val TextPrimary = Color(0xFF1F2937)
private val TextSecondary = Color(0xFF6B7280)
private val BackgroundLight = Color(0xFFF3F4F6)
private val AccentGreen = Color(0xFF10B981)
private val AccentGreenBg = Color(0xFFD1FAE5) // Vert très clair pour le fond des badges
private val BorderLight = Color(0xFFE5E7EB)

@Composable
fun ModernAdherentRow(
    adherent: AdherentDto,
    onClick: () -> Unit,
    sessionManager: SessionManager,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            ),
        color = Color.White,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp), // Padding vertical réduit légèrement
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. IMAGE OPTIMISÉE (48dp avec bordure)
            Box(
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(48.dp), // Réduit de 56dp à 48dp
                    shape = CircleShape,
                    border = BorderStroke(1.dp, BorderLight),
                    color = BackgroundLight
                ) {
                    // Si pas de photo, on pourrait mettre une icône par défaut ici
                    if (adherent.photo != null) {
                        ServerImage(
                            filename = adherent.photo.toString(),
                            sessionManager = sessionManager,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        // Fallback visuel si pas d'image
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. CONTENU CENTRAL
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${adherent.prenoms} ${adherent.nom}",
                    fontSize = 16.sp, // Légèrement plus grand pour la lisibilité
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Info Personnes à charge avec icône
                    Icon(
                        imageVector = Icons.Rounded.FamilyRestroom,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${adherent.personnesCharge.size} à charge",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    // Séparateur bullet
                    Text(
                        text = " • ",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Text(
                        text = adherent.regime ?: "Autre",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 3. BADGE STATUT À DROITE (Style "Chip/Gélule")
            StatusChip(isPaid = true) // Logique à adapter selon vos données
        }
    }
}

@Composable
fun ModernAdherentCard(
    adherent: AdherentDto,
    onClick: () -> Unit,
    sessionManager: SessionManager,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Légère ombre pour détacher
        border = BorderStroke(1.dp, BorderLight.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image Compacte (45dp)
            Surface(
                modifier = Modifier.size(45.dp),
                shape = RoundedCornerShape(12.dp), // Carré arrondi moderne (Squircle)
                color = BackgroundLight
            ) {
                ServerImage(
                    filename = adherent.photo.toString(),
                    sessionManager = sessionManager,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${adherent.prenoms} ${adherent.nom}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = adherent.regime ?: "Non défini",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            // Badge Minimaliste
            Surface(
                color = AccentGreenBg,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Payé",
                    color = AccentGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// Composant utilitaire pour le badge
@Composable
private fun StatusChip(isPaid: Boolean) {
    Surface(
        color = if (isPaid) AccentGreenBg else Color(0xFFFEE2E2), // Vert ou Rouge clair
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(if (isPaid) AccentGreen else Color.Red, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isPaid) "Payé" else "Impayé",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPaid) Color(0xFF047857) else Color(0xFFB91C1C) // Texte plus foncé pour le contraste
            )
        }
    }
}