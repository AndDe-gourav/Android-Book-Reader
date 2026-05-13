package com.timepass.bookreader.data

import androidx.room.TypeConverter
import com.timepass.bookreader.data.entity.ReadingStatus

class Converters {

    @TypeConverter
    fun toStatus(value: String): ReadingStatus = ReadingStatus.valueOf(value)

    @TypeConverter
    fun fromStatus(status: ReadingStatus): String = status.name
}
