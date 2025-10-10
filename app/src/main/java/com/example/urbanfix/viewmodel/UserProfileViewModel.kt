package com.example.urbanfix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface UserProfileState {
    object Idle : UserProfileState
    object Loading : UserProfileState
    data class Success(val userName: String, val userEmail: String) : UserProfileState
    data class Error(val message: String) : UserProfileState
}

class UserProfileViewModel : ViewModel() {

    private val _userProfileState = MutableStateFlow<UserProfileState>(UserProfileState.Idle)
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
            if (password.value.isNotEmpty() || confirmPassword.value.isNotEmpty()) {
                if (password.value.isBlank() || confirmPassword.value.isBlank()) {
                    _editError.value = "Debes llenar ambos campos para cambiar la contraseña"
                    return@launch
                }
                if (password.value != confirmPassword.value) {
                    _editError.value = "Las contraseñas no coinciden"
                    return@launch
                }
            }
            _editError.value = null
            _userProfileState.update {
                if (it is UserProfileState.Success) {
                    it.copy(userName = fullName.value)
                } else {
                    it
                }
            }
            password.value = ""
            confirmPassword.value = ""
            onSuccess()
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _userProfileState.value = UserProfileState.Loading
            delay(1000)
            val fetchedName = "Johan Felipe Aguilar Castillo"
            _userProfileState.value = UserProfileState.Success(
                userName = fetchedName,
                userEmail = "email@usuario.com"
            )
            fullName.value = fetchedName
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            _navigateToLogin.value = true
        }
    }

    fun onNavigateToLoginHandled() {
        _navigateToLogin.value = false
    }
}