package com.example.urbanfix.data

import android.graphics.Bitmap
import com.mapbox.geojson.Point

object ReportDataHolder {
    var eventAddress: String = ""
    var referencePoint: String = ""
    var photos: List<Bitmap> = emptyList()
    var selectedLocation: Point? = null

    fun setData(address: String, reference: String, photoList: List<Bitmap>, location: Point?) {
        eventAddress = address
        referencePoint = reference
        photos = photoList
        selectedLocation = location
    }

    fun clearData() {
        eventAddress = ""
        referencePoint = ""
        photos = emptyList()
        selectedLocation = null
    }
}