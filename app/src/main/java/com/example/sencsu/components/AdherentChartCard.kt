import android.annotation.TargetApi
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sencsu.data.remote.dto.AdherentDto
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlin.math.roundToInt

// Couleurs cohérentes
private val CardWhite = Color.White
private val BrandBlue = Color(0xFF2563EB)
private val BrandGreen = Color(0xFF10B981)
private val BrandOrange = Color(0xFFF59E0B)
private val BrandRed = Color(0xFFEF4444)
private val DarkText = Color(0xFF111827)
private val SubtleText = Color(0xFF6B7280)
private val SurfaceColor = Color(0xFFF3F4F6)
private val LightGray = Color(0xFFE5E7EB)

enum class ChartType {
    BAR, LINE
}

@Composable
fun AdherentChartCard(adherents: List<AdherentDto>) {
    var selectedChartType by remember { mutableStateOf(ChartType.BAR) }
    var selectedMonth by remember { mutableStateOf<String?>(null) }

    val monthlyData = remember(adherents) { processMonthlyData(adherents) }
    val stats = remember(adherents, monthlyData) { calculateStats(monthlyData) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header avec sélecteur de type de graphique
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Nouveaux Membres",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkText
                    )
                    Text(
                        "6 derniers mois",
                        fontSize = 13.sp,
                        color = SubtleText
                    )
                }

                // Sélecteur de type de graphique
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceColor
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp)
                    ) {
                        ChartTypeButton(
                            icon = Icons.Rounded.Add,
                            isSelected = selectedChartType == ChartType.BAR,
                            onClick = { selectedChartType = ChartType.BAR }
                        )
                        Spacer(Modifier.width(4.dp))
                        ChartTypeButton(
                            icon = Icons.Rounded.Star,
                            isSelected = selectedChartType == ChartType.LINE,
                            onClick = { selectedChartType = ChartType.LINE }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Statistiques
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    icon = Icons.Rounded.AddCircle ,
                    label = "Total",
                    value = stats.total.toString(),
                    color = BrandBlue,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Rounded.Add,
                    label = "Moyenne",
                    value = stats.average.toString(),
                    color = BrandGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Rounded.Star,
                    label = "Record",
                    value = stats.max.toString(),
                    color = BrandOrange,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Graphique
            when (selectedChartType) {
                ChartType.BAR -> AnimatedBarChart(
                    data = monthlyData,
                    selectedMonth = selectedMonth,
                    onMonthSelected = { selectedMonth = if (selectedMonth == it) null else it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
                ChartType.LINE -> AnimatedLineChart(
                    data = monthlyData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            }

            // Détails du mois sélectionné
            selectedMonth?.let { month ->
                Spacer(Modifier.height(16.dp))
                MonthDetailCard(
                    month = month,
                    count = monthlyData[month] ?: 0,
                    onDismiss = { selectedMonth = null }
                )
            }
        }
    }
}

@Composable
private fun ChartTypeButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(36.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) BrandBlue else Color.Transparent
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color.White else SubtleText,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.2f),
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.padding(6.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DarkText
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = SubtleText
            )
        }
    }
}

@Composable
private fun MonthDetailCard(
    month: String,
    count: Int,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = BrandBlue.copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = BrandBlue.copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = month,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Text(
                        text = "$count nouveau${if (count > 1) "x" else ""} membre${if (count > 1) "s" else ""}",
                        fontSize = 13.sp,
                        color = SubtleText
                    )
                }
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Fermer",
                    tint = SubtleText
                )
            }
        }
    }
}

