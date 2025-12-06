package com.uh.smdprojectsoulwrite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotificationAdapter(
    private val notifications: List<NotificationItem>,
    private val onItemClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.notif_title)
        val messageText: TextView = itemView.findViewById(R.id.notif_message)
        val timeText: TextView = itemView.findViewById(R.id.notif_time)

        fun bind(notification: NotificationItem) {
            titleText.text = notification.title
            messageText.text = notification.message
            timeText.text = formatTime(notification.createdAt)

            itemView.setOnClickListener {
                onItemClick(notification)
            }

            // Slightly transparent if already read
            itemView.alpha = if (notification.isRead) 0.6f else 1.0f
        }

        private fun formatTime(timestamp: String): String {
            try {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                val date = sdf.parse(timestamp)
                val now = java.util.Date()
                val diff = now.time - (date?.time ?: 0)

                val seconds = diff / 1000
                val minutes = seconds / 60
                val hours = minutes / 60
                val days = hours / 24

                return when {
                    days > 0 -> "${days}d ago"
                    hours > 0 -> "${hours}h ago"
                    minutes > 0 -> "${minutes}m ago"
                    else -> "Just now"
                }
            } catch (e: Exception) {
                return timestamp
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount() = notifications.size
}

