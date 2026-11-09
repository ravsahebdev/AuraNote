package com.premium.auranotes.data

data class FolderWithCount(
    val id: Long,
    val name: String,
    val iconEmoji: String,
    val createdAt: Long,
    val noteCount: Int
)
