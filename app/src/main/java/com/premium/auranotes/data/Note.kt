package com.premium.auranotes.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = Folder::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["folderId"])]
)
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "",
    val content: List<Block> = listOf(Block()),
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val color: Long = 0xFF1E1E1E,
    val folderId: Long? = null,
    val category: NoteCategory = NoteCategory.PERSONAL,
    val isFavorite: Boolean = false,
    val isTrashed: Boolean = false,
    val trashedAt: Long? = null,
    val reminderTime: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList(),
    val wordCount: Int = 0,
    val isLocked: Boolean = false
) {
    fun getPlainTextPreview(): String {
        return content.joinToString(" ") { it.text.trim() }.trim()
    }

    fun getTodoListProgress(): Pair<Int, Int>? {
        val todos = content.filter { it.type == BlockType.TODO }
        if (todos.isEmpty()) return null
        val completed = todos.count { it.checked }
        return Pair(completed, todos.size)
    }

    fun getFirstImageUrl(): String? {
        return content.firstOrNull { it.type == BlockType.IMAGE && it.text.isNotBlank() }?.text
    }

    companion object {
        fun calculateWordCount(title: String, blocks: List<Block>): Int {
            val allText = (title + " " + blocks.joinToString(" ") { it.text }).trim()
            if (allText.isBlank()) return 0
            return allText.split(Regex("\\s+")).count { it.isNotBlank() }
        }
    }
}
