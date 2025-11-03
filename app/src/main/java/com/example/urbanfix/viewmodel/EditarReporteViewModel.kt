package com.example.urbanfix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.example.urbanfix.data.UserPreferencesManager
import com.example.urbanfix.network.* // Asegúrate de importar todos tus data classes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado completo para la UI de esta pantalla
data class ReporteDetalleUiState(
    val reporte: MiReporte? = null,
    val comentarios: List<ComentarioResponse> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val nuevoComentarioTexto: String = ""
)

class EditarReporteViewModel(
    private val reporteId: Int,
    private val userPreferences: UserPreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReporteDetalleUiState())
    val uiState = _uiState.asStateFlow()

    // Propiedades de ayuda para obtener el usuario actual
    private val currentUserId: Int
        get() = userPreferences.getUserId()
    private val currentUserRole: String
        get() = userPreferences.getUserRole() ?: "usuario"

    init {
        // Carga todos los datos tan pronto como se crea el ViewModel
        if (reporteId != -1) {
            cargarDatosCompletos()
        } else {
            _uiState.update { it.copy(isLoading = false, error = "ID de reporte inválido.") }
        }
    }

    fun cargarDatosCompletos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // 1. Cargar el reporte y los comentarios en paralelo
                val reporteResponseAsync = RetrofitInstance.api.getReporteById(reporteId, currentUserId, currentUserRole)
                val comentariosResponseAsync = RetrofitInstance.api.getComentarios(reporteId)

                // 2. Esperar a que ambas respuestas lleguen
                val reporteResponse = reporteResponseAsync
                val comentariosResponse = comentariosResponseAsync

                // 3. --- VERIFICACIÓN DE SEGURIDAD ---
                if (reporteResponse.isSuccessful && comentariosResponse.isSuccessful && reporteResponse.body() != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            reporte = reporteResponse.body(),
                            comentarios = comentariosResponse.body() ?: emptyList()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar datos. Reporte: ${reporteResponse.code()}, Comentarios: ${comentariosResponse.code()}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "No hay conexión: ${e.message}") }
            }
        }
    }

    // Actualiza el texto del campo de comentario
    fun onNuevoComentarioChange(texto: String) {
        _uiState.update { it.copy(nuevoComentarioTexto = texto) }
    }

    // Publica un nuevo comentario
    fun postComentario() {
        if (_uiState.value.nuevoComentarioTexto.isBlank() || currentUserId == -1) return
        val request = ComentarioRequest(
            texto = _uiState.value.nuevoComentarioTexto,
            usuario_id = currentUserId
        )
        viewModelScope.launch {
            _uiState.update { it.copy(nuevoComentarioTexto = "") }
            try {
                val response = RetrofitInstance.api.postComentario(reporteId, request)
                if (response.isSuccessful) {
                    cargarDatosCompletos() // Recarga todo para estar seguro
                } else {
                    _uiState.update { it.copy(error = "No se pudo publicar el comentario (${response.code()})") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al publicar comentario: ${e.message}") }
            }
        }
    }

    // Maneja el clic en "like" o "dislike"
    fun handleReaccion(tipo: String) {
        if (currentUserId == -1) return
        viewModelScope.launch {
            val reaccionActual = _uiState.value.reporte?.current_user_reaction
            try {
                if (reaccionActual == tipo) {
                    val request = ReactionRemoveRequest(actor_id = currentUserId, role = currentUserRole)
                    RetrofitInstance.api.removeReaccion(reporteId, request)
                } else {
                    val request = ReactionRequest(tipo = tipo, actor_id = currentUserId, role = currentUserRole)
                    RetrofitInstance.api.setReaccion(reporteId, request)
                }
                // Recargamos el reporte para obtener los nuevos contadores
                val updatedReporteResponse = RetrofitInstance.api.getReporteById(reporteId, currentUserId, currentUserRole)
                if (updatedReporteResponse.isSuccessful) {
                    _uiState.update { it.copy(reporte = updatedReporteResponse.body()) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al procesar reacción") }
            }
        }
    }

    // Elimina un comentario
    fun deleteComentario(comentarioId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.deleteComentario(comentarioId)
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(comentarios = it.comentarios.filterNot { c -> c.id == comentarioId })
                    }
                }
            } catch (e: Exception) {
                println("Error al eliminar comentario: ${e.message}")
            }
        }
    }

    // Edita un comentario
    fun updateComentario(comentarioId: Int, nuevoTexto: String) {
        viewModelScope.launch {
            val request = ComentarioUpdateRequest(texto = nuevoTexto)
            try {
                val response = RetrofitInstance.api.updateComentario(comentarioId, request)
                if (response.isSuccessful && response.body() != null) {
                    val comentarioActualizado = response.body()!!
                    _uiState.update {
                        it.copy(comentarios = it.comentarios.map { c ->
                            if (c.id == comentarioId) comentarioActualizado else c
                        })
                    }
                }
            } catch (e: Exception) {
                println("Error al actualizar comentario: ${e.message}")
            }
        }
    }
    //Estadosreportes
    fun updateEstadoReporte(nuevoEstado: String) {
        // Comprobación de seguridad
        if (currentUserRole != "funcionario" || _uiState.value.reporte == null) {
            _uiState.update { it.copy(error = "Acción no autorizada") }
            return
        }

        viewModelScope.launch {
            val request = UpdateEstadoRequest(estado = nuevoEstado)
            try {
                // endpointcall
                val response = RetrofitInstance.api.updateReporteEstado(
                    reporteId,
                    currentUserRole,
                    currentUserId,
                    request
                )

                if (response.isSuccessful && response.body() != null) {
                    // Ok - ubdate
                    _uiState.update { it.copy(reporte = response.body()) }
                } else {
                    _uiState.update { it.copy(error = "No se pudo actualizar el estado (${response.code()})") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error de red: ${e.message}") }
            }
        }
    }
    class Factory(
        private val context: Context,
        private val reporteId: Int
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EditarReporteViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EditarReporteViewModel(
                    reporteId,
                    UserPreferencesManager(context.applicationContext)
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}