@Composable
fun AnimatedBarChart(
    data: Map<String, Int>,
    selectedMonth: String?,
    onMonthSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val maxValue = data.values.maxOrNull() ?: 1
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress = animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "barAnimation"
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val barWidth = size.width / (data.size * 2f)
                val spacing = barWidth / 2f

                data.values.forEachIndexed { index, value ->
                    val animatedHeight = (value.toFloat() / maxValue) * size.height * 0.85f * animatedProgress.value
                    val x = spacing + (index * (barWidth + spacing))

                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                if (data.keys.elementAt(index) == selectedMonth) BrandOrange else BrandBlue,
                                if (data.keys.elementAt(index) == selectedMonth) BrandOrange.copy(alpha = 0.7f) else BrandBlue.copy(alpha = 0.7f)
                            )
                        ),
                        topLeft = Offset(x, size.height - animatedHeight),
                        size = Size(barWidth, animatedHeight),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                data.entries.forEachIndexed { index, (month, value) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onMonthSelected(month) },
                        contentAlignment = Alignment.TopCenter
                    ) {
                        if (value > 0 && animatedProgress.value > 0.5f) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = (if (month == selectedMonth) BrandOrange else BrandBlue).copy(alpha = 0.15f),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = value.toString(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (month == selectedMonth) BrandOrange else BrandBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.keys.forEach { month ->
                Text(
                    text = month,
                    fontSize = 11.sp,
                    color = if (month == selectedMonth) BrandOrange else SubtleText,
                    fontWeight = if (month == selectedMonth) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun AnimatedLineChart(
    data: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val maxValue = data.values.maxOrNull() ?: 1
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress = animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = EaseOutCubic),
        label = "lineAnimation"
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val pointSpacing = size.width / (data.size - 1)
                val points = data.values.mapIndexed { index, value ->
                    val x = index * pointSpacing
                    val y = size.height - (value.toFloat() / maxValue) * size.height * 0.85f
                    Offset(x, y)
                }

                // Dessiner l'aire sous la courbe
                val gradientPath = Path().apply {
                    if (points.isNotEmpty()) {
                        moveTo(points.first().x, size.height)
                        points.take((points.size * animatedProgress.value).toInt().coerceAtLeast(1)).forEach { point ->
                            lineTo(point.x, point.y)
                        }
                        val lastPoint = points[(points.size * animatedProgress.value).toInt().coerceAtMost(points.size - 1)]
                        lineTo(lastPoint.x, size.height)
                        close()
                    }
                }

                drawPath(
                    path = gradientPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            BrandBlue.copy(alpha = 0.3f),
                            BrandBlue.copy(alpha = 0.05f)
                        )
                    )
                )

                // Dessiner la ligne
                if (points.size > 1) {
                    val linePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        points.take((points.size * animatedProgress.value).toInt().coerceAtLeast(1)).forEach { point ->
                            lineTo(point.x, point.y)
                        }
                    }

                    drawPath(
                        path = linePath,
                        color = BrandBlue,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                // Dessiner les points
                points.take((points.size * animatedProgress.value).toInt()).forEach { point ->
                    drawCircle(
                        color = Color.White,
                        radius = 6.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = BrandBlue,
                        radius = 4.dp.toPx(),
                        center = point
                    )
                }
            }

            // Valeurs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                data.values.forEachIndexed { index, value ->
                    Box(
                        modifier = Modifier.fillMaxHeight(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        if (value > 0 && index < (data.size * animatedProgress.value).toInt()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BrandBlue.copy(alpha = 0.15f),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = value.toString(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.keys.forEach { month ->
                Text(
                    text = month,
                    fontSize = 11.sp,
                    color = SubtleText,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

data class ChartStats(
    val total: Int,
    val average: Int,
    val max: Int,
    val min: Int
)

fun calculateStats(data: Map<String, Int>): ChartStats {
    val values = data.values
    return ChartStats(
        total = values.sum(),
        average = if (values.isNotEmpty()) (values.sum() / values.size.toFloat()).roundToInt() else 0,
        max = values.maxOrNull() ?: 0,
        min = values.minOrNull() ?: 0
    )
}

@TargetApi(Build.VERSION_CODES.O)
fun processMonthlyData(adherents: List<AdherentDto>): Map<String, Int> {
    val now = LocalDate.now()
    val last6Months = (0..5).map { now.minusMonths(it.toLong()) }.reversed()

    val monthlyCount = mutableMapOf<YearMonth, Int>()
    last6Months.forEach { date ->
        monthlyCount[YearMonth.from(date)] = 0
    }

    adherents.forEach { adherent ->
        try {
            val dateString = adherent.createdAt ?: ""
            if (dateString.isNotBlank()) {
                val date = parseDate(dateString)
                val yearMonth = YearMonth.from(date)
                if (monthlyCount.containsKey(yearMonth)) {
                    monthlyCount[yearMonth] = monthlyCount[yearMonth]!! + 1
                }
            }
        } catch (e: Exception) {
            // Ignorer les dates invalides
        }
    }

    return monthlyCount.entries
        .sortedBy { it.key }
        .associate { (yearMonth, count) ->
            val monthName = yearMonth.month.getDisplayName(TextStyle.SHORT, Locale.FRENCH)
                .replaceFirstChar { it.uppercase() }
                .take(3)
            monthName to count
        }
}

@TargetApi(Build.VERSION_CODES.O)
fun parseDate(dateString: String): LocalDate {
    val formatters = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy")
    )

    for (formatter in formatters) {
        try {
            return LocalDate.parse(dateString, formatter)
        } catch (e: Exception) {
            continue
        }
    }

    return LocalDate.now()
}