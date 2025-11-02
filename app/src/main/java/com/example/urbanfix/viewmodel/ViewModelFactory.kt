package com.example.urbanfix.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.urbanfix.data.UserPreferencesManager

class ViewModelFactory(
    private val context: Context,
    private val otherUserId: Int? = null,
    private val otherUserRole: String? = null // NUEVO: Parámetro para el rol
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val userPreferencesManager = UserPreferencesManager(context.applicationContext)

        return when {
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                // Pasar tanto el otherUserId como el otherUserRole
                ProfileViewModel(userPreferencesManager, otherUserId, otherUserRole) as T
            }
            modelClass.isAssignableFrom(ReportViewModel::class.java) -> {
                ReportViewModel(userPreferencesManager) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}