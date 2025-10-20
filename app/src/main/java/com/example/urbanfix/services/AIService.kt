package com.example.urbanfix.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.HttpURLConnection

object AIService {
    private const val GEMINI_API_KEY = "AIzaSyCBUGAivLRHiworyvsiPH1OO4F6tYpV_2A"
    private const val GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent"

    suspend fun generateDescription(
        address: String,
        referencePoint: String,
        subtype: String,
        reportType: String = "general"
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                val reportName = getReportTypeName(reportType)

                val prompt = """
                    Eres un asistente que redacta reportes de problemas urbanos de forma clara y profesional.
                    
                    INFORMACIÓN DEL REPORTE:
                    - Tipo de problema: $reportName
                    - Subtipo específico: $subtype
                    - Ubicación: $address
                    - Punto de referencia: $referencePoint
                    
                    INSTRUCCIONES IMPORTANTES:
                    1. Redacta la descripción en 1 o 2 oraciones MÁXIMO
                    2. Usa lenguaje simple y directo, evita tecnicismos
                    3. Sé específico sobre qué está dañado o qué falta
                    4. El tono debe ser profesional pero accesible
                    5. NO incluyas fórmulas genéricas como "Se reporta que..." o "Se observa que..."
                    6. Comienza directamente describiendo el problema
                    7. Incluye dónde está exactamente usando el punto de referencia
                    
                    EJEMPLO DE BUEN FORMATO:
                    "Hay un hueco profundo en la calle que ocupa casi todo el ancho de la vía, justo antes de la tienda de la esquina. Es muy peligroso para los carros."
                    
                    Ahora redacta la descripción:
                """.trimIndent()

                val requestBody = JSONObject().apply {
                    put("contents", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", org.json.JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", prompt)
                                })
                            })
                        })
                    })
                }.toString()

                val fullUrl = "$GEMINI_API_URL?key=$GEMINI_API_KEY"
                val url = URL(fullUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                connection.outputStream.use { os ->
                    os.write(requestBody.toByteArray())
                    os.flush()
                }

                val responseCode = connection.responseCode
                return@withContext if (responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    try {
                        val jsonResponse = JSONObject(response)
                        val candidates = jsonResponse.getJSONArray("candidates")
                        if (candidates.length() > 0) {
                            val content = candidates.getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text")
                                .trim()
                            content
                        } else {
                            "No se pudo generar la descripción"
                        }
                    } catch (e: Exception) {
                        "Error procesando respuesta: ${e.message}"
                    }
                } else {
                    val errorStream = connection.errorStream.bufferedReader().use { it.readText() }
                    "Error HTTP $responseCode: $errorStream"
                }

            } catch (e: Exception) {
                e.printStackTrace()
                "Error: ${e.message}"
            }
        }
    }

    private fun getReportTypeName(reportType: String): String {
        return when (reportType) {
            "huecos" -> "Huecos en la vía"
            "alumbrado" -> "Problemas de iluminación"
            "basura" -> "Acumulación de basura"
            "semaforo" -> "Problemas con semáforos"
            "hidrante" -> "Problemas con hidrantes"
            "alcantarilla" -> "Problemas con alcantarillas"
            else -> "Problema urbano"
        }
    }
}