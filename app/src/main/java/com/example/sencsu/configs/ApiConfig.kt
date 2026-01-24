package com.example.sencsu.configs

object ApiConfig {
//   const val BASE_URL = "http://192.168.1.7:8080/api/"
    const val BASE_URL = "http://10.0.2.2:8080/api/"

    private const val FILES_ENDPOINT = "files/"
    const val IMAGE_BASE_URL = "$BASE_URL$FILES_ENDPOINT"

    fun getImageUrl(filename: String?): String? {
        if (filename.isNullOrBlank()) {
            return null
        }
        return IMAGE_BASE_URL + filename
    }
}
