package com.example.urbanfix.data

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import com.example.urbanfix.utils.ImageUtils

class UserPreferencesManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("urbanfix_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val REMEMBER_ME_KEY = "remember_me"
        private const val EMAIL_KEY = "saved_email"
        private const val PASSWORD_KEY = "saved_password"

        private const val USER_ID_KEY = "user_id"
        private const val USER_NAME_KEY = "user_name"
        private const val USER_EMAIL_KEY = "user_email"
        private const val USER_PHONE_KEY = "user_phone"
        private const val USER_ROLE_KEY = "user_role"
        private const val COMPANY_NAME_KEY = "company_name"
        private const val REGISTRATION_DATE_KEY = "registration_date"
        private const val PROFILE_PIC_BASE64_KEY = "profile_pic_base64"
    }

    fun saveUserData(id: Int, name: String, email: String, phone: String?, role: String, companyName: String?, registrationDate: String) {
        sharedPreferences.edit().apply {
            putInt(USER_ID_KEY, id)
            putString(USER_NAME_KEY, name)
            putString(USER_EMAIL_KEY, email)
            putString(USER_PHONE_KEY, phone ?: "")
            putString(USER_ROLE_KEY, role)
            putString(COMPANY_NAME_KEY, companyName ?: "")
            putString(REGISTRATION_DATE_KEY, registrationDate)
            apply()
        }
    }

    fun getUserId(): Int = sharedPreferences.getInt(USER_ID_KEY, -1)
    fun getUserName(): String = sharedPreferences.getString(USER_NAME_KEY, "Usuario") ?: "Usuario"
    fun getCompanyName(): String = sharedPreferences.getString(COMPANY_NAME_KEY, "") ?: ""
    fun getUserEmail(): String = sharedPreferences.getString(USER_EMAIL_KEY, "") ?: ""
    fun getUserPhone(): String = sharedPreferences.getString(USER_PHONE_KEY, "") ?: ""
    fun getUserRole(): String = sharedPreferences.getString(USER_ROLE_KEY, "usuario") ?: "usuario"
    fun getRegistrationDate(): String = sharedPreferences.getString(REGISTRATION_DATE_KEY, "") ?: ""

    fun saveCredentials(email: String, password: String, rememberMe: Boolean) {
        sharedPreferences.edit().apply {
            putBoolean(REMEMBER_ME_KEY, rememberMe)
            if (rememberMe) {
                putString(EMAIL_KEY, email)
                putString(PASSWORD_KEY, password)
            } else {
                remove(EMAIL_KEY)
                remove(PASSWORD_KEY)
            }
            apply()
        }
    }

    fun getRememberMe(): Boolean = sharedPreferences.getBoolean(REMEMBER_ME_KEY, false)
    fun getSavedEmail(): String = sharedPreferences.getString(EMAIL_KEY, "") ?: ""
    fun getSavedPassword(): String = sharedPreferences.getString(PASSWORD_KEY, "") ?: ""

    fun clearCredentials() {
        sharedPreferences.edit().apply {
            remove(USER_ID_KEY)
            remove(USER_NAME_KEY)
            remove(USER_EMAIL_KEY)
            remove(USER_PHONE_KEY)
            remove(USER_ROLE_KEY)
            remove(COMPANY_NAME_KEY)
            remove(REGISTRATION_DATE_KEY)
            apply()
        }
    }

    fun deleteAllData() {
        sharedPreferences.edit().clear().apply()
    }

    fun saveUserName(newName: String) {
        sharedPreferences.edit().putString(USER_NAME_KEY, newName).apply()
    }
    // FUNCIONES SIMPLIFICADAS PARA IMAGEN DE PERFIL

    fun saveProfilePicFromUri(uri: Uri): Boolean {
        return try {
            val bitmap = ImageUtils.processImageFromUri(context, uri)
            if (bitmap != null) {
                saveProfilePicBitmap(bitmap)
                bitmap.recycle()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun saveProfilePicBitmap(bitmap: Bitmap) {
        try {
            val base64String = ImageUtils.bitmapToBase64(bitmap)
            sharedPreferences.edit().putString(PROFILE_PIC_BASE64_KEY, base64String).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getProfilePicBitmap(): Bitmap? {
        val base64String = sharedPreferences.getString(PROFILE_PIC_BASE64_KEY, null)
        return base64String?.let { ImageUtils.base64ToBitmap(it) }
    }

    fun getProfilePicBase64(): String? {
        return sharedPreferences.getString(PROFILE_PIC_BASE64_KEY, null)
    }

    fun deleteProfilePic() {
        sharedPreferences.edit().remove(PROFILE_PIC_BASE64_KEY).apply()
    }

    fun hasProfilePic(): Boolean {
        return sharedPreferences.contains(PROFILE_PIC_BASE64_KEY)
    }
}