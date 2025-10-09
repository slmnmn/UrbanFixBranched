package com.example.urbanfix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbanfix.network.CreateUserRequest
import com.example.urbanfix.network.ErrorResponse
import com.example.urbanfix.network.RetrofitInstance
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estados para el proceso de registro
sealed interface RegistrationState {
    object Idle : RegistrationState
    object Loading : RegistrationState
    object Success : RegistrationState
    data class Error(val message: String) : RegistrationState
}

class RegistrationViewModel : ViewModel() {

    // StateFlows para cada campo del formulario
    val email = MutableStateFlow("")
    val nombres = MutableStateFlow("")
    val apellidos = MutableStateFlow("")
    val password = MutableStateFlow("")
    val confirmPassword = MutableStateFlow("")

    // StateFlow para el estado general del registro
    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val registrationState = _registrationState.asStateFlow()

    // Funciones para actualizar cada campo desde la UI
    fun onEmailChange(value: String) { email.value = value }
    fun onNombresChange(value: String) { nombres.value = value }
    fun onApellidosChange(value: String) { apellidos.value = value }
    fun onPasswordChange(value: String) { password.value = value }
    fun onConfirmPasswordChange(value: String) { confirmPassword.value = value }

    fun registerUser() {
        //  Validaciones
        if (email.value.isBlank() || nombres.value.isBlank() || apellidos.value.isBlank() || password.value.isBlank() || confirmPassword.value.isBlank()) {
            _registrationState.value = RegistrationState.Error("Debes completar todos los campos")
            return
        }
        if (password.value != confirmPassword.value) {
            _registrationState.value = RegistrationState.Error("Verifica tu contraseña y confirmación.")
            return
        }

        // Iniciar la corutina para la llamada de red
        viewModelScope.launch {
            _registrationState.value = RegistrationState.Loading
            try {
                // Combinamos nombres y apellidos para el request
                val fullName = "${nombres.value} ${apellidos.value}"

                val response = RetrofitInstance.api.createuser(
                    CreateUserRequest(
                        nombre = fullName,
                        email = email.value,
                        contrasena = password.value
                    )
                )

                if (response.isSuccessful) {
                    _registrationState.value = RegistrationState.Success
                } else {
                    val errorMsg = response.errorBody()?.string()?.let {
                        Gson().fromJson(it, ErrorResponse::class.java).message
                    } ?: "Error al crear la cuenta (Código: ${response.code()})"
                    _registrationState.value = RegistrationState.Error(errorMsg)
                }

            } catch (e: Exception) {
                _registrationState.value = RegistrationState.Error("No se pudo conectar al servidor. Inténtalo de nuevo.")
                println("REGISTRATION_ERROR: ${e.message}")
            }
        }
    }

    // Función para resetear el estado (ej. al cerrar un diálogo de error)
    fun dismissDialog() {
        _registrationState.value = RegistrationState.Idle
    }
}