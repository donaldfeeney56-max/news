package com.newsapp.app.data.local

import androidx.room.TypeConverter
import com.newsapp.app.data.model.SportCategory

class Converters {
    @TypeConverter
    fun fromSportCategory(value: SportCategory): String = value.name

    @TypeConverter
    fun toSportCategory(value: String): SportCategory = SportCategory.valueOf(value)
}
