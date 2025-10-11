package com.example.urbanfix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbanfix.data.UserPreferencesManager
import com.example.urbanfix.network.RetrofitInstance
import com.example.urbanfix.network.UpdateUserRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface UserProfileState {
    object Loading : UserProfileState
    data class Success(val userName: String, val userEmail: String) : UserProfileState
    data class Error(val message: String) : UserProfileState
}

class UserProfileViewModel(
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _userProfileState = MutableStateFlow<UserProfileState>(UserProfileState.Loading)
    val userProfileState = _userProfileState.asStateFlow()

    private val _navigateToLogin = MutableStateFlow(false)
    val navigateToLogin = _navigateToLogin.asStateFlow()
    val fullName = MutableStateFlow("")
    val password = MutableStateFlow("")
    val confirmPassword = MutableStateFlow("")
    private val _editError = MutableStateFlow<String?>(null)
    val editError = _editError.asStateFlow()

    init {
        loadUserProfile()
    }

    fun onNameChange(newName: String) { fullName.value = newName }
    fun onPasswordChange(newPass: String) { password.value = newPass }
    fun onConfirmPasswordChange(newPass: String) { confirmPassword.value = newPass }
    fun clearEditError() { _editError.value = null }

    fun onSaveChanges(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (fullName.value.isBlank()) {
                _editError.value = "El nombre no puede estar vacío"
                return@launch
            }
            if (password.value.isNotEmpty() && password.value.length < 6) {
                _editError.value = "La contraseña debe tener al menos 6 caracteres"
                return@launch
            }
            if (password.value != confirmPassword.value) {
                _editError.value = "Las contraseñas no coinciden"
                return@launch
            }
            _editError.value = null

            try {
                val role = "usuarios"
                val userId = userPreferencesManager.getUserId()

                if (userId == -1) {
                    _editError.value = "Error: No se pudo obtener el ID del usuario."
                    return@launch
                }

                val requestBody = UpdateUserRequest(
                    nombre = fullName.value,
                    contrasena = if (password.value.isNotEmpty()) password.value else null
                )

                val response = RetrofitInstance.api.updateUser(role, userId, requestBody)

                if (response.isSuccessful) {
                    userPreferencesManager.saveUserData(
                        id = userId,
                        name = fullName.value,
                        email = userPreferencesManager.getUserEmail(),
                        phone = userPreferencesManager.getUserPhone(),
                        role = userPreferencesManager.getUserRole(),
                        companyName = null
                    )

                    _userProfileState.update {
                        if (it is UserProfileState.Success) {
                            it.copy(userName = fullName.value)
                        } else { it }
                    }
                    password.value = ""
                    confirmPassword.value = ""
                    onSuccess()
                } else {
                    _editError.value = "Error al guardar los cambios (código: ${response.code()})"
                }
            } catch (e: Exception) {
                _editError.value = "Error de red: ${e.message}"
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _userProfileState.value = UserProfileState.Loading
            try {
                val realUserName = userPreferencesManager.getUserName()
                val realUserEmail = userPreferencesManager.getUserEmail()

                if (realUserEmail.isNotEmpty()) {
                    _userProfileState.value = UserProfileState.Success(
                        userName = realUserName,
                        userEmail = realUserEmail
                    )
                    fullName.value = realUserName
                } else {
                    _userProfileState.value = UserProfileState.Error("No se encontraron datos del usuario.")
                }
            } catch (e: Exception) {
                _userProfileState.value = UserProfileState.Error("Error al cargar el perfil: ${e.message}")
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            userPreferencesManager.clearCredentials()
            _navigateToLogin.value = true
        }
    }

    fun onNavigateToLoginHandled() {
        _navigateToLogin.value = false
    }
}