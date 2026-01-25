package com.example.sencsu.domain.model

import com.example.sencsu.data.remote.dto.DashboardResponseDto

data class DashboardState(
    val data: DashboardResponseDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
) {
    val isError: Boolean get() = !error.isNullOrEmpty()
    val isEmpty: Boolean get() = data == null && !isLoading && error.isNullOrEmpty()
    val shouldShowLoading: Boolean get() = isLoading && data == null
    val shouldShowError: Boolean get() = isError && !isLoading

    // Propriétés calculées pour faciliter l'accès
    val message: String get() = data?.message ?: ""
    val adherents: List<Any> get() = data?.data ?: emptyList()
    val adherentsCount: Int get() = data?.data?.size ?: 0
}