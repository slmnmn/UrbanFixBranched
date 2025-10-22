package com.example.urbanfix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbanfix.network.MiReporte // Asegúrate de importar tu data class
import com.example.urbanfix.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MisReportesViewModel : ViewModel() {

    private val _reportes = MutableStateFlow<List<MiReporte>>(emptyList())
    val reportes = _reportes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun fetchMisReportes(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.getMisReportes(userId)
                _reportes.value = response
            } catch (e: Exception) {
                e.printStackTrace()
                _reportes.value = emptyList() // Limpia en caso de error
            } finally {
                _isLoading.value = false
            }
        }
    }
}