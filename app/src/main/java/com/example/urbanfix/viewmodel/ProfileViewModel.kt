// ProfileViewModel.kt - ACTUALIZACIÓN COMPLETA

package com.example.urbanfix.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbanfix.R
import com.example.urbanfix.data.UserPreferencesManager
import com.example.urbanfix.network.RetrofitInstance
import com.example.urbanfix.network.UpdateUserRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ProfileState {
    object Loading : ProfileState
    data class Success(
        val role: String,
        val userEmail: String,
        val registrationDate: String,
        val profilePicBitmap: Bitmap?,
        val userName: String? = null,
        val companyName: String? = null,
        val personalName: String? = null
    ) : ProfileState
    data class Error(val messageId: Int) : ProfileState
}

class ProfileViewModel(
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _navigateToLogin = MutableStateFlow(false)
    val navigateToLogin: StateFlow<Boolean> = _navigateToLogin.asStateFlow()

    // Campos para edición
    private val _role = MutableStateFlow("")
    val role: StateFlow<String> = _role.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _editError = MutableStateFlow<Int?>(null)
    val editError: StateFlow<Int?> = _editError.asStateFlow()

    private val _profilePicBitmap = MutableStateFlow<Bitmap?>(null)
    val profilePicBitmap: StateFlow<Bitmap?> = _profilePicBitmap.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading

            try {
                val userRole = userPreferencesManager.getUserRole()
                val email = userPreferencesManager.getUserEmail()
                val registrationDate = userPreferencesManager.getRegistrationDate()

                val picBitmap = userPreferencesManager.getProfilePicBitmap()
                _profilePicBitmap.value = picBitmap

                if (userRole.isEmpty() || email.isEmpty()) {
                    _profileState.value = ProfileState.Error(R.string.error_profile_not_found)
                    return@launch
                }

                _role.value = userRole

                when (userRole) {
                    "usuario" -> {
                        val userName = userPreferencesManager.getUserName()
                        _name.value = userName
                        _profileState.value = ProfileState.Success(
                            role = "usuario",
                            userEmail = email,
                            userName = userName,
                            registrationDate = registrationDate,
                            profilePicBitmap = picBitmap  // ← USAR picBitmap
                        )
                    }
                    "funcionario" -> {
                        val companyName = userPreferencesManager.getCompanyName()
                        val personalName = userPreferencesManager.getUserName()
                        _name.value = personalName
                        _profileState.value = ProfileState.Success(
                            role = "funcionario",
                            userEmail = email,
                            companyName = companyName,
                            personalName = personalName,
                            registrationDate = registrationDate,
                            profilePicBitmap = picBitmap  // ← USAR picBitmap
                        )
                    }
                    else -> {
                        _profileState.value = ProfileState.Error(R.string.error_invalid_role)
                    }
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(R.string.error_loading_profile)
            }
        }
    }

    // Funciones para edición
    fun onNameChange(newName: String) {
        _name.value = newName
        _editError.value = null
    }

    fun onPasswordChange(newPass: String) {
        _password.value = newPass
        _editError.value = null
    }

    fun onConfirmPasswordChange(newPass: String) {
        _confirmPassword.value = newPass
        _editError.value = null
    }

    fun clearEditError() {
        _editError.value = null
    }

    fun onSaveChanges(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (_name.value.isBlank()) {
                _editError.value = if (_role.value == "funcionario") {
                    R.string.error_official_name_empty
                } else {
                    R.string.error_user_name_empty
                }
                return@launch
            }
            if (_password.value.isNotEmpty() || _confirmPassword.value.isNotEmpty()) {
                if (_password.value.length < 6) {
                    _editError.value = R.string.error_password_length
                    return@launch
                }
                if (_password.value != _confirmPassword.value) {
                    _editError.value = R.string.error_password_mismatch
                    return@launch
                }
            }
            _editError.value = null

            try {
                val userId = userPreferencesManager.getUserId()
                if (userId == -1) {
                    _editError.value = R.string.error_no_user_id
                    return@launch
                }

                val requestBody = UpdateUserRequest(
                    nombre = _name.value,
                    contrasena = if (_password.value.isNotEmpty()) _password.value else null
                )

                val pathRole = if (_role.value == "funcionario") "funcionarios" else "usuarios"

                val response = RetrofitInstance.api.updateUser(pathRole, userId, requestBody)

                if (response.isSuccessful) {
                    val existingDate = userPreferencesManager.getRegistrationDate()
                    userPreferencesManager.saveUserData(
                        id = userId,
                        name = _name.value,
                        email = userPreferencesManager.getUserEmail(),
                        phone = userPreferencesManager.getUserPhone(),
                        role = _role.value,
                        companyName = if (_role.value == "funcionario") userPreferencesManager.getCompanyName() else null,
                        registrationDate = existingDate
                    )

                    _profileState.update {
                        when (it) {
                            is ProfileState.Success -> {
                                if (_role.value == "funcionario") {
                                    it.copy(personalName = _name.value)
                                } else {
                                    it.copy(userName = _name.value)
                                }
                            }
                            else -> it
                        }
                    }

                    _password.value = ""
                    _confirmPassword.value = ""
                    onSuccess()
                } else {
                    _editError.value = R.string.error_save_changes
                }
            } catch (e: Exception) {
                _editError.value = R.string.error_network_connection
            }
        }
    }

    fun validateForSave(): Boolean {
        if (_name.value.isBlank()) {
            _editError.value = if (_role.value == "funcionario") {
                R.string.error_official_name_empty
            } else {
                R.string.error_user_name_empty
            }
            return false
        }
        if (_password.value.isNotEmpty() || _confirmPassword.value.isNotEmpty()) {
            if (_password.value.length < 6) {
                _editError.value = R.string.error_password_length
                return false
            }
            if (_password.value != _confirmPassword.value) {
                _editError.value = R.string.error_password_mismatch
                return false
            }
        }
        _editError.value = null
        return true
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

    private val _accountDeleted = MutableStateFlow(false)
    val accountDeleted: StateFlow<Boolean> = _accountDeleted.asStateFlow()

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                val userId = userPreferencesManager.getUserId()
                if (userId == -1) { return@launch }

                val pathRole = if (_role.value == "funcionario") "funcionarios" else "usuarios"
                val response = RetrofitInstance.api.deleteUser(pathRole, userId)

                if (response.isSuccessful) {
                    userPreferencesManager.deleteAllData()
                    _accountDeleted.value = true
                }
            } catch (e: Exception) {
                // Manejo de error
            }
        }
    }

    fun onAccountDeletedHandled() {
        _accountDeleted.value = false
    }
}