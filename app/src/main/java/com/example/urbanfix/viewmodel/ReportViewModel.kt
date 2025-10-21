package com.example.urbanfix.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbanfix.R
import com.example.urbanfix.data.UserPreferencesManager
import com.example.urbanfix.network.CreateReporteRequest
import com.example.urbanfix.network.RetrofitInstance
import com.example.urbanfix.utils.ImageUtils
import com.mapbox.geojson.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ReportState {
    object Idle : ReportState
    object Loading : ReportState
    object Success : ReportState
    data class Error(val messageId: Int) : ReportState
}

class ReportViewModel(
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    // ===== PANTALLA 1: REPORTAR =====
    private val _eventAddress = MutableStateFlow("")
    val eventAddress: StateFlow<String> = _eventAddress.asStateFlow()

    private val _referencePoint = MutableStateFlow("")
    val referencePoint: StateFlow<String> = _referencePoint.asStateFlow()

    private val _photos = MutableStateFlow<List<Bitmap>>(emptyList())
    val photos: StateFlow<List<Bitmap>> = _photos.asStateFlow()

    private val _selectedLocation = MutableStateFlow<Point?>(null)
    val selectedLocation: StateFlow<Point?> = _selectedLocation.asStateFlow()

    // ===== PANTALLA 2: REPORTAR DOS =====
    private val _selectedSubtype = MutableStateFlow("")
    val selectedSubtype: StateFlow<String> = _selectedSubtype.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _isGeneratingDescription = MutableStateFlow(false)
    val isGeneratingDescription: StateFlow<Boolean> = _isGeneratingDescription.asStateFlow()

    // ===== ESTADO DEL REPORTE =====
    private val _reportState = MutableStateFlow<ReportState>(ReportState.Idle)
    val reportState: StateFlow<ReportState> = _reportState.asStateFlow()

    // ===== FUNCIONES PANTALLA 1 =====
    fun onEventAddressChange(address: String) {
        _eventAddress.value = address
    }

    fun onReferencePointChange(reference: String) {
        _referencePoint.value = reference
    }

    fun addPhoto(bitmap: Bitmap) {
        if (_photos.value.size < 2) {
            _photos.value = _photos.value + bitmap
        }
    }

    fun removePhoto(index: Int) {
        _photos.value = _photos.value.filterIndexed { i, _ -> i != index }
    }

    fun updateLocation(point: Point) {
        _selectedLocation.value = point
    }

    fun validateStep1(): Boolean {
        return _eventAddress.value.isNotEmpty() && _referencePoint.value.isNotEmpty()
    }

    // ===== FUNCIONES PANTALLA 2 =====
    fun onSubtypeChange(subtype: String) {
        _selectedSubtype.value = subtype
    }

    fun onDescriptionChange(description: String) {
        _description.value = description
    }

    fun setGeneratingDescription(isGenerating: Boolean) {
        _isGeneratingDescription.value = isGenerating
    }

    fun validateStep2(): Boolean {
        return _selectedSubtype.value.isNotEmpty()
    }

    // ===== FUNCIÓN PARA CREAR REPORTE =====
    fun createReport(
        reportType: String,
        onSuccess: () -> Unit
    ) {
        if (!validateStep2()) {
            _reportState.value = ReportState.Error(R.string.incomplete_fields_message)
            return
        }

        viewModelScope.launch {
            _reportState.value = ReportState.Loading

            try {
                val userId = userPreferencesManager.getUserId()
                if (userId == -1) {
                    _reportState.value = ReportState.Error(R.string.error_no_user_id)
                    return@launch
                }

                val location = _selectedLocation.value
                if (location == null) {
                    _reportState.value = ReportState.Error(R.string.error_no_location)
                    return@launch
                }

                // Convertir bitmaps a Base64
                val photo1Base64 = if (_photos.value.isNotEmpty()) {
                    ImageUtils.bitmapToBase64(_photos.value[0])
                } else {
                    // Si la primera foto es obligatoria, validamos aquí
                    _reportState.value = ReportState.Error(R.string.error_no_photos)
                    return@launch
                }

                val photo2Base64 = if (_photos.value.size > 1) {
                    ImageUtils.bitmapToBase64(_photos.value[1])
                } else {
                    "" // Vacío si no hay segunda foto
                }

                // Obtener ID de categoría según el tipo de reporte
                val categoryId = getCategoryId(reportType)

                // Usar descripción o generar una por defecto con el subtipo
                val finalDescription = if (_description.value.isNotEmpty()) {
                    _description.value
                } else {
                    "Reporte de ${_selectedSubtype.value}"
                }

                // Crear request
                val request = CreateReporteRequest(
                    descripcion = finalDescription,
                    direccion = _eventAddress.value,
                    referencia = _referencePoint.value,
                    img_prueba_1 = photo1Base64,
                    img_prueba_2 = photo2Base64,
                    latitud = location.latitude().toFloat(),
                    longitud = location.longitude().toFloat(),
                    usuario_creador_id = userId.toString(), // Asegúrate de que el backend espera String
                    categoria_id = categoryId,
                    tipo_evento = _selectedSubtype.value, // <-- LÍNEA CORREGIDA
                )

                // Enviar al servidor
                val response = RetrofitInstance.api.createReporte(request)

                if (response.isSuccessful) {
                    _reportState.value = ReportState.Success
                    onSuccess()
                } else {
                    _reportState.value = ReportState.Error(R.string.error_creating_report)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _reportState.value = ReportState.Error(R.string.error_network_connection)
            }
        }
    }

    // Mapear tipos de reporte a IDs de categoría
    private fun getCategoryId(reportType: String): Int {
        return when (reportType) {
            "huecos" -> 1
            "alumbrado" -> 2
            "basura" -> 3
            "semaforo" -> 4
            "hidrante" -> 5
            "alcantarilla" -> 6
            else -> 1
        }
    }

    // ===== FUNCIONES DE LIMPIEZA =====
    fun clearReportData() {
        _eventAddress.value = ""
        _referencePoint.value = ""
        _photos.value = emptyList()
        _selectedLocation.value = null
        _selectedSubtype.value = ""
        _description.value = ""
        _isGeneratingDescription.value = false
        _reportState.value = ReportState.Idle
    }

    fun resetReportState() {
        _reportState.value = ReportState.Idle
    }

    // ===== FUNCIONES PARA DEBUGGING =====
    fun getReportSummary(): String {
        return """
            Address: ${_eventAddress.value}
            Reference: ${_referencePoint.value}
            Photos: ${_photos.value.size}
            Location: ${_selectedLocation.value?.let { "(${it.latitude()}, ${it.longitude()})" } ?: "null"}
            Subtype: ${_selectedSubtype.value}
            Description: ${_description.value}
        """.trimIndent()
    }
}

// Data class para facilitar el paso de datos entre pantallas (opcional, ya no necesario)
data class ReportData(
    val eventAddress: String,
    val referencePoint: String,
    val photos: List<Bitmap>,
    val selectedLocation: Point?
)