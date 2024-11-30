package com.example.godparttimejob.ui.companydetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.godparttimejob.R

data class Review(
    val userId: String = "",
    val userName: String = "",
    val workIntensity: Int = 0,
    val salary: Int = 0,
    val environment: Int = 0,
    val comment: String = "",
    val image1: String? = null,
    val image2: String? = null,
    val averageRating: Double = 0.0
)

class ReviewAdapter(private val reviews: List<Review>) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textWorkIntensity: TextView = view.findViewById(R.id.textWorkIntensity)
        val textSalary: TextView = view.findViewById(R.id.textSalary)
        val textEnvironment: TextView = view.findViewById(R.id.textEnvironment)
        val textComment: TextView = view.findViewById(R.id.textReviewComment)
        val image1: ImageView = view.findViewById(R.id.imageReview1)
        val image2: ImageView = view.findViewById(R.id.imageReview2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]

        holder.textWorkIntensity.text = "근무 강도: ${review.workIntensity}/5"
        holder.textSalary.text = "급여: ${review.salary}/5"
        holder.textEnvironment.text = "환경: ${review.environment}/5"
        holder.textComment.text = review.comment

        if (!review.image1.isNullOrEmpty()) {
            holder.image1.visibility = View.VISIBLE
            Glide.with(holder.image1.context).load(review.image1).into(holder.image1)
        } else {
            holder.image1.visibility = View.GONE
        }

        if (!review.image2.isNullOrEmpty()) {
            holder.image2.visibility = View.VISIBLE
            Glide.with(holder.image2.context).load(review.image2).into(holder.image2)
        } else {
            holder.image2.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return reviews.size
    }
}
