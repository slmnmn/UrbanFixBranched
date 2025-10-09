package com.example.urbanfix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Definimos todos los posibles estados de esta pantalla
sealed interface ForgotPasswordState {
    object EnterEmail : ForgotPasswordState // Estado inicial para pedir el e-mail
    object Loading : ForgotPasswordState
    object EmailSentSuccess : ForgotPasswordState // Estado para mostrar el diálogo de éxito
    data class Error(val message: String) : ForgotPasswordState
    object EnterCode : ForgotPasswordState // Estado para pedir el código de verificación
}

class OlvidarconViewModel : ViewModel() {
    val email = MutableStateFlow("")
    val verificationCode = MutableStateFlow("")

    private val _state = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.EnterEmail)
    val state = _state.asStateFlow()

    fun onEmailChange(newEmail: String) { email.value = newEmail }
    fun onCodeChange(newCode: String) { verificationCode.value = newCode }

    // Función para enviar el código de recuperación
    fun sendRecoveryCode() {
        viewModelScope.launch {
            if (email.value.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.value).matches()) {
                _state.value = ForgotPasswordState.Error("Debes escribir un e-mail válido, intenta otra vez")
                return@launch
            }
            _state.value = ForgotPasswordState.Loading
            delay(1500) // Simula la llamada a la API
            _state.value = ForgotPasswordState.EmailSentSuccess
        }
    }

    // Función para verificar el código
    fun verifyCode() {
        viewModelScope.launch {
            if (verificationCode.value.length < 5) { // Simula una validación
                _state.value = ForgotPasswordState.Error("El código es incorrecto")
                return@launch
            }
            _state.value = ForgotPasswordState.Loading
            delay(1500) // Simula la llamada a la API
            // Aquí, si el código es correcto, navegarías a la pantalla de "Nueva Contraseña"
            // Por ahora, lo dejamos pendiente.
        }
    }

    // Función para cerrar los diálogos
    fun dismissDialog() {
        if (_state.value is ForgotPasswordState.EmailSentSuccess) {
            _state.value = ForgotPasswordState.EnterCode // Después del éxito, pasamos a pedir el código
        } else {
            _state.value = ForgotPasswordState.EnterEmail // Si hay error, volvemos a pedir el email
        }
    }
}