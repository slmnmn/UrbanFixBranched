package com.example.urbanfix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CompanyProfileState {
    object Idle : CompanyProfileState
    object Loading : CompanyProfileState
    data class Success(val companyName: String, val userEmail: String) : CompanyProfileState
    data class Error(val message: String) : CompanyProfileState
}

class CompanyProfileViewModel : ViewModel() {

    private val _profileState = MutableStateFlow<CompanyProfileState>(CompanyProfileState.Idle)
    val profileState = _profileState.asStateFlow()

    init {
        loadCompanyProfile()
    }

    private fun loadCompanyProfile() {
        viewModelScope.launch {
            _profileState.value = CompanyProfileState.Loading
            delay(1000)
            _profileState.value = CompanyProfileState.Success(
                companyName = "Nombre de Empresa S.A.",
                userEmail = "contacto@empresa.com"
            )
        }
    }
}