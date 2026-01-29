package com.example.healthcard.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sencsu.data.remote.dto.AdherentDto

/**
 * Modèle de données pour la carte de couverture sanitaire
 */
data class HealthCardData(
    val nom: String,
    val prenom: String,
    val numeroImmatriculation: String,
    val dateEmission: String,
    val dateExpiration: String = "",
    val qrCodeUrl: String? = null,
    val photoUrl: String? = null,
    val codeBarres: String = "01443720",
    val flagUrl: String = "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fd/Flag_of_Senegal.svg/1200px-Flag_of_Senegal.svg.png",
    val logoUrl: String = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7e/Coat_of_arms_of_Senegal.svg/1024px-Coat_of_arms_of_Senegal.svg.png"
)

/**
 * Composant principal pour afficher la carte avec animation flip
 */
@Composable
fun HealthInsuranceCardWithFlip(
    data: AdherentDto,
    modifier: Modifier = Modifier
) {
    var isFlipped by remember { mutableStateOf(false) }

    // Animation de rotation
    val rotationY by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 600)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.7f)
            .clickable { isFlipped = !isFlipped }
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0xFF000000),
                spotColor = Color(0xFF000000)
            )
    ) {
        if (rotationY < 90) {
            // Recto de la carte
            CardRecto(
                data = data,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.rotationY = rotationY
                        cameraDistance = 8 * density
                    }
            )
        } else {
            // Verso de la carte
            CardVerso(
                data = data,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.rotationY = rotationY
                        cameraDistance = 8 * density
                    }
            )
        }
    }
}

/**
 * Recto de la carte
 */
@Composable
private fun CardRecto(
    data: AdherentDto,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(20.dp))
    ) {
        // Dégradé de fond
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFF5F5F5),
                            Color(0xFFFFFFFF),
                            Color(0xFFF0F0F0)
                        )
                    )
                )
        )

        // Bande colorée en bas (drapeau sénégalais)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF2D7F4F), // Vert
                            Color(0xFFFCE181), // Jaune
                            Color(0xFFC41E3A)  // Rouge
                        )
                    )
                )
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
        )

        // Contenu principal
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Section gauche - Code QR et infos
            Column(
                modifier = Modifier
                    .weight(0.45f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // QR Code
                Card(
                    modifier = Modifier
                        .size(140.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        if (data.qrCodeUrl != null) {
                            AsyncImage(
                                model = data.qrCodeUrl,
                                contentDescription = "QR Code",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Placeholder QR Code
                            Text(
                                text = "QR",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFCCCCCC)
                                )
                            )
                        }
                    }
                }

                // Code barres
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(30.dp)
                            .background(Color.White, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "║ ║ ║║║║ ║ ║",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontFamily = FontFamily.Monospace
                            ),
                            letterSpacing = 1.5.sp
                        )
                    }
                    Text(
                        text = data.codeBarres,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace
                        ),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            // Séparateur
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFEEEEEE))
            )

            // Section droite - Informations personnelles
            Column(
                modifier = Modifier
                    .weight(0.55f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // En-tête avec drapeau
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Drapeau du Sénégal depuis URL
                    AsyncImage(
                        model = data.flagUrl,
                        contentDescription = "Drapeau Sénégal",
                        modifier = Modifier
                            .size(50.dp, 33.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Text(
                        text = "CARTE DE COUVERTURE SANTÉ",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Informations personnelles
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InfoField(label = "Nom", value = data.nom)
                    InfoField(label = "Prénom(s)", value = data.prenoms)
                    InfoField(label = "N° Immatriculation", value = data.id.toString())
                    InfoField(label = "Carte émise le", value = data.createdAt)
                }

                // Logos en bas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = data.logoUrl,
                        contentDescription = "Logo ASSU",
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = "AGENCE SENEGALAISE DE LA\nCOUVERTURE SANITAIRE",
                        style = TextStyle(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF666666),
                            lineHeight = 10.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Verso de la carte
 */
@Composable
private fun CardVerso(
    data: AdherentDto,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF1A1A1A))
            .clip(RoundedCornerShape(20.dp))
    ) {
        // Bande de sécurité en haut
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(40.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF2D7F4F).copy(alpha = 0.8f),
                            Color(0xFFFCE181).copy(alpha = 0.8f),
                            Color(0xFFC41E3A).copy(alpha = 0.8f)
                        )
                    )
                )
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
        ) {
            Text(
                text = "VERSO - INFORMATIONS CONFIDENTIELLES",
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 12.dp)
            )
        }

        // Contenu du verso
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 60.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Section 1: Bande magnétique
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(Color(0xFF2D2D2D), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFF444444), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "█████████████████████████████",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = Color(0xFF444444),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
                Text(
                    text = "Bande magnétique / Piste de sécurité",
                    style = TextStyle(
                        fontSize = 9.sp,
                        color = Color(0xFF999999),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Section 2: Conditions d'utilisation
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "CONDITIONS D'UTILISATION",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFCE181)
                    )
                )

                val conditions = listOf(
                    "• Carte nominative - Non transférable",
                    "• Valide jusqu'au: ${data.createdAt?.ifEmpty { "À déterminer" }}",
                    "• Conserver en bon état",
                    "• Signaler la perte immédiatement",
                    "• En cas de problème: contactez l'ASSU"
                )

                conditions.forEach { condition ->
                    Text(
                        text = condition,
                        style = TextStyle(
                            fontSize = 9.sp,
                            color = Color(0xFFCCCCCC),
                            lineHeight = 12.sp
                        )
                    )
                }
            }

            // Section 3: Contacts
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "CONTACTS D'URGENCE",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC41E3A)
                    )
                )

                Text(
                    text = "ASSU (Agence Sénégalaise de Couverture Sanitaire Universelle)",
                    style = TextStyle(
                        fontSize = 9.sp,
                        color = Color(0xFFCCCCCC),
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Text(
                    text = "Tel: +221 33 XXX XX XX\nEmail: support@assu.sn\nSite: www.assu.sn",
                    style = TextStyle(
                        fontSize = 8.sp,
                        color = Color(0xFF999999),
                        lineHeight = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }

            // Section 4: Signature/Hologramme
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color(0xFF2D2D2D), RoundedCornerShape(4.dp))
                    .border(2.dp, Color(0xFFFCE181), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "◆ HOLOGRAMME DE SÉCURITÉ ◆",
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFCE181)
                        )
                    )
                    Text(
                        text = "Série: ${data.matricule?.take(6)}",
                        style = TextStyle(
                            fontSize = 8.sp,
                            color = Color(0xFF999999),
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        }

        // Bande de couleur en bas
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF2D7F4F),
                            Color(0xFFFCE181),
                            Color(0xFFC41E3A)
                        )
                    )
                )
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
        )
    }
}

/**
 * Composant pour afficher un champ d'information
 */
@Composable
private fun InfoField(label: String, value: String?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF999999)
            )
        )
        Text(
            text = value.toString(),
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        )
    }
}