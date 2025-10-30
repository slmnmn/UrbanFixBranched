package com.example.urbanfix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.example.urbanfix.data.UserPreferencesManager
import com.example.urbanfix.network.* // Asegúrate de importar todos tus data classes (MiReporte, ComentarioResponse, etc.)
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
                // 1. Cargar el reporte y los comentarios en paralelo (más rápido)
                val reporteResponseAsync = RetrofitInstance.api.getReporteById(reporteId, currentUserId, currentUserRole)
                val comentariosResponseAsync = RetrofitInstance.api.getComentarios(reporteId)

                // 2. Esperar a que ambas respuestas lleguen
                val reporteResponse = reporteResponseAsync
                val comentariosResponse = comentariosResponseAsync

                // 3. --- VERIFICACIÓN DE SEGURIDAD ---
                // Revisamos que AMBAS llamadas fueran exitosas Y que el reporte tenga cuerpo
                if (reporteResponse.isSuccessful && comentariosResponse.isSuccessful && reporteResponse.body() != null) {
                    // ¡Éxito! Actualizamos la UI
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            reporte = reporteResponse.body(), // Sabemos que no es nulo
                            comentarios = comentariosResponse.body() ?: emptyList() // Si es nulo, usamos lista vacía
                        )
                    }
                } else {
                    // Si una de las dos falla, mostramos un error claro
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar datos. Reporte: ${reporteResponse.code()}, Comentarios: ${comentariosResponse.code()}"
                        )
                    }
                }
            } catch (e: Exception) {
                // Error de red (ej. sin internet)
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
            // Limpia el campo de texto INMEDIATAMENTE para que el usuario vea una respuesta
            _uiState.update { it.copy(nuevoComentarioTexto = "") }

            try {
                // Hacemos el post
                val response = RetrofitInstance.api.postComentario(reporteId, request)

                if (response.isSuccessful) {
                    // El post fue exitoso.
                    // AHORA, en lugar de confiar en el response.body(),
                    // simplemente volvemos a cargar todo.
                    // Es un poco más lento, pero 100% seguro contra crasheos.
                    cargarDatosCompletos()
                } else {
                    // El posteo falló, mostrar un error
                    _uiState.update { it.copy(error = "No se pudo publicar el comentario (${response.code()})") }
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al publicar comentario: ${e.message}") }
                println("Error al publicar comentario: ${e.message}")
            }
        }
    }

    // Maneja el clic en "like" o "dislike"
    fun handleReaccion(tipo: String) {
        if (currentUserId == -1) return // No permitir reacciones si no está logueado

        viewModelScope.launch {
            val reaccionActual = _uiState.value.reporte?.current_user_reaction

            try {
                if (reaccionActual == tipo) {
                    // El usuario hizo clic en el mismo botón (ej. "like" otra vez): Quitar reacción
                    val request = ReactionRemoveRequest(
                        actor_id = currentUserId, // <-- CORREGIDO
                        role = currentUserRole    // <-- AÑADIDO
                    )
                    RetrofitInstance.api.removeReaccion(reporteId, request)
                } else {
                    // El usuario hizo clic en un botón nuevo: Poner/cambiar reacción
                    val request = ReactionRequest(
                        tipo = tipo,
                        actor_id = currentUserId, // <-- CORREGIDO
                        role = currentUserRole    // <-- AÑADIDO
                    )
                    RetrofitInstance.api.setReaccion(reporteId, request)
                }

                // Después de la reacción, recargamos el reporte para obtener los nuevos contadores
                // y el estado de 'current_user_reaction'
                val updatedReporteResponse = RetrofitInstance.api.getReporteById(reporteId, currentUserId, currentUserRole)
                if (updatedReporteResponse.isSuccessful) {
                    _uiState.update { it.copy(reporte = updatedReporteResponse.body()) }
                }

            } catch (e: Exception) {
                println("Error al gestionar reacción: ${e.message}")
                // Opcional: Mostrar un error al usuario
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
                    // Eliminar el comentario de la lista local
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
                if (response.isSuccessful) {
                    // Actualizar el comentario en la lista local
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

    // Factory para poder pasar el ID del reporte y las preferencias al ViewModel
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