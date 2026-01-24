package com.example.sencsu.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.foundation.LocalIndication

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sencsu.R
import com.example.sencsu.configs.ApiConfig
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.repository.SessionManager

// Palette de couleurs douces et cohérentes
private val TextPrimary = Color(0xFF1F2937)
private val TextSecondary = Color(0xFF6B7280)
private val TextTertiary = Color(0xFF9CA3AF)
private val BackgroundLight = Color(0xFFF9FAFB)
private val AccentPurple = Color(0xFF8B5CF6)
private val AccentGreen = Color(0xFF10B981)
private val BorderLight = Color(0xFFE5E7EB)

@Composable
fun ModernAdherentRow(
    adherent: AdherentDto,
    onClick: () -> Unit,
    sessionManager: SessionManager, // Ajout de SessionManager
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
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Section gauche : Photo + Infos
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Photo de profil avec overlay de statut
                Box {
                    ServerImage(
                        filename = adherent.photo.toString(),
                        sessionManager = sessionManager,
//                        contentDescription = "Photo ${adherent.prenoms}",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(BackgroundLight),
                        contentScale = ContentScale.Crop,
                    )

                    // Badge de statut "Payé"
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(18.dp)
                            .background(Color.White, CircleShape)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(AccentGreen, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Informations textuelles
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${adherent.prenoms} ${adherent.nom}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Badge régime
                        Text(
                            text = adherent.regime ?: "Non spécifié",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextTertiary,
                            modifier = Modifier
                                .background(BackgroundLight, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )

                        // Nombre de personnes à charge
                        if (adherent.personnesCharge.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Group,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "+${adherent.personnesCharge.size}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Section droite : Statut de paiement
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Payé",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentGreen
                    )
                }

                Text(
                    text = "${adherent.personnesCharge.size + 1} bénéficiaire${if (adherent.personnesCharge.size + 1 > 1) "s" else ""}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextTertiary
                )
            }
        }
    }
}

// Version alternative avec design carte
@Composable
fun ModernAdherentCard(
    adherent: AdherentDto,
    onClick: () -> Unit,
    sessionManager: SessionManager, // Ajout de SessionManager
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo
            ServerImage(
                filename = adherent.photo.toString(),
                sessionManager = sessionManager,
//                contentDescription = "Photo ${adherent.prenoms}",
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(BackgroundLight),
                contentScale = ContentScale.Crop,
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${adherent.prenoms} ${adherent.nom}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Régime
                    Text(
                        text = adherent.regime ?: "—",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    // Séparateur
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .background(TextTertiary, CircleShape)
                    )

                    // Personnes à charge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Group,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${adherent.personnesCharge.size}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Badge statut
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AccentGreen.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(AccentGreen, CircleShape)
                    )
                    Text(
                        text = "Payé",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AccentGreen
                    )
                }
            }
        }
    }
}

// Version compacte pour listes denses
@Composable
fun CompactAdherentRow(
    adherent: AdherentDto,
    onClick: () -> Unit,
    sessionManager: SessionManager, // Ajout de SessionManager
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Photo compacte
        ServerImage(
            filename = adherent.photo.toString(),
            sessionManager = sessionManager,
//            contentDescription = null,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(BackgroundLight),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${adherent.prenoms} ${adherent.nom}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1
            )
            Text(
                text = "${adherent.personnesCharge.size + 1} bénéficiaire${if (adherent.personnesCharge.size + 1 > 1) "s" else ""} • ${adherent.regime ?: "—"}",
                fontSize = 11.sp,
                color = TextTertiary
            )
        }

        // Indicateur de statut minimaliste
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(AccentGreen, CircleShape)
        )
    }
}