// ProfileViewModel.kt - VERSIÓN COMPLETA Y CORREGIDA

package com.example.urbanfix.viewmodel

import android.graphics.Bitmap
import android.util.Base64 // <-- Import para Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbanfix.R
import com.example.urbanfix.data.UserPreferencesManager
import com.example.urbanfix.network.FotoPerfilRequest // <-- Import del nuevo modelo
import com.example.urbanfix.network.RetrofitInstance // <-- Asumo que tienes esto
import com.example.urbanfix.network.UpdateUserRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream // <-- Import para convertir Bitmap

// --- Estado general del perfil ---
sealed interface ProfileState {
    object Loading : ProfileState
    data class Success(
        val role: String,
        val userEmail: String,
        val registrationDate: String,
        val profilePicBitmap: Bitmap?, // Ya estaba
        val userName: String? = null,
        val companyName: String? = null,
        val personalName: String? = null
    ) : ProfileState
    data class Error(val messageId: Int) : ProfileState
}

// --- NUEVO: Estado específico para la subida de foto ---
sealed interface PhotoUploadState {
    object Idle : PhotoUploadState // Estado inicial, listo
    object Loading : PhotoUploadState // Subiendo foto
    object Success : PhotoUploadState // Foto subida con éxito
    data class Error(val message: String) : PhotoUploadState // Error al subir
}

