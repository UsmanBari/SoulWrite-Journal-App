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
    val userFcmToken: String = "",
    val isPublic: Boolean = false,
    val authorName: String = "",
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    var isLiked: Boolean = false
)

