package com.uh.smdprojectsoulwrite

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val profileImageUrl: String = "",
    val following: List<String> = emptyList()
)

