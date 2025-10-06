package com.example.urbanfix.data

import android.content.Context
import android.content.SharedPreferences

class UserPreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("urbanfix_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val REMEMBER_ME_KEY = "remember_me"
        private const val EMAIL_KEY = "saved_email"
        private const val PASSWORD_KEY = "saved_password"
    }

    // Guardar las credenciales
    fun saveCredentials(email: String, password: String, rememberMe: Boolean) {
        sharedPreferences.edit().apply {
            putBoolean(REMEMBER_ME_KEY, rememberMe)
            if (rememberMe) {
                putString(EMAIL_KEY, email)
                putString(PASSWORD_KEY, password)
            } else {
                // Si no se activa "Recordarme", limpiamos las credenciales
                remove(EMAIL_KEY)
                remove(PASSWORD_KEY)
            }
            apply()
        }
    }

    // Obtener si está activado "Recordarme"
    fun getRememberMe(): Boolean {
        return sharedPreferences.getBoolean(REMEMBER_ME_KEY, false)
    }

    // Obtener el email guardado
    fun getSavedEmail(): String {
        return sharedPreferences.getString(EMAIL_KEY, "") ?: ""
    }

    // Obtener la contraseña guardada
    fun getSavedPassword(): String {
        return sharedPreferences.getString(PASSWORD_KEY, "") ?: ""
    }

    // Limpiar todas las credenciales (para logout)
    fun clearCredentials() {
        sharedPreferences.edit().apply {
            remove(REMEMBER_ME_KEY)
            remove(EMAIL_KEY)
            remove(PASSWORD_KEY)
            apply()
        }
    }
}