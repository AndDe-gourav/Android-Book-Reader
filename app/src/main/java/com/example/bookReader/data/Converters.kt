package com.example.bookReader.data

import androidx.room.TypeConverter
import com.example.bookReader.data.entity.ReadingStatus

class Converters {

    @TypeConverter
    fun toStatus(value: String): ReadingStatus = ReadingStatus.valueOf(value)

    @TypeConverter
    fun fromStatus(status: ReadingStatus): String = status.name
}
