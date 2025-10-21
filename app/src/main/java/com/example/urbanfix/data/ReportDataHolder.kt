package com.example.urbanfix.data

import android.graphics.Bitmap
import com.mapbox.geojson.Point

/**
 * @deprecated Este objeto está deprecado. Usa ReportViewModel en su lugar.
 * Se mantiene solo por compatibilidad temporal durante la migración.
 */
@Deprecated("Use ReportViewModel instead", ReplaceWith("ReportViewModel"))
object ReportDataHolder {

    // Datos de la pantalla 1
    var eventAddress: String = ""
    var referencePoint: String = ""
    var photos: MutableList<Bitmap> = mutableListOf()
    var selectedLocation: Point? = null

    // Datos de la pantalla 2
    var selectedSubtype: String = ""
    var description: String = ""

    /**
     * Limpia todos los datos almacenados
     */
    fun clear() {
        eventAddress = ""
        referencePoint = ""
        photos.clear()
        selectedLocation = null
        selectedSubtype = ""
        description = ""
    }

    /**
     * Valida que los datos de la pantalla 1 estén completos
     */
    fun validateStep1(): Boolean {
        return eventAddress.isNotEmpty() && referencePoint.isNotEmpty()
    }

    /**
     * Valida que los datos de la pantalla 2 estén completos
     */
    fun validateStep2(): Boolean {
        return selectedSubtype.isNotEmpty()
    }

    fun hasPhotos(): Boolean {
        return photos.isNotEmpty()
    }

    fun addPhoto(bitmap: Bitmap): Boolean {
        return if (photos.size < 2) {
            photos.add(bitmap)
            true
        } else {
            false
        }
    }

    fun removePhoto(index: Int) {
        if (index in photos.indices) {
            photos.removeAt(index)
        }
    }
}