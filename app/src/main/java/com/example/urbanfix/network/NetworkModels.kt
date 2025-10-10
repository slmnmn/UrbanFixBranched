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
    val telefono: String?
)
// Data you EXPECT TO RECEIVE on a successful login
data class LoginResponse(
    val message: String,
    val role: String,
    val user_data: String
)

// Data you EXPECT TO RECEIVE on a failed login
data class ErrorResponse(
    val message: String
)