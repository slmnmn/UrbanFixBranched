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

sealed interface CompanyProfileState {
    object Loading : CompanyProfileState
    data class Success(val companyName: String, val personalName: String, val userEmail: String) : CompanyProfileState
    data class Error(val message: String) : CompanyProfileState
}

class CompanyProfileViewModel(
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _profileState = MutableStateFlow<CompanyProfileState>(CompanyProfileState.Loading)
    val profileState = _profileState.asStateFlow()

    private val _navigateToLogin = MutableStateFlow(false)
    val navigateToLogin = _navigateToLogin.asStateFlow()

    val companyName = MutableStateFlow("")
    val password = MutableStateFlow("")
    val confirmPassword = MutableStateFlow("")
    private val _editError = MutableStateFlow<String?>(null)
    val editError = _editError.asStateFlow()

    init {
        loadCompanyProfile()
    }

    fun onNameChange(newName: String) { companyName.value = newName }
    fun onPasswordChange(newPass: String) { password.value = newPass }
    fun onConfirmPasswordChange(newPass: String) { confirmPassword.value = newPass }
    fun clearEditError() { _editError.value = null }

    private fun initializeEditFields(name: String) {
        companyName.value = name
    }

    fun onSaveChanges(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (companyName.value.isBlank()) {
                _editError.value = "El nombre del funcionario no puede estar vacío"
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
                val role = "funcionarios"
                val userId = userPreferencesManager.getUserId()

                if (userId == -1) {
                    _editError.value = "Error: No se pudo obtener el ID del usuario."
                    return@launch
                }

                val requestBody = UpdateUserRequest(
                    nombre = companyName.value,
                    contrasena = if (password.value.isNotEmpty()) password.value else null
                )

                val response = RetrofitInstance.api.updateUser(role, userId, requestBody)

                if (response.isSuccessful) {
                    userPreferencesManager.saveUserData(
                        id = userId,
                        name = companyName.value,
                        email = userPreferencesManager.getUserEmail(),
                        phone = userPreferencesManager.getUserPhone(),
                        role = userPreferencesManager.getUserRole(),
                        companyName = userPreferencesManager.getCompanyName()
                    )
                    _profileState.update {
                        if (it is CompanyProfileState.Success) {
                            it.copy(personalName = companyName.value)
                        } else { it }
                    }
                    password.value = ""
                    confirmPassword.value = ""
                    onSuccess()
                } else {
                    _editError.value = "Error al guardar (código: ${response.code()})"
                }
            } catch (e: Exception) {
                _editError.value = "Error de red: ${e.message}"
            }
        }
    }

    private fun loadCompanyProfile() {
        viewModelScope.launch {
            _profileState.value = CompanyProfileState.Loading
            try {
                val fetchedCompanyName = userPreferencesManager.getCompanyName()
                val personalName = userPreferencesManager.getUserName()
                val userEmail = userPreferencesManager.getUserEmail()

                if (userEmail.isNotEmpty()) {
                    _profileState.value = CompanyProfileState.Success(
                        companyName = fetchedCompanyName,
                        personalName = personalName,
                        userEmail = userEmail
                    )
                    initializeEditFields(personalName)
                } else {
                    _profileState.value = CompanyProfileState.Error("No se encontraron datos.")
                }
            } catch (e: Exception) {
                _profileState.value = CompanyProfileState.Error("Error al cargar el perfil: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferencesManager.clearCredentials()
            _navigateToLogin.value = true
        }
    }

    fun onNavigateToLoginHandled() {
        _navigateToLogin.value = false
    }
}