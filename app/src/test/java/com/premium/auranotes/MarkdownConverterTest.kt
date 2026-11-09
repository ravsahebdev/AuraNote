package com.premium.auranotes

import com.premium.auranotes.data.*
import com.premium.auranotes.util.MarkdownConverter
import org.junit.Assert.*
import org.junit.Test

class MarkdownConverterTest {

    @Test
    fun toMarkdown_convertsHeadingsAndTodos() {
        val note = Note(
            id = 1,
            title = "Project Plan",
            content = listOf(
                Block(type = BlockType.HEADING1, text = "Overview"),
                Block(type = BlockType.TEXT, text = "This is the description."),
                Block(type = BlockType.TODO, text = "Task 1", checked = true),
                Block(type = BlockType.TODO, text = "Task 2", checked = false),
                Block(type = BlockType.BULLET, text = "Point A"),
                Block(type = BlockType.QUOTE, text = "A wise thought.")
            ),
            tags = listOf("work", "plan")
        )

        val md = MarkdownConverter.toMarkdown(note)

        assertTrue(md.contains("# Project Plan"))
        assertTrue(md.contains("# Overview"))
        assertTrue(md.contains("This is the description."))
        assertTrue(md.contains("- [x] Task 1"))
        assertTrue(md.contains("- [ ] Task 2"))
        assertTrue(md.contains("- Point A"))
        assertTrue(md.contains("> A wise thought."))
        assertTrue(md.contains("#work"))
    }

    @Test
    fun toPlainText_convertsCleanly() {
        val note = Note(
            id = 1,
            title = "Meeting Notes",
            content = listOf(
                Block(type = BlockType.TODO, text = "Send email", checked = true),
                Block(type = BlockType.BULLET, text = "Discuss budget")
            )
        )

        val text = MarkdownConverter.toPlainText(note)
        assertTrue(text.contains("Meeting Notes"))
        assertTrue(text.contains("[✓] Send email"))
        assertTrue(text.contains("• Discuss budget"))
    }
}
