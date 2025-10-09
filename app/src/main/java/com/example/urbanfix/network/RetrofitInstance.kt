package com.example.urbanfix.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // ❗ IMPORTANTE: Cambia esto por la IP de tu computadora.
    // NO uses "localhost" o "127.0.0.1".
    private const val BASE_URL = "https://59c19f308c7b.ngrok-free.app/" //

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}