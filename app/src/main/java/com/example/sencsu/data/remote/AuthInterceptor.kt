package com.example.sencsu.data.remote

import com.example.sencsu.data.repository.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            sessionManager.tokenFlow.first()
        }

        val request = chain.request().newBuilder()
        if (!token.isNullOrBlank()) {
            request.addHeader("Authorization", "Bearer $token")
        }

        val response = chain.proceed(request.build())

        // Vérifier si le token a expiré (erreur 401)
        if (response.code == 401) {
            runBlocking {
                sessionManager.clearSessionAndNotify()
            }
        }

        return response
    }
}
