package com.example.urbanfix.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.HTTP

interface ApiService {
    @POST("/login") // The path of your login endpoint
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/usuarios")
    suspend fun createuser(@Body request: CreateUserRequest): Response<ErrorResponse> // TEMPORAL RESPONSE PQ SOLO TIENE MENSAJE

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

    @GET("misreportes")
    suspend fun getMisReportes(
        @Query("user_id") userId: Int
    ): List<MiReporte>

    @DELETE("reportes/{id}")
    suspend fun deleteReporte(
        @Path("id") reporteId: Int
    ): Response<Unit>

    @POST("/usuarios/{id}/foto_perfil")
    suspend fun subirFotoUsuario(
        @Path("id") userId: Int,
        @Body request: FotoPerfilRequest
    ): Response<Unit>

    @POST("/funcionarios/{id}/foto_perfil")
    suspend fun subirFotoFuncionario(
        @Path("id") funcionarioId: Int,
        @Body request: FotoPerfilRequest
    ): Response<Unit>

    @GET("/reportes/{id}")
    suspend fun getReporteById(
        @Path("id") reporteId: Int,
        @Query("user_id") userId: Int?
    ): Response<MiReporte> // Use Response<>

    //User-Specific Report Lists ---
    @GET("/usuarios/{id}/denuncias")
    suspend fun getUserDenuncias(
        @Path("id") userId: Int
    ): Response<List<MiReporte>> // Use Response<>

    @GET("/usuarios/{id}/apoyos")
    suspend fun getUserApoyos(
        @Path("id") userId: Int
    ): Response<List<MiReporte>> // Use Response<>

    //Like/Dislike (Reaction) Endpoints
    @POST("/reportes/{id}/reaccion")
    suspend fun setReaccion(
        @Path("id") reporteId: Int,
        @Body request: ReactionRequest
    ): Response<Unit>

    @HTTP(method = "DELETE", path = "/reportes/{id}/reaccion", hasBody = true)
    suspend fun removeReaccion(
        @Path("id") reporteId: Int,
        @Body request: ReactionRemoveRequest //
    ): Response<Unit>
}
