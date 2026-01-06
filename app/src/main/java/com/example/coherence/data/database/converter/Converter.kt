package com.example.coherence.data.database.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    // Convierte una lista de enteros a un String (JSON) para guardarla en la BD
    @TypeConverter
    fun fromListInt(value: List<Int>?): String {
        return Gson().toJson(value)
    }

    // Convierte el String (JSON) de vuelta a una lista de enteros al leer de la BD
    @TypeConverter
    fun toListInt(value: String): List<Int> {
        val listType = object : TypeToken<List<Int>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }
}