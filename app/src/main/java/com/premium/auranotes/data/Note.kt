package com.premium.auranotes.data
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val color: Long = 0xFF2B2B2B
)
