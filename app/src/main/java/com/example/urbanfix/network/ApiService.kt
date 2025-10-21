package com.example.urbanfix.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Path
interface ApiService {
    @POST("/login") // The path of your login endpoint
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/usuarios")
    suspend fun createuser (@Body request: CreateUserRequest): Response<ErrorResponse> // TEMPORAL  REPONSE PQ SOLO TIENE MENSAJE

    @POST("/funcionarios")
    suspend fun createFuncionario(@Body request: CreateFuncionarioRequest): Response<Unit>

    @PUT("/{role}/{userId}")
    suspend fun updateUser(
        @Path("role") role: String,
        @Path("userId") userId: Int,
        @Body request: UpdateUserRequest
    ): Response<Unit>

    @DELETE("/{role}/{userId}")
    suspend fun deleteUser(
        @Path("role") role: String,
        @Path("userId") userId: Int
    ): Response<Unit>

    @POST("/reportes")
    suspend fun createReporte(@Body request: CreateReporteRequest): Response<Unit>
}