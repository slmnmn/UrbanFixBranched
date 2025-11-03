package com.example.urbanfix.viewmodel // O el paquete que uses para ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.urbanfix.data.ReportesRepository // (Del paso 1)
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 1. Define los posibles estados de la UI del mapa
sealed interface MapaUiState {
    object Loading : MapaUiState
    data class Success(val geoJsonData: String) : MapaUiState // Pasará el JSON como String
    data class Error(val message: String) : MapaUiState
}

// 2. El ViewModel
class MapaViewModel(private val reportesRepository: ReportesRepository) : ViewModel() {

    // Flujo de estado privado y mutable
    private val _uiState = MutableStateFlow<MapaUiState>(MapaUiState.Loading)
    // Flujo de estado público e inmutable para la UI
    val uiState: StateFlow<MapaUiState> = _uiState.asStateFlow()

    init {
        // Carga los datos en cuanto el ViewModel se inicializa
        fetchReportesGeoJson()
    }

    fun fetchReportesGeoJson() {
        viewModelScope.launch {
            _uiState.value = MapaUiState.Loading
            try {
                // Llama al repositorio
                val response = reportesRepository.getReportesGeoJson()

                if (response.isSuccessful && response.body() != null) {
                    // ¡Éxito! Convierte el JsonObject a String
                    _uiState.value = MapaUiState.Success(response.body().toString())
                } else {
                    // Error del servidor
                    _uiState.value = MapaUiState.Error(response.message())
                }
            } catch (e: Exception) {
                // Error de red u otra excepción
                _uiState.value = MapaUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}

// 3. La Fábrica (Factory) del ViewModel
// Esto es necesario para poder "inyectar" el repositorio en el constructor del ViewModel
class MapaViewModelFactory(
    private val reportesRepository: ReportesRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapaViewModel(reportesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}