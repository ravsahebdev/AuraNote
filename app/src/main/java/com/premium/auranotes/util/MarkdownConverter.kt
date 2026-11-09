package com.premium.auranotes.util

import com.premium.auranotes.data.Block
import com.premium.auranotes.data.BlockType
import com.premium.auranotes.data.Note
import com.premium.auranotes.data.SpanType

object MarkdownConverter {

    fun toMarkdown(note: Note): String {
        val sb = StringBuilder()
        if (note.title.isNotBlank()) {
            sb.append("# ").append(note.title).append("\n\n")
        }

        if (note.tags.isNotEmpty()) {
            sb.append(note.tags.joinToString(" ") { if (it.startsWith("#")) it else "#$it" }).append("\n\n")
        }

        var numberedIndex = 1
        for (i in note.content.indices) {
            val block = note.content[i]
            val indentStr = "  ".repeat(block.indent)
            val formattedText = formatBlockSpans(block)

            when (block.type) {
                BlockType.TEXT -> {
                    sb.append(indentStr).append(formattedText).append("\n\n")
                    numberedIndex = 1
                }
                BlockType.HEADING1 -> {
                    sb.append(indentStr).append("# ").append(formattedText).append("\n\n")
                    numberedIndex = 1
                }
                BlockType.HEADING2 -> {
                    sb.append(indentStr).append("## ").append(formattedText).append("\n\n")
                    numberedIndex = 1
                }
                BlockType.HEADING3 -> {
                    sb.append(indentStr).append("### ").append(formattedText).append("\n\n")
                    numberedIndex = 1
                }
                BlockType.BULLET -> {
                    sb.append(indentStr).append("- ").append(formattedText).append("\n")
                    numberedIndex = 1
                }
                BlockType.NUMBERED -> {
                    sb.append(indentStr).append("$numberedIndex. ").append(formattedText).append("\n")
                    numberedIndex++
                }
                BlockType.TODO -> {
                    val box = if (block.checked) "[x]" else "[ ]"
                    sb.append(indentStr).append("- ").append(box).append(" ").append(formattedText).append("\n")
                    numberedIndex = 1
                }
                BlockType.TOGGLE -> {
                    sb.append(indentStr).append("<details><summary>").append(formattedText).append("</summary></details>\n\n")
                    numberedIndex = 1
                }
                BlockType.QUOTE -> {
                    sb.append(indentStr).append("> ").append(formattedText).append("\n\n")
                    numberedIndex = 1
                }
                BlockType.CODE -> {
                    sb.append(indentStr).append("```\n").append(block.text).append("\n```\n\n")
                    numberedIndex = 1
                }
                BlockType.CALLOUT -> {
                    sb.append(indentStr).append("> 💡 ").append(formattedText).append("\n\n")
                    numberedIndex = 1
                }
                BlockType.DIVIDER -> {
                    sb.append("---\n\n")
                    numberedIndex = 1
                }
                BlockType.IMAGE -> {
                    sb.append(indentStr).append("![Image](").append(block.text).append(")\n\n")
                    numberedIndex = 1
                }
            }
        }
        return sb.toString().trim()
    }

    fun toPlainText(note: Note): String {
        val sb = StringBuilder()
        if (note.title.isNotBlank()) {
            sb.append(note.title).append("\n\n")
        }
        for (block in note.content) {
            val prefix = when (block.type) {
                BlockType.BULLET -> "• "
                BlockType.NUMBERED -> "1. "
                BlockType.TODO -> if (block.checked) "[✓] " else "[ ] "
                BlockType.QUOTE -> "“ "
                BlockType.CALLOUT -> "💡 "
                BlockType.DIVIDER -> "—————————————"
                else -> ""
            }
            if (block.type == BlockType.DIVIDER) {
                sb.append(prefix).append("\n")
            } else if (block.text.isNotBlank()) {
                sb.append(prefix).append(block.text).append("\n")
            }
        }
        return sb.toString().trim()
    }

    private fun formatBlockSpans(block: Block): String {
        if (block.spans.isEmpty() || block.text.isEmpty()) return block.text
        var text = block.text
        for (span in block.spans.sortedByDescending { it.start }) {
            if (span.start >= 0 && span.end <= text.length && span.start <= span.end) {
                val prefix = text.substring(0, span.start)
                val target = text.substring(span.start, span.end)
                val suffix = text.substring(span.end)
                val wrapped = when (span.type) {
                    SpanType.BOLD -> "**$target**"
                    SpanType.ITALIC -> "*$target*"
                    SpanType.UNDERLINE -> "<u>$target</u>"
                    SpanType.STRIKETHROUGH -> "~~$target~~"
                    SpanType.LINK -> "[$target](${span.url ?: ""})"
                }
                text = prefix + wrapped + suffix
            }
        }
        return text
    }
}
