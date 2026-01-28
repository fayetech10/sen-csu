package com.example.sencsu.data.repository

import com.example.sencsu.data.remote.ApiService
import com.example.sencsu.data.remote.dto.CotisationDto
import javax.inject.Inject

class Cotisationepository @Inject constructor(
   private val apiService: ApiService
){
    suspend fun getCotisationByIdahderent(adherentId: Long): List<CotisationDto> {

      return  apiService.getCotisationByAdherentId(adherentId)

    }
}