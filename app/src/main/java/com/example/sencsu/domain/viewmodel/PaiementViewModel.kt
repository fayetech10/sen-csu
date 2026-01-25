package com.example.sencsu.domain.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.sencsu.screen.PaiementFormState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer
import javax.inject.Inject

class PaiementViewModel @Inject constructor() : ViewModel() {
    var uiState by mutableStateOf(PaiementFormState())
        private set

    fun updateReference(ref: String) { uiState = uiState.copy(reference = ref) }
    fun updateMode(mode: String) { uiState = uiState.copy(modePaiement = mode) }
    fun updateMontant(montant: String) { uiState = uiState.copy(montantTotal = montant) }
    fun setPhoto(uri: Uri?) { uiState = uiState.copy(photoPaiement = uri) }

    fun processImage(uri: Uri, context: Context, recognizer: TextRecognizer) {
        uiState = uiState.copy(isLoading = true)

        val image: InputImage
        try {
            image = InputImage.fromFilePath(context, uri)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val detectedRef = extractReferencePattern(visionText.text)
                    uiState = uiState.copy(reference = detectedRef, isLoading = false)
                }
                .addOnFailureListener {
                    uiState = uiState.copy(isLoading = false, errorMessage = "Erreur de lecture")
                }
        } catch (e: Exception) {
            uiState = uiState.copy(isLoading = false, errorMessage = "Fichier invalide")
        }
    }

//    private fun extractReferencePattern(text: String): String {
//        // 1. Astuce importante : On remplace les sauts de ligne par des espaces.
//        // Cela permet de lire "ID de transaction TL7..." comme une seule phrase,
//        // même si l'OCR les voit sur des lignes différentes.
//        val flatText = text.replace("\n", " ").replace("\r", " ")
//
//        val patterns = listOf(
//            // PRIORITÉ 1 : Le format exact de ta photo (Wave)
//            // Cherche "ID de transaction" (insensible à la casse) suivi d'espaces, puis capture le code
//            """(?i)id\s+de\s+transaction\s*[:\s]*([A-Z0-9]+)""".toRegex(),
//
//            // PRIORITÉ 2 : Autres formats courants (Orange Money utilise souvent "Trans ID")
//            """(?i)(?:trans|transaction)\s*id\s*[:\s]*([A-Z0-9]+)""".toRegex(),
//
//            // PRIORITÉ 3 : Fallback (Si on ne trouve pas le label, on cherche juste un long code)
//            // Les codes Wave/OM font généralement au moins 10 caractères majuscules/chiffres
//            """([A-Z0-9]{10,})""".toRegex()
//        )
//
//        for (pattern in patterns) {
//            val match = pattern.find(flatText)
//            if (match != null) {
//                // On retourne le groupe 1 (la partie entre parenthèses, c'est-à-dire le code)
//                return match.groupValues.getOrNull(1) ?: ""
//            }
//        }
//        return ""
//    }
    private fun extractReferencePattern(text: String): String {
        // Diviser le texte en lignes
        val lines = text.lines()

        // Chercher la ligne qui contient "ID de transaction"
        for (i in lines.indices) {
            val line = lines[i].trim()

            // Si cette ligne contient "ID de transaction"
            if (line.contains("ID de transaction", ignoreCase = true) ||
                line.contains("Transaction ID", ignoreCase = true) ||
                line.contains("TXN", ignoreCase = true)) {

                // Option 1: Le code peut être sur la même ligne après ":"
                val parts = line.split(":", " ")
                for (part in parts) {
                    val trimmed = part.trim()
                    // Vérifier si c'est un code de transaction (alphanumérique, longueur > 8)
                    if (trimmed.matches(Regex("[A-Z0-9]{8,}"))) {
                        return trimmed
                    }
                }

                // Option 2: Le code peut être sur la ligne suivante
                if (i + 1 < lines.size) {
                    val nextLine = lines[i + 1].trim()
                    // Chercher un code alphanumérique dans la ligne suivante
                    val pattern = Regex("([A-Z0-9]+(?:\\s+[A-Z0-9]+)?)")
                    val match = pattern.find(nextLine)
                    if (match != null) {
                        return match.value.trim()
                    }
                }
            }
        }

        // Fallback: chercher n'importe quel code qui ressemble à un ID de transaction
        val fallbackPatterns = listOf(
            """([A-Z0-9]{8,}\s+[A-Z0-9]{5,})""".toRegex(), // Avec espace
            """([A-Z0-9]{12,})""".toRegex() // Sans espace
        )

        for (pattern in fallbackPatterns) {
            val match = pattern.find(text)
            if (match != null) {
                return match.groupValues.getOrNull(1) ?: match.value
            }
        }

        return ""
    }
}