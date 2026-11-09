package com.premium.auranotes

import com.premium.auranotes.data.Block
import com.premium.auranotes.data.BlockType
import com.premium.auranotes.data.Note
import org.junit.Assert.*
import org.junit.Test

class NoteTest {

    @Test
    fun getPlainTextPreview_joinsAllBlocks() {
        val note = Note(
            title = "My Note",
            content = listOf(
                Block(text = "First paragraph."),
                Block(text = "Second paragraph.")
            )
        )
        val preview = note.getPlainTextPreview()
        assertEquals("First paragraph. Second paragraph.", preview)
    }

    @Test
    fun getTodoListProgress_returnsCorrectCount() {
        val noteWithTodos = Note(
            content = listOf(
                Block(type = BlockType.TODO, text = "Task 1", checked = true),
                Block(type = BlockType.TODO, text = "Task 2", checked = false),
                Block(type = BlockType.TODO, text = "Task 3", checked = true),
                Block(type = BlockType.TEXT, text = "Not a todo")
            )
        )

        val progress = noteWithTodos.getTodoListProgress()
        assertNotNull(progress)
        assertEquals(2, progress!!.first)
        assertEquals(3, progress.second)
    }

    @Test
    fun getTodoListProgress_returnsNullWhenNoTodos() {
        val note = Note(
            content = listOf(Block(type = BlockType.TEXT, text = "Just text"))
        )
        assertNull(note.getTodoListProgress())
    }

    @Test
    fun calculateWordCount_countsWordsCorrectly() {
        val title = "Aura Note Architecture"
        val blocks = listOf(
            Block(text = "This is a native Android application."),
            Block(text = "Built with Kotlin and Jetpack Compose.")
        )
        val count = Note.calculateWordCount(title, blocks)
        assertEquals(15, count)
    }
}
