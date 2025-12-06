package com.uh.smdprojectsoulwrite

import org.json.JSONObject

data class NotificationItem(
    val id: Int,
    val type: String,
    val title: String,
    val message: String,
    val fromUserName: String = "Someone",
    val isRead: Boolean = false,
    val createdAt: String,
    val data: JSONObject? = null
)

