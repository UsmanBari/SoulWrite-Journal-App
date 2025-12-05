package com.uh.smdprojectsoulwrite

data class Journal(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val imageUrl: String = "",
    val thumbnailUrl: String = "",
    val date: Long = 0,
    val userName: String = "",
    val isPublic: Boolean = false
)

