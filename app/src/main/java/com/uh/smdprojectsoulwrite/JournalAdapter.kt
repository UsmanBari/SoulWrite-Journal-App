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
        val authorName: TextView = itemView.findViewById(R.id.author_name)
        val likeCommentLayout: View = itemView.findViewById(R.id.like_comment_layout)
        val likeCount: TextView = itemView.findViewById(R.id.like_count)
        val commentCount: TextView = itemView.findViewById(R.id.comment_count)

        fun bind(journal: Journal) {
            titleText.text = journal.title

            // Show author name for public journals
            if (journal.isPublic && journal.authorName.isNotEmpty()) {
                authorName.text = "by ${journal.authorName}"
                authorName.visibility = View.VISIBLE
                likeCommentLayout.visibility = View.VISIBLE
                likeCount.text = "❤ ${journal.likeCount}"
                commentCount.text = "💬 ${journal.commentCount}"
            } else {
                authorName.visibility = View.GONE
                likeCommentLayout.visibility = View.GONE
            }

            // Format date
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            dateText.text = sdf.format(Date(journal.date))

            // Show preview of content (first 50 characters)
            contentText.text = if (journal.content.length > 50) {
                journal.content.substring(0, 50) + "..."
            } else {
                journal.content
            }

            // Load thumbnail or full image
            val imageToLoad = when {
                journal.thumbnailUrl.isNotEmpty() -> journal.thumbnailUrl
                journal.imageUrl.isNotEmpty() -> journal.imageUrl
                else -> null
            }

            android.util.Log.d("JournalAdapter", "=== Loading Journal ${journal.id} ===")
            android.util.Log.d("JournalAdapter", "Title: ${journal.title}")
            android.util.Log.d("JournalAdapter", "thumbnailUrl: '${journal.thumbnailUrl}'")
            android.util.Log.d("JournalAdapter", "imageUrl: '${journal.imageUrl}'")
            android.util.Log.d("JournalAdapter", "Will attempt to load: $imageToLoad")

            if (imageToLoad != null) {
                android.util.Log.d("JournalAdapter", "Starting Glide load for URL: $imageToLoad")
                Glide.with(itemView.context)
                    .load(imageToLoad)
                    .placeholder(R.drawable.image1)
                    .error(R.drawable.image1)
                    .centerCrop()
                    .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                        override fun onLoadFailed(
                            e: com.bumptech.glide.load.engine.GlideException?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            android.util.Log.e("JournalAdapter", "❌ Glide FAILED to load: $imageToLoad")
                            android.util.Log.e("JournalAdapter", "Error: ${e?.message}")
                            e?.logRootCauses("JournalAdapter")
                            return false
                        }

                        override fun onResourceReady(
                            resource: android.graphics.drawable.Drawable?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                            dataSource: com.bumptech.glide.load.DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            android.util.Log.d("JournalAdapter", "✅ Glide SUCCESS loaded: $imageToLoad")
                            return false
                        }
                    })
                    .into(thumbnailImage)
            } else {
                android.util.Log.w("JournalAdapter", "❌ No image URL available, showing placeholder")
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

