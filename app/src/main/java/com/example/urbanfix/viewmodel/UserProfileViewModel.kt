package com.example.urbanfix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estado para la UI del perfil, similar a LoginState
sealed interface UserProfileState {
    object Idle : UserProfileState
    object Loading : UserProfileState
    data class Success(val userName: String, val userEmail: String) : UserProfileState // Ejemplo con datos
    data class Error(val message: String) : UserProfileState
}

class UserProfileViewModel : ViewModel() {

    private val _userProfileState = MutableStateFlow<UserProfileState>(UserProfileState.Idle)
    val userProfileState = _userProfileState.asStateFlow()

    // Este Flow nos servirá para notificar a la UI que debe navegar hacia el login
    private val _navigateToLogin = MutableStateFlow(false)
    val navigateToLogin = _navigateToLogin.asStateFlow()

    init {
        // Al iniciar el ViewModel, cargamos los datos del usuario (simulado por ahora)
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _userProfileState.value = UserProfileState.Loading
            // Aquí iría la lógica para llamar a tu API y obtener los datos del usuario
            // Por ahora, simulamos una carga exitosa con datos de ejemplo
            kotlinx.coroutines.delay(1000) // Simula la espera de la red
            _userProfileState.value = UserProfileState.Success(
                userName = "Dylan", // Reemplazar con datos reales
                userEmail = "email@usuario.com" // Reemplazar con datos reales
            )
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            // Aquí iría la lógica para limpiar tokens o datos de sesión guardados
            // Por ejemplo: userPreferencesManager.clearSession()
            _navigateToLogin.value = true
        }
    }

    fun onNavigateToLoginHandled() {
        _navigateToLogin.value = false
    }
}