class ProfileViewModel(
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    // --- ESTADOS PRINCIPALES ---
    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _navigateToLogin = MutableStateFlow(false)
    val navigateToLogin: StateFlow<Boolean> = _navigateToLogin.asStateFlow()

    // --- ESTADOS PARA EDICIÓN (Nombre, Contraseña) ---
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

    // --- ESTADO PARA LA FOTO DE PERFIL (Bitmap) ---
    private val _profilePicBitmap = MutableStateFlow<Bitmap?>(null)
    val profilePicBitmap: StateFlow<Bitmap?> = _profilePicBitmap.asStateFlow()

    // --- NUEVO: ESTADO PARA LA SUBIDA DE LA FOTO ---
    private val _photoUploadState = MutableStateFlow<PhotoUploadState>(PhotoUploadState.Idle)
    val photoUploadState: StateFlow<PhotoUploadState> = _photoUploadState.asStateFlow()

    // --- INICIALIZACIÓN ---
    init {
        loadProfile()
    }

    // --- CARGAR DATOS DEL PERFIL ---
    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                // Lee datos básicos de SharedPreferences
                val userRole = userPreferencesManager.getUserRole()
                val email = userPreferencesManager.getUserEmail()
                val registrationDate = userPreferencesManager.getRegistrationDate()
                _role.value = userRole // Guarda el rol para lógica de edición/subida

                // *** Carga el Bitmap de la foto desde SharedPreferences ***
                val picBitmap = userPreferencesManager.getProfilePicBitmap() // Asume que esta función existe
                _profilePicBitmap.value = picBitmap // Actualiza el StateFlow del Bitmap

                if (userRole.isEmpty() || email.isEmpty()) {
                    _profileState.value = ProfileState.Error(R.string.error_profile_not_found)
                    return@launch
                }

                // Actualiza el estado principal según el rol
                when (userRole) {
                    "usuario" -> {
                        val userName = userPreferencesManager.getUserName()
                        _name.value = userName // Pre-rellena el campo de edición
                        _profileState.value = ProfileState.Success(
                            role = "usuario",
                            userEmail = email,
                            userName = userName,
                            registrationDate = registrationDate,
                            profilePicBitmap = picBitmap // Pasa el bitmap al estado Success
                        )
                    }
                    "funcionario" -> {
                        val companyName = userPreferencesManager.getCompanyName()
                        val personalName = userPreferencesManager.getUserName()
                        _name.value = personalName // Pre-rellena el campo de edición
                        _profileState.value = ProfileState.Success(
                            role = "funcionario",
                            userEmail = email,
                            companyName = companyName,
                            personalName = personalName,
                            registrationDate = registrationDate,
                            profilePicBitmap = picBitmap // Pasa el bitmap al estado Success
                        )
                    }
                    else -> _profileState.value = ProfileState.Error(R.string.error_invalid_role)
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(R.string.error_loading_profile)
            }
        }
    }

    // --- FUNCIONES PARA EDICIÓN (Nombre, Contraseña) ---
    fun onNameChange(newName: String) { _name.value = newName; _editError.value = null }
    fun onPasswordChange(newPass: String) { _password.value = newPass; _editError.value = null }
    fun onConfirmPasswordChange(newPass: String) { _confirmPassword.value = newPass; _editError.value = null }
    fun clearEditError() { _editError.value = null }

    fun validateForSave(): Boolean {
        if (_name.value.isBlank()) {
            _editError.value = if (_role.value == "funcionario") R.string.error_official_name_empty else R.string.error_user_name_empty
            return false
        }
        if (_password.value.isNotEmpty() || _confirmPassword.value.isNotEmpty()) {
            if (_password.value.length < 6) { _editError.value = R.string.error_password_length; return false }
            if (_password.value != _confirmPassword.value) { _editError.value = R.string.error_password_mismatch; return false }
        }
        _editError.value = null
        return true
    }

    fun onSaveChanges(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (!validateForSave()) return@launch // Usa la validación
            _editError.value = null // Limpia errores

            try {
                val userId = userPreferencesManager.getUserId()
                if (userId == -1) { _editError.value = R.string.error_no_user_id; return@launch }

                val requestBody = UpdateUserRequest(
                    nombre = _name.value,
                    contrasena = if (_password.value.isNotEmpty()) _password.value else null
                )
                val pathRole = if (_role.value == "funcionario") "funcionarios" else "usuarios"
                val response = RetrofitInstance.api.updateUser(pathRole, userId, requestBody)

                if (response.isSuccessful) {
                    // *** ¡IMPORTANTE! Idealmente, crea userPreferencesManager.saveUserName(newName: String) ***
                    userPreferencesManager.saveUserName(_name.value) // Asume que existe esta función

                    _profileState.update {
                        when (it) {
                            is ProfileState.Success -> {
                                if (_role.value == "funcionario") it.copy(personalName = _name.value)
                                else it.copy(userName = _name.value)
                            }
                            else -> it
                        }
                    }
                    _password.value = ""; _confirmPassword.value = "" // Limpia campos de contraseña
                    onSuccess() // Llama al callback para navegar
                } else { _editError.value = R.string.error_save_changes }
            } catch (e: Exception) { _editError.value = R.string.error_network_connection }
        }
    }

    private fun UserPreferencesManager.saveUserName(value: String) {}

    // --- NUEVO: FUNCIONES PARA FOTO DE PERFIL ---
    fun actualizarFotoDePerfil(bitmap: Bitmap) {
        viewModelScope.launch {
            _photoUploadState.value = PhotoUploadState.Loading
            try {
                // Convertir a Base64
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                val byteArray = outputStream.toByteArray()
                val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
                val requestBody = FotoPerfilRequest(foto_base64 = base64String)

                // Obtener ID y Rol
                val userId = userPreferencesManager.getUserId()
                val userRole = _role.value // Usamos el rol ya cargado en loadProfile
                if (userId == -1) { _photoUploadState.value = PhotoUploadState.Error("ID Usuario no encontrado"); return@launch }

                // Llamar API correcta
                val response = if (userRole == "usuario") {
                    RetrofitInstance.api.subirFotoUsuario(userId, requestBody)
                } else if (userRole == "funcionario") {
                    RetrofitInstance.api.subirFotoFuncionario(userId, requestBody)
                } else {
                    _photoUploadState.value = PhotoUploadState.Error("Rol inválido"); return@launch
                }

                // Procesar respuesta
                if (response.isSuccessful) {
                    userPreferencesManager.saveProfilePicBitmap(bitmap) // Guarda localmente (NECESITA EXISTIR)
                    _profilePicBitmap.value = bitmap // Actualiza UI inmediata
                    // Actualiza también el estado principal para reflejar la nueva foto
                    _profileState.update {
                        when(it) {
                            is ProfileState.Success -> it.copy(profilePicBitmap = bitmap)
                            else -> it
                        }
                    }
                    _photoUploadState.value = PhotoUploadState.Success
                } else {
                    _photoUploadState.value = PhotoUploadState.Error("Error Servidor: ${response.code()}")
                }
            } catch (e: Exception) {
                _photoUploadState.value = PhotoUploadState.Error("Error Red: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun eliminarFotoDePerfil() {
        // (Opcional: Llamar API para borrar de S3 si tienes endpoint)
        userPreferencesManager.deleteProfilePic() // Borra localmente (NECESITA EXISTIR)
        _profilePicBitmap.value = null // Actualiza UI inmediata
        // Actualiza también el estado principal para quitar la foto
        _profileState.update {
            when(it) {
                is ProfileState.Success -> it.copy(profilePicBitmap = null)
                else -> it
            }
        }
        // (Opcional: Podrías añadir un estado PhotoUploadState.Deleted o similar)
    }

    fun resetPhotoUploadState() {
        _photoUploadState.value = PhotoUploadState.Idle
    }

    // --- LOGOUT Y BORRAR CUENTA ---
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
                } else {
                    // Quizás mostrar un error si la eliminación falla
                    _profileState.value = ProfileState.Error(R.string.error_deleting_account) // Necesitas este string
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(R.string.error_network_connection)
            }
        }
    }

    fun onAccountDeletedHandled() {
        _accountDeleted.value = false
    }
}