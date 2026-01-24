package com.example.sencsu.utils

import android.annotation.TargetApi
import android.os.Build
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


object Formatters {
    @TargetApi(Build.VERSION_CODES.O)
    fun formatMillisToDate(millis: Long): String {
        // On utilise UTC pour éviter les décalages de date (le jour qui change selon l'heure)
        val date = Instant.ofEpochMilli(millis)
            .atZone(ZoneId.of("UTC"))
            .toLocalDate()

        // Utilisation d'un DateTimeFormatter (plus moderne et robuste que .format)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return date.format(formatter)
    }

    @TargetApi(Build.VERSION_CODES.O)
    fun formatDateForApi(dateUi: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE // yyyy-MM-dd

        val localDate = LocalDate.parse(dateUi, inputFormatter)
        return localDate.format(outputFormatter)
    }
    fun formatPhoneNumber(number: String): String {
        val cleaned = number.replace("\\s".toRegex(), "")
        val limited = cleaned.take(10)
        val formatted = StringBuilder()

        for (i in limited.indices) {
            if (i == 2 || i == 5 || i == 7) formatted.append(' ')
            formatted.append(limited[i])
        }

        return formatted.toString()
    }

    fun formatDate(text: String): String {
        val cleaned = text.replace("[^0-9]".toRegex(), "")
        val formatted = StringBuilder()

        for (i in cleaned.indices.take(8)) {
            if (i == 2 || i == 4) formatted.append('/')
            formatted.append(cleaned[i])
        }

        return formatted.toString()
    }

    @TargetApi(Build.VERSION_CODES.O)
    fun isValidDate(dateStr: String): Boolean {
        if (!Regex("^\\d{2}/\\d{2}/\\d{4}$").matches(dateStr)) return false

        val parts = dateStr.split('/')
        if (parts.size != 3) return false

        val day = parts[0].toIntOrNull() ?: return false
        val month = parts[1].toIntOrNull() ?: return false
        val year = parts[2].toIntOrNull() ?: return false

        // Validation de base
        if (day < 1 || day > 31) return false
        if (month < 1 || month > 12) return false
        if (year < 1900 || year > java.time.LocalDate.now().year) return false

        // Validation des mois de 30 jours
        val months30 = listOf(4, 6, 9, 11)
        if (months30.contains(month) && day > 30) return false

        // Validation de février
        if (month == 2) {
            val isLeapYear = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
            if (isLeapYear && day > 29) return false
            if (!isLeapYear && day > 28) return false
        }

        return true
    }

    fun formatDateForAPI(dateString: String): String {
        val parts = dateString.split('/')
        return if (parts.size == 3) {
            val year = parts[2]
            val month = parts[1].padStart(2, '0')
            val day = parts[0].padStart(2, '0')
            "$year-$month-$day"
        } else {
            dateString
        }
    }
}



fun Int.toLocaleString(): String {
    return NumberFormat.getNumberInstance(Locale.FRENCH).format(this)
}