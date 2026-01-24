package com.example.sencsu.models

import androidx.compose.ui.graphics.Color

// --- Couleurs extraites de l'image ---
val BgColor = Color(0xFFF5F7FA)
val PrimaryBlue = Color(0xFF025205) // Bleu du bouton "Add Member"
val TextBlack = Color(0xFF111827)
val TextGray = Color(0xFF6B7280)

// Couleurs de status/badges
val GreenBg = Color(0xFFDCFCE7)
val GreenText = Color(0xFF166534)
val RedBg = Color(0xFFFEE2E2)
val RedText = Color(0xFF991B1B)
val BlueBg = Color(0xFFDBEAFE)
val BlueText = Color(0xFF1E40AF)
val YellowBg = Color(0xFFFEF3C7) // Pour l'icone monétaire

// --- Modèles de données ---
data class StatItem(
    val title: String,
    val value: String,
    val trend: String,
    val iconRes: androidx.compose.ui.graphics.vector.ImageVector,
    val themeColor: Color, // Couleur principale de l'icone/badge
    val isAlert: Boolean = false // Pour le rouge "High"
)

data class ActivityItem(
    val name: String,
    val action: String,
    val time: String,
    val status: String,
    val statusColor: Color,
    val statusBg: Color,
    val avatarUrl: String // Placeholder pour l'exemple
)