package com.example.sencsu.data.remote.dto

data class PaiementDto(
    val id: Long? = null,
    val reference: String,
    val montant: Double,
    val modePaiement: String,
    val photoPaiement: String,
    val adherentId: Long
)

