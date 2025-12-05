package com.uh.smdprojectsoulwrite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class JournalAdapter(
    private val journals: List<Journal>,
    private val onItemClick: (Journal) -> Unit
) : RecyclerView.Adapter<JournalAdapter.JournalViewHolder>() {

    inner class JournalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnailImage: ImageView = itemView.findViewById(R.id.journal_thumbnail)
        val titleText: TextView = itemView.findViewById(R.id.journal_title)
        val dateText: TextView = itemView.findViewById(R.id.journal_date)
        val contentText: TextView = itemView.findViewById(R.id.journal_preview)

        fun bind(journal: Journal) {
            titleText.text = journal.title

            // Format date
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            dateText.text = sdf.format(Date(journal.date))

            // Show preview of content (first 50 characters)
            contentText.text = if (journal.content.length > 50) {
                journal.content.substring(0, 50) + "..."
            } else {
                journal.content
            }

            // Load thumbnail image
            if (journal.thumbnailUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(journal.thumbnailUrl)
                    .placeholder(R.drawable.image1)
                    .into(thumbnailImage)
            } else {
                thumbnailImage.setImageResource(R.drawable.image1)
            }

            itemView.setOnClickListener {
                onItemClick(journal)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_journal, parent, false)
        return JournalViewHolder(view)
    }

    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        holder.bind(journals[position])
    }

    override fun getItemCount() = journals.size
}

