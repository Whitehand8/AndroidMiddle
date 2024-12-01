package com.example.godparttimejob.ui.reviews

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.godparttimejob.R
import com.google.firebase.firestore.FirebaseFirestore

class UserReviewsAdapter(
    private val reviews: List<UserReview>,
    private val db: FirebaseFirestore
) : RecyclerView.Adapter<UserReviewsAdapter.UserReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_review, parent, false)
        return UserReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.bind(review)
    }

    override fun getItemCount(): Int = reviews.size

    inner class UserReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textReviewTitle: TextView = itemView.findViewById(R.id.textReviewTitle)
        private val textReviewContent: TextView = itemView.findViewById(R.id.textReviewContent)
        private val textLikes: TextView = itemView.findViewById(R.id.textLikes)

        fun bind(review: UserReview) {
            textReviewTitle.text = "${review.userName}님의 리뷰"
            textReviewContent.text = review.comment
            textLikes.text = "좋아요: ${review.likes}"

            itemView.setOnClickListener {
                Toast.makeText(itemView.context, "${review.comment} 클릭됨", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
