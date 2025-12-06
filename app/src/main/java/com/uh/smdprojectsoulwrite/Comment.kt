package com.uh.smdprojectsoulwrite

data class Comment(
    val id: Int = 0,
    val journalId: Int = 0,
    val userId: Int = 0,
    val userName: String = "",
    val commentText: String = "",
    val createdAt: String = ""
)

