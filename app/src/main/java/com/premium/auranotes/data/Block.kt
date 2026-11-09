package com.premium.auranotes.data

import java.util.UUID

enum class BlockType {
    TEXT,
    HEADING1,
    HEADING2,
    HEADING3,
    BULLET,
    NUMBERED,
    TODO,
    TOGGLE,
    QUOTE,
    CODE,
    CALLOUT,
    DIVIDER,
    IMAGE
}

enum class SpanType {
    BOLD,
    ITALIC,
    UNDERLINE,
    STRIKETHROUGH,
    LINK
}

data class SpanData(
    val type: SpanType,
    val start: Int,
    val end: Int,
    val url: String? = null
)

data class Block(
    val id: String = UUID.randomUUID().toString(),
    val type: BlockType = BlockType.TEXT,
    val text: String = "",
    val checked: Boolean = false,
    val indent: Int = 0,
    val spans: List<SpanData> = emptyList()
)
