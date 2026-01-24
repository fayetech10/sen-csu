package com.example.sencsu.data.repository

import com.example.sencsu.data.remote.ApiService
import com.example.sencsu.data.remote.dto.LoginRequest
import com.example.sencsu.data.remote.dto.LoginResponse
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun login(email: String, password: String): LoginResponse {
        val request = LoginRequest(email, password)
        return apiService.login(request)
    }
}