package com.example.urbanfix.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/iniciosesion") // The path of your login endpoint
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
} // TODO: Toda esta interfaz se termina editando para añadir todos los endpoints perros