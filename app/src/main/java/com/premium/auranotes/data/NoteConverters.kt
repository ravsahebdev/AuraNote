package com.premium.auranotes.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NoteConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromBlockList(blocks: List<Block>?): String {
        return if (blocks.isNullOrEmpty()) "[]" else gson.toJson(blocks)
    }

    @TypeConverter
    fun toBlockList(json: String?): List<Block> {
        if (json.isNullOrBlank()) return listOf(Block())
        return try {
            val trimmed = json.trim()
            if (trimmed.startsWith("[")) {
                val type = object : TypeToken<List<Block>>() {}.type
                val parsed: List<Block>? = gson.fromJson(json, type)
                if (parsed.isNullOrEmpty()) listOf(Block()) else parsed
            } else {
                // Graceful fallback for version 1 plain text content
                listOf(Block(text = json))
            }
        } catch (e: Exception) {
            listOf(Block(text = json))
        }
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return if (list.isNullOrEmpty()) "[]" else gson.toJson(list)
    }

    @TypeConverter
    fun toStringList(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val trimmed = json.trim()
            if (trimmed.startsWith("[")) {
                val type = object : TypeToken<List<String>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromCategory(category: NoteCategory?): String {
        return (category ?: NoteCategory.PERSONAL).name
    }

    @TypeConverter
    fun toCategory(value: String?): NoteCategory {
        return try {
            if (value != null) NoteCategory.valueOf(value) else NoteCategory.PERSONAL
        } catch (e: Exception) {
            NoteCategory.PERSONAL
        }
    }
}
