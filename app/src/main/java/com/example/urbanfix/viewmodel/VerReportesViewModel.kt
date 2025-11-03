package com.example.urbanfix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.urbanfix.data.ReportesRepository
import com.example.urbanfix.network.MiReporte
import com.example.urbanfix.network.ReactionRemoveRequest
import com.example.urbanfix.network.ReactionRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 1. Define los estados de la UI
sealed interface ReportesUiState {
    data class Success(val reportes: List<MiReporte>) : ReportesUiState
    object Error : ReportesUiState
    object Loading : ReportesUiState
}

// 2. El ViewModel
class VerReportesViewModel(
    private val repository: ReportesRepository,
    private val userId: Int?, // ID del usuario que ha iniciado sesión
    private val userRole: String? // Rol del usuario
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportesUiState>(ReportesUiState.Loading)
    val uiState: StateFlow<ReportesUiState> = _uiState.asStateFlow()

    init {
        fetchReportes()
    }

    fun fetchReportes() {
        viewModelScope.launch {
            _uiState.value = ReportesUiState.Loading
            try {
                // Pasamos el userId para que el API nos diga la reacción actual
                val response = repository.getReportes(userId)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = ReportesUiState.Success(response.body()!!)
                } else {
                    _uiState.value = ReportesUiState.Error
                }
            } catch (e: Exception) {
                _uiState.value = ReportesUiState.Error
            }
        }
    }

    /**
     * Maneja un clic en Like o Dislike
     * @param reporte El reporte que fue clickeado
     * @param newReaction "like", "dislike", o null (para quitar reacción)
     */
    fun handleReaccion(reporte: MiReporte, newReaction: String?) {
        // Requerimos ID y Rol para la API de reacción
        if (userId == null || userRole == null) {
            // Opcional: mostrar un Toast "Debes iniciar sesión"
            return
        }

        val oldReaction = reporte.current_user_reaction

        // Si el usuario hace clic en el mismo botón (ej: 'like' cuando ya tenía 'like')
        val finalReaction = if (oldReaction == newReaction) null else newReaction

        // 1. Actualización Optimista (actualiza la UI inmediatamente)
        updateLocalReportState(reporte.id, finalReaction, oldReaction)

        // 2. Llamada a la API en segundo plano
        viewModelScope.launch {
            try {
                if (finalReaction != null) {
                    // Añadir o actualizar reacción
                    val request = ReactionRequest(userId, userRole, finalReaction)
                    repository.setReaccion(reporte.id, request)
                } else {
                    // Quitar reacción
                    val request = ReactionRemoveRequest(userId, userRole)
                    repository.removeReaccion(reporte.id, request)
                }
                // Éxito: la UI ya está actualizada. Podríamos re-sincronizar
                // por si acaso, pero la actualización optimista es suficiente.

            } catch (e: Exception) {
                // 3. Revertir en caso de error
                // Si la API falla, revierte la UI al estado original
                updateLocalReportState(reporte.id, oldReaction, finalReaction)
                // Opcional: Mostrar Toast de error
            }
        }
    }

    /**
     * Helper para la actualización optimista de la lista
     */
    private fun updateLocalReportState(reporteId: Int, newReaction: String?, oldReaction: String?) {
        if (_uiState.value !is ReportesUiState.Success) return

        _uiState.update { currentState ->
            if (currentState is ReportesUiState.Success) {
                val updatedList = currentState.reportes.map { reporte ->
                    if (reporte.id == reporteId) {
                        // Calcula los nuevos contadores
                        var likes = reporte.apoyos_count
                        var dislikes = reporte.desapoyos_count

                        // Quitar reacción vieja
                        if (oldReaction == "like") likes--
                        if (oldReaction == "dislike") dislikes--

                        // Añadir reacción nueva
                        if (newReaction == "like") likes++
                        if (newReaction == "dislike") dislikes++

                        // Devuelve el reporte actualizado
                        reporte.copy(
                            current_user_reaction = newReaction,
                            apoyos_count = likes.coerceAtLeast(0), // Evita negativos
                            desapoyos_count = dislikes.coerceAtLeast(0) // Evita negativos
                        )
                    } else {
                        reporte // Devuelve los otros reportes sin cambios
                    }
                }
                ReportesUiState.Success(updatedList)
            } else {
                currentState
            }
        }
    }
}

// 3. La Fábrica (Factory) del ViewModel
class VerReportesViewModelFactory(
    private val repository: ReportesRepository,
    private val userId: Int?,
    private val userRole: String?
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VerReportesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VerReportesViewModel(repository, userId, userRole) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}