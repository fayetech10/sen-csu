package com.example.sencsu.components.cartes


import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.example.sencsu.R

@Composable
fun CarteSanteUniverselle(
    nom: String = "FAYE",
    prenom: String = "CHEIKH",
    numeroImmatriculation: String = "Z1762 K2061",
    numeroCarte: String = "01443720",
    dateEmission: String = "06/01/2026",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Générer un QR code à partir du numéro de carte
    val qrCodeBitmap = rememberQrCodeBitmap(numeroCarte)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E88E5), // Bleu clair
                            Color(0xFF0D47A1)  // Bleu foncé
                        )
                    )
                )
        ) {
            // QR Code en arrière-plan avec transparence
            if (qrCodeBitmap != null) {
                Image(
                    bitmap = qrCodeBitmap,
                    contentDescription = "QR Code",
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.1f)
                )
            }

            // Contenu principal de la carte
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // En-tête de la carte
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Côté gauche : Logo et titre
                    Column {
                        Text(
                            text = "COUVERTURE SANITAIRE",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "UNIVERSELLE",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Logo EDIRAMU (à remplacer par votre ressource)
                        Box(
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(4.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "EDIRAMU",
                                color = Color(0xFF0D47A1),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = "Cette carte vous donne accès aux réseaux",
                            color = Color.White,
                            fontSize = 8.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "de prestataires de soins agréés.",
                            color = Color.White,
                            fontSize = 8.sp
                        )
                    }

                    // Côté droit : Numéro de carte
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "N° CARTE",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp
                        )
                        Text(
                            text = numeroCarte,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section centrale avec les informations personnelles
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        // Titre en haut
                        Text(
                            text = "CARTE DE COUVERTURE SANTÉ",
                            color = Color(0xFF0D47A1),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Informations personnelles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Nom:",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = nom,
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Column {
                                Text(
                                    text = "Prénom(s):",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = prenom,
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Column {
                                Text(
                                    text = "Date émission:",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = dateEmission,
                                    color = Color.Black,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Numéro d'immatriculation
                        Column {
                            Text(
                                text = "N° Immatriculation:",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = numeroImmatriculation,
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Footer avec l'agence
                        Text(
                            text = "AGENCE SÉNÉGALAISE DE LA COUVERTURE SANITAIRE UNIVERSELLE",
                            color = Color(0xFF0D47A1),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Devise en bas
                Text(
                    text = "Un Peuple - Un But - Une Foi",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun rememberQrCodeBitmap(text: String): ImageBitmap? {
    return remember(text) {
        generateQrCodeBitmap(text)?.asImageBitmap()
    }
}

private fun generateQrCodeBitmap(text: String, size: Int = 512): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Extension pour l'opacité
@SuppressLint("SuspiciousModifierThen")
@Composable
fun Modifier.alpha(alpha: Float): Modifier {
    return this.then(
        graphicsLayer(alpha = alpha)
    )
}

// Pour l'utiliser dans votre écran :
/*
@Composable
fun MesCartesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CarteSanteUniverselle(
            modifier = Modifier.fillMaxWidth()
        )

        // Autre carte avec des données différentes
        CarteSanteUniverselle(
            nom = "DIOP",
            prenom = "MOHAMED",
            numeroImmatriculation = "A1234 B5678",
            numeroCarte = "98765432",
            dateEmission = "15/12/2025",
            modifier = Modifier.fillMaxWidth()
        )
    }
}
*/