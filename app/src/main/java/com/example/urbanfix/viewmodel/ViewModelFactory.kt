package com.example.urbanfix.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.urbanfix.data.UserPreferencesManager

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val userPreferencesManager = UserPreferencesManager(context.applicationContext)

        return when {
            modelClass.isAssignableFrom(UserProfileViewModel::class.java) -> {
                UserProfileViewModel(userPreferencesManager) as T
            }
            modelClass.isAssignableFrom(CompanyProfileViewModel::class.java) -> {
                CompanyProfileViewModel(userPreferencesManager) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}