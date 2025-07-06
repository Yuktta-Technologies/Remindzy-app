package com.app.reminderpro.model

import androidx.room.TypeConverter

class Converters {

    @Suppress("unused")
    @TypeConverter
    fun fromRepeatMode(value: RepeatMode): String {
        return value.name
    }

    @Suppress("unused")
    @TypeConverter
    fun toRepeatMode(value: String): RepeatMode {
        return RepeatMode.valueOf(value)
    }
}
