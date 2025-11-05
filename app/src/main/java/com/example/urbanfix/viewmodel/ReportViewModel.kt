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
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import coil.Coil
import coil.request.ImageRequest

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

    // Debajo de _reportState
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private var editingReportId: Int? = null
    private val _photosChanged = MutableStateFlow(false)

    private var originalPhoto1Url: String? = null
    private var originalPhoto2Url: String? = null

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
            _photosChanged.value = true  // NUEVO
        }
    }

    fun removePhoto(index: Int) {
        _photos.value = _photos.value.filterIndexed { i, _ -> i != index }
        _photosChanged.value = true  // NUEVO
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

    // Nueva función
    fun loadReportForEdit(reporteId: Int, context: Context) { // <-- 1. AÑADIMOS 'context'
        if (reporteId == -1) {
            clearReportData() // Asegura que esté limpio para "Crear"
            _isEditMode.value = false
            editingReportId = null
            return
        }

        _isEditMode.value = true
        editingReportId = reporteId
        _reportState.value = ReportState.Loading
        _photosChanged.value = false

        viewModelScope.launch {
            try {
                val userId = userPreferencesManager.getUserId()
                val userRole = userPreferencesManager.getUserRole() ?: "usuario"

                val response = RetrofitInstance.api.getReporteById(reporteId, userId, userRole)

                if (response.isSuccessful && response.body() != null) {
                    val reporte = response.body()!!

                    _eventAddress.value = reporte.direccion ?: ""
                    _referencePoint.value = reporte.referencia ?: ""
                    _selectedLocation.value = Point.fromLngLat(
                        reporte.longitud.toDoubleOrNull() ?: 0.0,
                        reporte.latitud.toDoubleOrNull() ?: 0.0
                    )
                    _selectedSubtype.value = reporte.nombre ?: ""
                    _description.value = reporte.descripcion ?: ""
                    originalPhoto1Url = reporte.img_prueba_1
                    originalPhoto2Url = reporte.img_prueba_2


                    val loadedBitmaps = mutableListOf<Bitmap>()

                    reporte.img_prueba_1?.let { url ->
                        loadBitmapFromUrl(context, url)?.let { bitmap ->
                            loadedBitmaps.add(bitmap)
                        }
                    }
                    reporte.img_prueba_2?.let { url ->
                        loadBitmapFromUrl(context, url)?.let { bitmap ->
                            loadedBitmaps.add(bitmap)
                        }
                    }

                    _photos.value = loadedBitmaps

                    _reportState.value = ReportState.Idle
                } else {
                    _reportState.value = ReportState.Error(R.string.error_loading_report)
                }
            } catch (e: Exception) {
                _reportState.value = ReportState.Error(R.string.error_network_connection)
            }
        }
    }
    fun submitReport(
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

                // Convertir bitmaps (esto se queda igual)
                val photo1Base64: String
                val photo2Base64: String

                if (_isEditMode.value && !_photosChanged.value) {
                    // En modo edición y fotos NO cambiaron: NO enviar nada
                    photo1Base64 = ""
                    photo2Base64 = ""
                } else {
                    // Fotos cambiaron o es modo crear: convertir a Base64
                    photo1Base64 = if (_photos.value.isNotEmpty()) {
                        ImageUtils.bitmapToBase64(_photos.value[0])
                    } else {
                        if (!_isEditMode.value) {
                            _reportState.value = ReportState.Error(R.string.error_no_photos)
                            return@launch
                        }
                        ""
                    }

                    photo2Base64 = if (_photos.value.size > 1) {
                        ImageUtils.bitmapToBase64(_photos.value[1])
                    } else { "" }
                }

                val categoryId = getCategoryId(reportType)
                val finalDescription = if (_description.value.isNotEmpty()) {
                    _description.value
                } else {
                    "Reporte de ${_selectedSubtype.value}"
                }

                // Crear request (es el mismo para crear y actualizar)
                val request = CreateReporteRequest(
                    descripcion = finalDescription,
                    direccion = _eventAddress.value,
                    referencia = _referencePoint.value,
                    img_prueba_1 = photo1Base64,
                    img_prueba_2 = photo2Base64,
                    latitud = location.latitude().toFloat(),
                    longitud = location.longitude().toFloat(),
                    usuario_creador_id = userId.toString(),
                    categoria_id = categoryId,
                    tipo_evento = _selectedSubtype.value,
                )

                // ===== 2. LA LÓGICA DE DECISIÓN =====
                if (_isEditMode.value && editingReportId != null) {
                    // --- MODO EDICIÓN ---
                    val response = RetrofitInstance.api.updateReporte(editingReportId!!, request)
                    if (response.isSuccessful) {
                        _reportState.value = ReportState.Success
                        onSuccess()
                    } else {
                        _reportState.value = ReportState.Error(R.string.error_updating_report) // Necesitarás añadir este string
                    }
                } else {
                    // --- MODO CREACIÓN ---
                    val response = RetrofitInstance.api.createReporte(request)
                    if (response.isSuccessful) {
                        _reportState.value = ReportState.Success
                        onSuccess()
                    } else {
                        _reportState.value = ReportState.Error(R.string.error_creating_report)
                    }
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

    private suspend fun loadBitmapFromUrl(context: Context, url: String): Bitmap? {
        if (url.isBlank()) return null
        return try {
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .build()
            val result = Coil.imageLoader(context).execute(request).drawable
            (result as? BitmapDrawable)?.bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
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
        _photosChanged.value = false
        originalPhoto1Url = null
        originalPhoto2Url = null
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