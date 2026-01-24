// utils/Result.kt
package com.example.sencsu.utils

sealed class Result<out T> {
    data class Success<out T>(val value: T) : Result<T>()
    data class Failure(val exception: Throwable) : Result<Nothing>()

    // Ajoutez la méthode fold
    fun <R> fold(
        onSuccess: (T) -> R,
        onFailure: (Throwable) -> R
    ): R = when (this) {
        is Success -> onSuccess(value)
        is Failure -> onFailure(exception)
    }

    // Méthode getOrNull pour récupérer la valeur ou null
    fun getOrNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }

    // Méthode exceptionOrNull pour récupérer l'exception ou null
    fun exceptionOrNull(): Throwable? = when (this) {
        is Success -> null
        is Failure -> exception
    }
}

// Fonctions d'extension pour utiliser comme .onSuccess et .onFailure
fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(value)
    return this
}

fun <T> Result<T>.onFailure(action: (Throwable) -> Unit): Result<T> {
    if (this is Result.Failure) action(exception)
    return this
}