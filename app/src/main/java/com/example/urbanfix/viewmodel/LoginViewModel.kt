package com.example.urbanfix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbanfix.network.ErrorResponse
import com.example.urbanfix.network.LoginRequest
import com.example.urbanfix.network.RetrofitInstance
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LoginState {
    object Idle : LoginState
    object Loading : LoginState
    object Success : LoginState
    data class Error(val message: String) : LoginState
}

class LoginViewModel : ViewModel() {
    val email = MutableStateFlow("")
    val password = MutableStateFlow("")

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState = _loginState.asStateFlow()

    fun onEmailChange(newEmail: String) { email.value = newEmail }
    fun onPasswordChange(newPassword: String) { password.value = newPassword }

    fun loginUser() {
        // 👇 AÑADE ESTA LÍNEA
        println("LOGIN_DEBUG: Función loginUser() en ViewModel iniciada.")

        if (email.value.isBlank() || password.value.isBlank()) {
            _loginState.value = LoginState.Error("Debes ingresar tus credenciales completas")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = RetrofitInstance.api.login(
                    LoginRequest(email = email.value, contrasena_hash = password.value)
                )

                // 👇 AÑADE ESTOS LOGS PARA VER LA RESPUESTA
                println("LOGIN_DEBUG: Respuesta recibida del servidor. Código: ${response.code()}")

                if (response.isSuccessful) {
                    println("LOGIN_DEBUG: Login exitoso. Body: ${response.body()}")
                    _loginState.value = LoginState.Success
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("LOGIN_DEBUG: Login fallido. Código de error: ${response.code()}. Body de error: $errorBody")

                    val errorMessage = errorBody?.let {
                        Gson().fromJson(it, ErrorResponse::class.java).message
                    } ?: "Credenciales o endpoint incorrectos (Error ${response.code()})"

                    _loginState.value = LoginState.Error(errorMessage)
                }

            } catch (e: Exception) {
                println("API_ERROR: Fallo la conexión con la excepción: ${e.message}")
                _loginState.value = LoginState.Error("No se pudo conectar al servidor.")
            }
        }
    }

    fun dismissError() {
        _loginState.value = LoginState.Idle
    }
}

