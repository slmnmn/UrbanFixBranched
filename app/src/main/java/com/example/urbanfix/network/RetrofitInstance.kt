package com.example.urbanfix.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // ❗ IMPORTANTE: Cambia esto por la IP de tu computadora.
    // NO uses "localhost" o "127.0.0.1".
    private const val BASE_URL = "http://10.0.2.2:5000" //

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}