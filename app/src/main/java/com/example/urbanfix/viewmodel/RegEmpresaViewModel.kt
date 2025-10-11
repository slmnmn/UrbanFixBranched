package com.example.urbanfix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbanfix.network.CreateFuncionarioRequest
import com.example.urbanfix.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RegState {
    object Idle : RegState
    object Loading : RegState
    object Success : RegState
    data class Error(val message: String) : RegState
}

class RegEmpresaViewModel : ViewModel() {
    val email = MutableStateFlow("")
    val entidadId = MutableStateFlow(0)
    val nombres = MutableStateFlow("")
    val apellidos = MutableStateFlow("")
    val password = MutableStateFlow("")
    val confirmPassword = MutableStateFlow("")

    private val _regState = MutableStateFlow<RegState>(RegState.Idle)
    val regState = _regState.asStateFlow()

    fun registerFuncionario() {
        if (nombres.value.isBlank() || apellidos.value.isBlank() || email.value.isBlank() || password.value.isBlank()) {
            _regState.value = RegState.Error("Todos los campos son obligatorios.")
            return
        }
        if (password.value != confirmPassword.value) {
            _regState.value = RegState.Error("Las contraseñas no coinciden.")
            return
        }
        if (entidadId.value == 0) {
            _regState.value = RegState.Error("Debes seleccionar una entidad.")
            return
        }

        viewModelScope.launch {
            _regState.value = RegState.Loading
            try {
                val nombreCompleto = "${nombres.value} ${apellidos.value}"

                val request = CreateFuncionarioRequest(
                    nombre = nombreCompleto,
                    email = email.value,
                    contrasena = password.value,
                    entidad_id = entidadId.value
                )

                val response = RetrofitInstance.api.createFuncionario(request)

                if (response.isSuccessful) {
                    _regState.value = RegState.Success
                } else {
                    _regState.value = RegState.Error("Error al registrar: ${response.message()}")
                }
            } catch (e: Exception) {
                _regState.value = RegState.Error("No se pudo conectar al servidor: ${e.message}")
            }
        }
    }

    fun dismissError() {
        _regState.value = RegState.Idle
    }
}