package com.example.urbanfix.network

// Data to SEND to the server
data class LoginRequest(
    val email: String,
    val contrasena: String // Key must match your Flask API XD XD
)

data class CreateUserRequest( //TODO: Si vamos a crear entonces usuarios de empresas esto se cambia
    val nombre: String,
    val email: String,
    val contrasena: String
)

data class UserData(
    val id: Int,
    val nombre: String,
    val email: String,
    val entidad_nombre: String?,
    val telefono: String?,
    val fecha_registro: String
)
// Data you EXPECT TO RECEIVE on a successful login
data class LoginResponse(
    val message: String,
    val role: String,
    val user_data: UserData
)

data class CreateFuncionarioRequest(
    val nombre: String,
    val email: String,
    val contrasena: String,
    val entidad_id: Int
)

data class UpdateUserRequest(
    val nombre: String,
    val contrasena: String? // Opcional
)

data class CreateReporteRequest(
    val descripcion: String, //Requerido
    val direccion: String, //Requerido
    val referencia: String, //Requerido
    val img_prueba_1: String, //Requeridxo. Manden el bitmap.
    val img_prueba_2: String,
    val latitud: Float, //Requerido
    val longitud: Float, //Requerido
    val usuario_creador_id: String, //Requerido
    val categoria_id: Int, //Requerido.
    val tipo_evento: String //Requerido
)

data class MiReporte(
    val id: Int,
    val nombre: String?,
    val img_prueba_1: String,
    val img_prueba_2: String?,
    val fecha_creacion: String,
    val direccion: String,
    val referencia: String,
    val latitud: String,
    val longitud: String,
    val estado: String?,
    val categoria_nombre: String?,
    val categoria_id: Int,
    val usuario_creador_id: Int,
    val funcionario_asignado_id: Int?,
    val apoyos_count: Int,
    val desapoyos_count: Int,
    val current_user_reaction: String?,
    val descripcion: String?
)

data class FotoPerfilRequest(
    val foto_base64: String
)

data class ReactionRequest(
    val usuario_id: Int,
    val tipo: String // "like" or "dislike"
)

data class ReactionRemoveRequest(
    val usuario_id: Int
)
// Data you EXPECT TO RECEIVE on a failed login
data class ErrorResponse(
    val message: String
)