package com.premium.auranotes

import com.premium.auranotes.data.*
import org.junit.Assert.*
import org.junit.Test

class NoteConvertersTest {

    private val converters = NoteConverters()

    @Test
    fun blockList_serializationAndDeserialization_preservesData() {
        val original = listOf(
            Block(id = "1", type = BlockType.HEADING1, text = "Heading Text"),
            Block(id = "2", type = BlockType.TODO, text = "Buy milk", checked = true, indent = 1),
            Block(id = "3", type = BlockType.CODE, text = "val x = 10")
        )

        val json = converters.fromBlockList(original)
        assertNotNull(json)
        assertTrue(json.startsWith("["))

        val restored = converters.toBlockList(json)
        assertEquals(3, restored.size)
        assertEquals("Heading Text", restored[0].text)
        assertEquals(BlockType.HEADING1, restored[0].type)
        assertEquals(true, restored[1].checked)
        assertEquals(1, restored[1].indent)
        assertEquals(BlockType.CODE, restored[2].type)
    }

    @Test
    fun legacyPlainText_convertsGracefullyToBlockList() {
        val legacyText = "This was created in Version 1 before blocks existed."
        val blocks = converters.toBlockList(legacyText)

        assertEquals(1, blocks.size)
        assertEquals(BlockType.TEXT, blocks[0].type)
        assertEquals(legacyText, blocks[0].text)
    }

    @Test
    fun stringList_serializationAndDeserialization() {
        val tags = listOf("ideas", "personal", "finance")
        val json = converters.fromStringList(tags)
        val restored = converters.toStringList(json)

        assertEquals(3, restored.size)
        assertEquals("ideas", restored[0])
        assertEquals("personal", restored[1])
        assertEquals("finance", restored[2])
    }

    @Test
    fun category_serialization() {
        val cat = NoteCategory.WORK
        val name = converters.fromCategory(cat)
        val restored = converters.toCategory(name)
        assertEquals(NoteCategory.WORK, restored)
    }
}
