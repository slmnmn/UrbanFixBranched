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
import retrofit2.http.Header
import com.google.gson.JsonObject
interface ApiService {
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/usuarios")
    suspend fun createuser(@Body request: CreateUserRequest): Response<ErrorResponse>

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

    // MÉTODO ORIGINAL - con parámetros opcionales para consulta detallada
    @GET("/reportes/{id}")
    suspend fun getReporteById(
        @Path("id") reporteId: Int,
        @Query("user_id") userId: Int? = null,
        @Header("User-Role") userRole: String = "usuario"
    ): Response<MiReporte>

    //User-Specific Report Lists
    @GET("/usuarios/{id}/denuncias")
    suspend fun getUserDenuncias(
        @Path("id") userId: Int
    ): Response<List<MiReporte>>

    @GET("/usuarios/{id}/apoyos")
    suspend fun getUserApoyos(
        @Path("id") userId: Int
    ): Response<List<MiReporte>>

    //Like/Dislike (Reaction) Endpoints
    @POST("/reportes/{id}/reaccion")
    suspend fun setReaccion(
        @Path("id") reporteId: Int,
        @Body request: ReactionRequest
    ): Response<Unit>

    @GET("/funcionarios/{id}/apoyos")
    suspend fun getFuncionarioApoyos(
        @Path("id") funcId: Int
    ): Response<List<MiReporte>>

    @GET("/funcionarios/{id}/denuncias")
    suspend fun getFuncionarioDenuncias(
        @Path("id") funcId: Int
    ): Response<List<MiReporte>>

    @HTTP(method = "DELETE", path = "/reportes/{id}/reaccion", hasBody = true)
    suspend fun removeReaccion(
        @Path("id") reporteId: Int,
        @Body request: ReactionRemoveRequest
    ): Response<Unit>

    @GET("/reportes/{id}/comentarios")
    suspend fun getComentarios(
        @Path("id") reporteId: Int
    ): Response<List<ComentarioResponse>>

    @POST("/reportes/{id}/comentarios")
    suspend fun postComentario(
        @Path("id") reporteId: Int,
        @Body request: ComentarioRequest
    ): Response<ComentarioResponse>

    @PUT("/comentarios/{id}")
    suspend fun updateComentario(
        @Path("id") comentarioId: Int,
        @Body request: ComentarioUpdateRequest
    ): Response<ComentarioResponse>

    @DELETE("/comentarios/{id}")
    suspend fun deleteComentario(
        @Path("id") comentarioId: Int
    ): Response<Unit>

    @GET("/usuarios/{userId}/perfil")
    suspend fun getOtherUserProfile(
        @Path("userId") userId: Int
    ): Response<OtherUserProfileResponse>

    @GET("/reportes/buscar/{codigo}")
    suspend fun buscarReportePorCodigo(
        @Path("codigo") codigo: String,
        @Query("user_id") userId: Int?,
        @Header("User-Role") userRole: String
    ): Response<MiReporte>

    @GET("/funcionarios/{userId}/perfil")
    suspend fun getOtherFuncionarioProfile(
        @Path("userId") userId: Int
    ): Response<OtherUserProfileResponse>

    @PUT("/reportes/{id}/estado")
    suspend fun updateReporteEstado(
        @Path("id") reporteId: Int,
        @Header("User-Role") userRole: String, // Para la autorización
        @Header("User-Id") userId: Int,
        @Body request: UpdateEstadoRequest
    ): Response<MiReporte>

    @GET("/reportes/geojson")
    suspend fun getReportesGeoJson(): Response<JsonObject>

    @GET("/reportes")
    suspend fun getReportes(
        @Query("user_id") userId: Int?
    ): Response<List<MiReporte>>
}

