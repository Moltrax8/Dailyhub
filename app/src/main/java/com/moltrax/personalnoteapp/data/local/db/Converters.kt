package com.moltrax.personalnoteapp.data.local.db

import androidx.room.TypeConverter
import com.moltrax.personalnoteapp.domain.model.SubTask
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter fun fromStringList(value: List<String>): String = json.encodeToString(value)
    @TypeConverter fun toStringList(value: String): List<String> = json.decodeFromString(value)

    // Alt görevler (checklist) görev satırına gömülü JSON olarak saklanır.
    @TypeConverter fun fromSubTaskList(value: List<SubTask>): String = json.encodeToString(value)
    @TypeConverter fun toSubTaskList(value: String): List<SubTask> = json.decodeFromString(value)

    // Haftalık tekrar günleri (ISO 1..7) JSON liste olarak saklanır.
    @TypeConverter fun fromIntList(value: List<Int>): String = json.encodeToString(value)
    @TypeConverter fun toIntList(value: String): List<Int> = json.decodeFromString(value)
}
