package com.example.godparttimejob.ui.companydetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.godparttimejob.R

// 데이터 모델 클래스 (리뷰 데이터)
data class Review(
    val userName: String = "",
    val comment: String = "",
    val averageRating: Double = 0.0
)

// RecyclerView Adapter
class ReviewAdapter(private val reviews: List<Review>) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    // ViewHolder: 각 항목의 View 관리
    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textUserName: TextView = itemView.findViewById(R.id.textUserName)
        val textComment: TextView = itemView.findViewById(R.id.textComment)
        val textAverageRating: TextView = itemView.findViewById(R.id.textAverageRating)
    }

    // ViewHolder 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    // ViewHolder와 데이터를 바인딩
    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.textUserName.text = review.userName
        holder.textComment.text = review.comment
        holder.textAverageRating.text = "평균 평점: ${review.averageRating}"
    }

    // 데이터 개수 반환
    override fun getItemCount(): Int = reviews.size
}
