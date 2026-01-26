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

    fun updateReference(ref: String) {
        uiState = uiState.copy(reference = ref)
    }

    fun updateMode(mode: String) {
        uiState = uiState.copy(modePaiement = mode)
    }

    fun updateMontant(montant: String) {
        uiState = uiState.copy(montantTotal = montant)
    }

    fun setPhoto(uri: Uri?) {
        uiState = uiState.copy(photoPaiement = uri)
    }

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

    private fun extractReferencePattern(text: String): String {
        // Normalise le texte EN REMPLAÇANT les sauts de ligne par des espaces
        // Cela permet "ID de\ntransaction" de devenir "ID de transaction"
        val flat = text.replace("\n", " ")
            .replace("\r", " ")
            .replace(Regex("\\s+"), " ") // Réduit les espaces multiples à un seul
            .trim()

        // DEBUG : Affiche le texte OCR complet dans les logs
        android.util.Log.d("OCR_DEBUG", "Texte OCR détecté: $flat")

        // STRATÉGIE 1 : Cherche "ID de transaction" suivi du code (peut être coupé en plusieurs lignes)
        // Le code peut être sur la même ligne ou la ligne suivante
        val regex1 = Regex(
            """(?i)\bid\s+de\s+transaction\s+([A-Za-z0-9]+(?:\s+[A-Za-z0-9]+)?)""",
            RegexOption.IGNORE_CASE
        )

        val match1 = regex1.find(flat)
        if (match1 != null) {
            var code = match1.groupValues[1].trim()
                .replace(Regex("\\s+"), "") // Supprime tous les espaces du code
            android.util.Log.d("OCR_DEBUG", "Match regex1 trouvé: $code")

            // Prend seulement les 15-25 premiers caractères
            if (code.length > 25) {
                code = code.substring(0, 25)
            }

            if (isValidTransactionCode(code)) {
                android.util.Log.d("OCR_DEBUG", "Code validé: $code")
                return code
            }
        }

        // STRATÉGIE 2 : Cherche avec des caractères spéciaux ou deux-points entre
        val regex2 = Regex(
            """(?i)\bid\s+de\s+transaction\s*[:\-]?\s*([A-Za-z0-9]+)""",
            RegexOption.IGNORE_CASE
        )

        val match2 = regex2.find(flat)
        if (match2 != null) {
            var code = match2.groupValues[1].trim()
            android.util.Log.d("OCR_DEBUG", "Match regex2 trouvé: $code")

            if (code.length > 25) {
                code = code.substring(0, 25)
            }

            if (isValidTransactionCode(code)) {
                android.util.Log.d("OCR_DEBUG", "Code validé (stratégie 2): $code")
                return code
            }
        }

        // STRATÉGIE 3 : Cherche juste le label "transaction" (au cas où "ID de" est vraiment séparé)
        val regex3 = Regex(
            """(?i)\btransaction\s+([A-Za-z0-9]+)""",
            RegexOption.IGNORE_CASE
        )

        val match3 = regex3.find(flat)
        if (match3 != null) {
            var code = match3.groupValues[1].trim()
            android.util.Log.d("OCR_DEBUG", "Match regex3 trouvé: $code")

            if (code.length > 25) {
                code = code.substring(0, 25)
            }

            if (isValidTransactionCode(code)) {
                android.util.Log.d("OCR_DEBUG", "Code validé (stratégie 3): $code")
                return code
            }
        }

        // STRATÉGIE 4 : Fallback - cherche un code sans label
        val regex4 = Regex("""([A-Za-z0-9]{15,25})""")
        val allMatches = regex4.findAll(flat)
        for (match in allMatches) {
            val code = match.value.trim()
            android.util.Log.d("OCR_DEBUG", "Code candidat: $code")
            if (isValidTransactionCode(code)) {
                android.util.Log.d("OCR_DEBUG", "Code validé (stratégie 4): $code")
                return code
            }
        }

        android.util.Log.d("OCR_DEBUG", "Aucun code trouvé")
        return ""
    }

    private fun isValidTransactionCode(code: String): Boolean {
        // Rejette les codes qui sont clairement du texte français
        val invalidPatterns = listOf(
            "PAIEMENT",
            "STATUT",
            "EFFECTUE",
            "FRAIS",
            "MONTANT",
            "REFERENCE",
            "DATE",
            "HEURE",
            "PARTENARIAT",
            "DIGITAL",
            "FINANCE",
            "WAVE",
            "ORANGE"
        )

        // Vérifie que le code n'est pas composé SEULEMENT de lettres
        if (!code.any { it.isDigit() }) {
            android.util.Log.d("OCR_DEBUG", "Rejeté (pas de chiffres): $code")
            return false
        }

        // Vérifie que le code a une longueur raisonnable
        if (code.length < 15 || code.length > 25) {
            android.util.Log.d("OCR_DEBUG", "Rejeté (longueur): $code (${code.length} caractères)")
            return false
        }

        // Vérifie que le code ne commence pas par un mot français
        for (pattern in invalidPatterns) {
            if (code.startsWith(pattern, ignoreCase = true)) {
                android.util.Log.d("OCR_DEBUG", "Rejeté (commence par '$pattern'): $code")
                return false
            }
        }

        return true
    }
}