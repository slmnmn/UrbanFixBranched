package com.example.urbanfix.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * Utilidad centralizada para manejar conversiones de imágenes
 * Convierte automáticamente Uri -> Bitmap -> Base64
 */
object ImageUtils {

    /**
     * Convierte un Uri a Bitmap
     * Funciona para galería y cámara
     */
    fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Redimensiona un Bitmap manteniendo la proporción
     */
    fun resizeBitmap(bitmap: Bitmap, maxSize: Int = 512): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            newWidth = maxSize
            newHeight = (maxSize / ratio).toInt()
        } else {
            newHeight = maxSize
            newWidth = (maxSize * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Convierte Bitmap a Base64
     */
    fun bitmapToBase64(bitmap: Bitmap, quality: Int = 85): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    /**
     * Convierte Base64 a Bitmap
     */
    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun processImageFromUri(context: Context, uri: Uri, maxSize: Int = 512): Bitmap? {
        val originalBitmap = uriToBitmap(context, uri) ?: return null
        val resizedBitmap = resizeBitmap(originalBitmap, maxSize)

        // Liberar memoria del bitmap original si es diferente
        if (resizedBitmap != originalBitmap) {
            originalBitmap.recycle()
        }

        return resizedBitmap
    }
}