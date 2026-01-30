import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sencsu.configs.ApiConfig
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.repository.SessionManager

/**
 * Composant amélioré pour afficher la carte de couverture sanitaire universelle
 * Design moderne avec photo du titulaire et QR Code réorganisé
 */
@Composable
fun HealthInsuranceCard(
    data: AdherentDto,
    modifier: Modifier = Modifier,
    sessionManager: SessionManager
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.7f)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0xFF000000),
                spotColor = Color(0xFF000000)
            )
    ) {
        // Dégradé de fond avec couleurs du drapeau sénégalais
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
                .clip(RoundedCornerShape(20.dp))
        )

        // Bande colorée en bas (drapeau sénégalais)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
        ) {

            // Bande tricolore
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF2D7F4F), // Vert
                                Color(0xFFFCE181), // Jaune
                                Color(0xFFC41E3A)  // Rouge
                            )
                        )
                    )
            )

            // Étoile verte au centre
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Étoile Sénégal",
                tint = Color(0xFF2D7F4F),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(6.dp)
            )
        }


        // Contenu principal
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Section gauche - Photo du titulaire
            Column(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Photo du titulaire
                PhotoProfile(
                    photoUrl = data.photo,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, Color(0xFF2D7F4F), RoundedCornerShape(12.dp)),
                    sessionManager = sessionManager

                )

                // Code-barres
                BarcodeView(code = data.codeBarres)
            }

            // Séparateur vertical
            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFDDDDDD))
            )

            // Section droite - Infos + QR Code
            Column(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // En-tête avec drapeau
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FlagSenegal(modifier = Modifier.size(45.dp, 30.dp))
                    Text(
                        text = "CARTE DE COUVERTURE SANTÉ",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            letterSpacing = 0.3.sp
                        ),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                // Conteneur infos + QR Code
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Informations personnelles (gauche)
                    Column(
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxHeight()
                            .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InfoField(label = "Nom", value = data.nom)
                        InfoField(label = "Prénom(s)", value = data.prenoms)
                        InfoField(label = "N° Immatriculation", value = data.matricule)
                        InfoField(label = "Carte émise le", value = data.createdAt)
                    }

                    // QR Code (droite)
                    Card(
                        modifier = Modifier
                            .weight(0.4f)
                            .aspectRatio(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White)
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (data.qrCodeUrl != null) {
                                AsyncImage(
//                                    model = data.qrCodeUrl,
                                    model = "https://pngimg.com/uploads/qr_code/qr_code_PNG17.png",
                                    contentDescription = "QR Code",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Placeholder QR Code
                                Text(
                                    text = "QR",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFCCCCCC)
                                    )
                                )
                            }
                        }
                    }
                }

                // Logos en bas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = "https://sencsu.sn/assets/logo.png", // Remplace par un lien direct
                        contentDescription = "Logo SEN-CSU",
                        modifier = Modifier
                            .size(32.dp) // Légèrement plus grand pour la lisibilité
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White),
                        contentScale = ContentScale.Fit, // Fit est souvent mieux pour les logos
                        // Ajouter un placeholder en cas d'erreur
                        error = painterResource(id = android.R.drawable.ic_menu_report_image)
                    )
                    Text(
                        text = "AGENCE SENEGALAISE DE LA\nCOUVERTURE SANITAIRE",
                        style = TextStyle(
                            fontSize = 7.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF666666),
                            lineHeight = 9.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Composant pour afficher la photo du profil
 */
@Composable
private fun PhotoProfile(
    photoUrl: String?,
    modifier: Modifier = Modifier,
    sessionManager: SessionManager
) {
    val context = LocalContext.current
    val token by sessionManager.tokenFlow.collectAsState(initial = null)
    val imageUrl = ApiConfig.getImageUrl(photoUrl)

    if (photoUrl != null) {
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
            contentDescription = "Photo du titulaire",
            modifier = modifier.background(Color.White),
            contentScale = ContentScale.Crop
        )


    } else {
        // Placeholder avec icône profil
        Box(
            modifier = modifier.background(Color(0xFFF0F0F0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Photo par défaut",
                modifier = Modifier.size(60.dp),
                tint = Color(0xFF999999)
            )
        }
    }
}

/**
 * Composant pour afficher un champ d'information
 */
@Composable
private fun InfoField(label: String, value: String?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF888888)
            )
        )
        if (value != null) {
            Text(
                text = value,
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF222222),
                    lineHeight = 11.sp
                ),
                maxLines = 1
            )
        }
    }
}

/**
 * Composant pour afficher le drapeau du Sénégal
 */
@Composable
private fun FlagSenegal(modifier: Modifier = Modifier) {
    Row(modifier = modifier) {

        // Vert
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(0xFF2D7F4F))
        )

        // Jaune + étoile
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(0xFFFCE181)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Étoile Sénégal",
                tint = Color(0xFF2D7F4F),
                modifier = Modifier.size(24.dp)
            )
        }

        // Rouge
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(0xFFC41E3A))
        )
    }
}


// Extension pour ajouter margin facilement
private fun Modifier.marginTop(value: Dp): Modifier {
    return this.padding(top = value)
}
@Composable
fun BarcodeView(code: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {

        // Zone code-barres
        Column(
            modifier = Modifier
                .width(120.dp)
                .height(40.dp)
                .background(Color.White, RoundedCornerShape(4.dp))
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.Center
        ) {

            code.forEach { char ->

                val barHeight = when (char) {
                    in '0'..'3' -> 2.dp
                    in '4'..'6' -> 3.dp
                    else -> 4.dp
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(barHeight)
                        .background(Color.Black)
                )

                Spacer(modifier = Modifier.height(1.dp))
            }
        }

        // Valeur texte
        Text(
            text = code,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}