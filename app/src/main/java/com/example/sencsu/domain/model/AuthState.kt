
package com.example.sencsu.domain.model

import com.example.sencsu.data.remote.dto.AgentDto

data class AuthState(
    val isLoading: Boolean = true,
    val user: AgentDto? = null,
    val error: String? = null
)
