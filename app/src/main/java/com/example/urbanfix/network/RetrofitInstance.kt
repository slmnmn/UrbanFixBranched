package com.example.urbanfix.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val BASE_URL = "http://192.168.2.7:5000" //


    // --- 1. CREA UN OKHTTPCLIENT CON TIMEOUTS MÁS LARGOS --- // Crear reporte demora mucho
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            // Tiempo para establecer la conexión inicial
            .connectTimeout(60, TimeUnit.SECONDS)
            // Tiempo máximo esperando datos después de conectar
            .readTimeout(60, TimeUnit.SECONDS)
            // Tiempo máximo enviando datos
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    // --- 2. PASA EL OKHTTPCLIENT A RETROFIT ---
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // Añade el cliente configurado
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}