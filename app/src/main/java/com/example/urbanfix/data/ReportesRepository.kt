package com.example.urbanfix.data // O 'repository'

import com.example.urbanfix.network.ApiService
import com.google.gson.JsonObject
import retrofit2.Response


class ReportesRepository(private val apiService: ApiService) {

    suspend fun getReportesGeoJson(): Response<JsonObject> {
        return apiService.getReportesGeoJson()
    }
}