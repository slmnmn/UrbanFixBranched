package com.example.urbanfix.viewmodel

import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbanfix.R
import com.example.urbanfix.data.UserPreferencesManager
import com.example.urbanfix.network.*
import retrofit2.Response
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import android.util.Log

// --- Estado general del perfil ---
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

// --- Estado para ver perfil de otro usuario ---
data class OtherUserProfileState(
    val isLoading: Boolean = true,
    val userName: String? = null,
    val userEmail: String? = null,
    val accountType: String? = null,
    val registrationDate: String? = null,
    val companyName: String? = null,
    val profile_pic_url: String? = null,
    val error: String? = null
)

// --- Estado específico para la subida de foto ---
sealed interface PhotoUploadState {
    object Idle : PhotoUploadState
    object Loading : PhotoUploadState
    object Success : PhotoUploadState
    data class Error(val message: String) : PhotoUploadState
}

// --- Estado para cargar las listas de reportes ---
sealed interface ReportListState {
    object Idle : ReportListState
    object Loading : ReportListState
    object Success : ReportListState
    data class Error(val message: String) : ReportListState
}

class ProfileViewModel(
    private val userPreferencesManager: UserPreferencesManager,
    private val otherUserId: Int? = null,
    private val otherUserRole: String? = null
) : ViewModel() {

    // --- ESTADOS PRINCIPALES ---
    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _navigateToLogin = MutableStateFlow(false)
    val navigateToLogin: StateFlow<Boolean> = _navigateToLogin.asStateFlow()

    // --- Estado para ver perfil de otro usuario ---
    private val _otherUserProfileState = MutableStateFlow(OtherUserProfileState())
    val otherUserProfileState: StateFlow<OtherUserProfileState> = _otherUserProfileState.asStateFlow()

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

    // --- ESTADO PARA LA SUBIDA DE LA FOTO ---
    private val _photoUploadState = MutableStateFlow<PhotoUploadState>(PhotoUploadState.Idle)
    val photoUploadState: StateFlow<PhotoUploadState> = _photoUploadState.asStateFlow()

    // --- ESTADOS PARA LAS LISTAS DE REPORTES ---
    private val _apoyosList = MutableStateFlow<List<MiReporte>>(emptyList())
    val apoyosList: StateFlow<List<MiReporte>> = _apoyosList.asStateFlow()

    private val _denunciasList = MutableStateFlow<List<MiReporte>>(emptyList())
    val denunciasList: StateFlow<List<MiReporte>> = _denunciasList.asStateFlow()

    private val _reportListState = MutableStateFlow<ReportListState>(ReportListState.Idle)
    val reportListState: StateFlow<ReportListState> = _reportListState.asStateFlow()

    // --- INICIALIZACIÓN ---
    init {
        if (otherUserId != null) {
            loadOtherUserProfile(otherUserId, otherUserRole ?: "usuario")
        } else {
            loadProfile()
        }
    }

    // --- CARGAR DATOS DEL PERFIL PROPIO ---
    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val userRole = userPreferencesManager.getUserRole()
                val email = userPreferencesManager.getUserEmail()
                val registrationDate = userPreferencesManager.getRegistrationDate()
                _role.value = userRole

                val picBitmap = userPreferencesManager.getProfilePicBitmap()
                _profilePicBitmap.value = picBitmap

                if (userRole.isEmpty() || email.isEmpty()) {
                    _profileState.value = ProfileState.Error(R.string.error_profile_not_found)
                    return@launch
                }

                when (userRole) {
                    "usuario" -> {
                        val userName = userPreferencesManager.getUserName()
                        _name.value = userName
                        _profileState.value = ProfileState.Success(
                            role = "usuario",
                            userEmail = email,
                            userName = userName,
                            registrationDate = registrationDate,
                            profilePicBitmap = picBitmap
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
                            profilePicBitmap = picBitmap
                        )
                    }
                    else -> _profileState.value = ProfileState.Error(R.string.error_invalid_role)
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(R.string.error_loading_profile)
            }
        }
    }

    // --- CARGAR PERFIL DE OTRO USUARIO ---
    // --- CARGAR PERFIL DE OTRO USUARIO ---
    fun loadOtherUserProfile(userId: Int, userRole: String = "usuario") {
        viewModelScope.launch {
            _otherUserProfileState.value = _otherUserProfileState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                Log.d("PROFILE_VM", "🔍 Intentando cargar perfil - ID: $userId, Rol: $userRole")

                val response = if (userRole == "funcionario") {
                    RetrofitInstance.api.getOtherFuncionarioProfile(userId)
                } else {
                    RetrofitInstance.api.getOtherUserProfile(userId)
                }

                Log.d("PROFILE_VM", "📡 Respuesta recibida - Código: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val profile = response.body()!!

                    Log.d("PROFILE_VM", "✅ Perfil cargado exitosamente: ${profile.nombre}")
                    Log.d("PROFILE_VM", "🖼️ URL de foto recibida: ${profile.profile_pic_url}")

                    // Verificar si la URL es nula o vacía
                    if (profile.profile_pic_url.isNullOrEmpty()) {
                        Log.w("PROFILE_VM", "⚠️ ADVERTENCIA: URL de foto es nula o vacía")
                    } else {
                        Log.d("PROFILE_VM", "✅ URL válida: ${profile.profile_pic_url}")
                    }

                    val accountType = when (profile.role) {
                        "funcionario" -> "Funcionario"
                        "usuario" -> "Usuario"
                        else -> "Desconocido"
                    }

                    _otherUserProfileState.value = OtherUserProfileState(
                        isLoading = false,
                        userName = profile.nombre,
                        userEmail = profile.email,
                        accountType = accountType,
                        registrationDate = profile.fecha_registro,
                        companyName = profile.entidad_nombre,
                        profile_pic_url = profile.profile_pic_url,
                        error = null
                    )

                    Log.d("PROFILE_VM", "✅ Estado actualizado correctamente")
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = "Error al cargar el perfil: ${response.code()}"

                    _otherUserProfileState.value = _otherUserProfileState.value.copy(
                        isLoading = false,
                        error = errorMsg
                    )

                    Log.e("PROFILE_VM", "❌ Error API: ${response.code()}")
                    Log.e("PROFILE_VM", "❌ Error body: $errorBody")
                    Log.e("PROFILE_VM", "❌ URL solicitada: ${response.raw().request.url}")
                }
            } catch (e: Exception) {
                _otherUserProfileState.value = _otherUserProfileState.value.copy(
                    isLoading = false,
                    error = "Error de conexión: ${e.message}"
                )
                Log.e("PROFILE_VM", "❌ Error de red al cargar perfil", e)
            }
        }
    }

    // --- FUNCIONES PARA EDICIÓN (Nombre, Contraseña) ---
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

    fun validateForSave(): Boolean {
        if (_name.value.isBlank()) {
            _editError.value = if (_role.value == "funcionario")
                R.string.error_official_name_empty
            else
                R.string.error_user_name_empty
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

    fun onSaveChanges(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (!validateForSave()) return@launch
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
                    userPreferencesManager.saveUserName(_name.value)

                    _profileState.update {
                        when (it) {
                            is ProfileState.Success -> {
                                if (_role.value == "funcionario")
                                    it.copy(personalName = _name.value)
                                else
                                    it.copy(userName = _name.value)
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

    // --- FUNCIONES PARA FOTO DE PERFIL ---
    fun actualizarFotoDePerfil(bitmap: Bitmap) {
        viewModelScope.launch {
            _photoUploadState.value = PhotoUploadState.Loading
            try {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                val byteArray = outputStream.toByteArray()
                val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
                val requestBody = FotoPerfilRequest(foto_base64 = base64String)

                val userId = userPreferencesManager.getUserId()
                val userRole = _role.value
                if (userId == -1) {
                    _photoUploadState.value = PhotoUploadState.Error("ID Usuario no encontrado")
                    return@launch
                }

                val response = if (userRole == "usuario") {
                    RetrofitInstance.api.subirFotoUsuario(userId, requestBody)
                } else if (userRole == "funcionario") {
                    RetrofitInstance.api.subirFotoFuncionario(userId, requestBody)
                } else {
                    _photoUploadState.value = PhotoUploadState.Error("Rol inválido")
                    return@launch
                }

                if (response.isSuccessful) {
                    userPreferencesManager.saveProfilePicBitmap(bitmap)
                    _profilePicBitmap.value = bitmap
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
        userPreferencesManager.deleteProfilePic()
        _profilePicBitmap.value = null
        _profileState.update {
            when(it) {
                is ProfileState.Success -> it.copy(profilePicBitmap = null)
                else -> it
            }
        }
    }

    fun resetPhotoUploadState() {
        _photoUploadState.value = PhotoUploadState.Idle
    }

    // --- FUNCIONES PARA CARGAR LISTAS DE REPORTES ---
    fun fetchUserApoyos() {
        viewModelScope.launch {
            _reportListState.value = ReportListState.Loading

            val actorId = userPreferencesManager.getUserId()
            val actorRole = userPreferencesManager.getUserRole()

            if (actorId == -1) {
                _reportListState.value = ReportListState.Error("Usuario no identificado")
                return@launch
            }

            try {
                val response: Response<List<MiReporte>>

                if (actorRole == "usuario") {
                    response = RetrofitInstance.api.getUserApoyos(actorId)
                } else if (actorRole == "funcionario") {
                    response = RetrofitInstance.api.getFuncionarioApoyos(actorId)
                } else {
                    Log.e("API_FAIL", "Rol desconocido: $actorRole")
                    _reportListState.value = ReportListState.Error("Rol desconocido")
                    return@launch
                }

                if (response.isSuccessful) {
                    val fetchedList = response.body() ?: emptyList()
                    Log.d("APOYOS_FETCH", "Datos recibidos del API: $fetchedList")
                    _apoyosList.value = fetchedList
                    _reportListState.value = ReportListState.Success
                } else {
                    _reportListState.value = ReportListState.Error("Error al cargar apoyos: ${response.code()}")
                }
            } catch (e: Exception) {
                _reportListState.value = ReportListState.Error("Error de red: ${e.message}")
            }
        }
    }

    fun fetchUserDenuncias() {
        viewModelScope.launch {
            _reportListState.value = ReportListState.Loading

            val actorId = userPreferencesManager.getUserId()
            val actorRole = userPreferencesManager.getUserRole()

            if (actorId == -1) {
                _reportListState.value = ReportListState.Error("Usuario no identificado")
                return@launch
            }

            try {
                val response: Response<List<MiReporte>>

                if (actorRole == "usuario") {
                    response = RetrofitInstance.api.getUserDenuncias(actorId)
                } else if (actorRole == "funcionario") {
                    response = RetrofitInstance.api.getFuncionarioDenuncias(actorId)
                } else {
                    Log.e("API_FAIL", "Rol desconocido: $actorRole")
                    _reportListState.value = ReportListState.Error("Rol desconocido")
                    return@launch
                }

                if (response.isSuccessful) {
                    _denunciasList.value = response.body() ?: emptyList()
                    _reportListState.value = ReportListState.Success
                } else {
                    _reportListState.value = ReportListState.Error("Error al cargar denuncias: ${response.code()}")
                }
            } catch (e: Exception) {
                _reportListState.value = ReportListState.Error("Error de red: ${e.message}")
            }
        }
    }

    // --- FUNCIONES PARA LIKE/DISLIKE (REACCIONES) ---
    private fun updateLocalReaction(reporteId: Int, newReaction: String?) {
        _apoyosList.update { currentList ->
            if (newReaction != "like") {
                currentList.filterNot { it.id == reporteId }
            } else {
                currentList.map {
                    if (it.id == reporteId)
                        it.copy(current_user_reaction = newReaction)
                    else
                        it
                }
            }
        }

        _denunciasList.update { currentList ->
            if (newReaction != "dislike") {
                currentList.filterNot { it.id == reporteId }
            } else {
                currentList.map {
                    if (it.id == reporteId)
                        it.copy(current_user_reaction = newReaction)
                    else
                        it
                }
            }
        }
    }

    fun toggleLikeDislike(reporteId: Int, currentReaction: String?, action: String) {
        viewModelScope.launch {
            val actorId = userPreferencesManager.getUserId()
            val actorRole = userPreferencesManager.getUserRole()

            if (actorId == -1 || actorRole.isNullOrEmpty()) {
                Log.e("API_FAIL", "ID de actor ($actorId) o Rol ($actorRole) no encontrados")
                return@launch
            }

            val shouldRemove = currentReaction == action

            if (shouldRemove) {
                updateLocalReaction(reporteId, null)
            } else {
                updateLocalReaction(reporteId, action)
            }

            try {
                val response: Response<Unit>

                if (shouldRemove) {
                    val removeRequestBody = ReactionRemoveRequest(actor_id = actorId, role = actorRole)
                    response = RetrofitInstance.api.removeReaccion(reporteId, removeRequestBody)
                } else {
                    val setRequestBody = ReactionRequest(actor_id = actorId, role = actorRole, tipo = action)
                    response = RetrofitInstance.api.setReaccion(reporteId, setRequestBody)
                }

                if (!response.isSuccessful) {
                    updateLocalReaction(reporteId, currentReaction)
                    Log.e("API_FAIL", "Error API reaction: ${response.code()} - ${response.errorBody()?.string()}")
                }

            } catch (e: Exception) {
                updateLocalReaction(reporteId, currentReaction)
                Log.e("API_FAIL", "Error Network reaction: ${e.message}")
                e.printStackTrace()
            }
        }
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
                    _profileState.value = ProfileState.Error(R.string.error_deleting_account)
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(R.string.error_network_connection)
            }
        }
    }

    fun onAccountDeletedHandled() {
        _accountDeleted.value = false
    }

    // --- FUNCIÓN ADICIONAL del código simple ---
    fun updateUserName(newName: String) {
        userPreferencesManager.saveUserName(newName)
        _name.value = newName
        _profileState.update {
            when (it) {
                is ProfileState.Success -> {
                    if (_role.value == "funcionario")
                        it.copy(personalName = newName)
                    else
                        it.copy(userName = newName)
                }
                else -> it
            }
        }
    }
}