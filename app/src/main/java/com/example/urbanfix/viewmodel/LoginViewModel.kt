package com.example.urbanfix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbanfix.network.ErrorResponse
import com.example.urbanfix.network.LoginRequest
import com.example.urbanfix.network.LoginResponse
import com.example.urbanfix.network.RetrofitInstance
import com.example.urbanfix.network.UserData
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


sealed interface LoginState {
    object Idle : LoginState
    object Loading : LoginState
    data class Success(val role: String, val userData: UserData) : LoginState
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
        if (email.value.isBlank() || password.value.isBlank()) {
            _loginState.value = LoginState.Error("Debes ingresar tus credenciales completas")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = RetrofitInstance.api.login(
                    LoginRequest(email = email.value, contrasena = password.value)
                )

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        _loginState.value = LoginState.Success(loginResponse.role, loginResponse.user_data)
                    } else {
                        _loginState.value = LoginState.Error("Respuesta vacía del servidor.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = errorBody?.let {
                        Gson().fromJson(it, ErrorResponse::class.java).message
                    } ?: "Credenciales o endpoint incorrectos (Error ${response.code()})"
                    _loginState.value = LoginState.Error(errorMessage)
                }

            } catch (e: Exception) {
                _loginState.value = LoginState.Error("No se pudo conectar al servidor: ${e.message}")
            }
        }
    }

    fun dismissError() {
        _loginState.value = LoginState.Idle
    }
}