package com.example.urbanfix.data // O 'repository'

import com.example.urbanfix.network.ApiService
import com.example.urbanfix.network.MiReporte
import com.example.urbanfix.network.ReactionRemoveRequest
import com.example.urbanfix.network.ReactionRequest
import com.google.gson.JsonObject
import retrofit2.Response


class ReportesRepository(private val apiService: ApiService) {

    suspend fun getReportesGeoJson(): Response<JsonObject> {
        return apiService.getReportesGeoJson()
    }

    suspend fun getReportes(userId: Int?): Response<List<MiReporte>> {
        return apiService.getReportes(userId)
    }

    suspend fun setReaccion(reporteId: Int, request: ReactionRequest): Response<Unit> {
        return apiService.setReaccion(reporteId, request)
    }

    suspend fun removeReaccion(reporteId: Int, request: ReactionRemoveRequest): Response<Unit> {
        return apiService.removeReaccion(reporteId, request)
    }
}