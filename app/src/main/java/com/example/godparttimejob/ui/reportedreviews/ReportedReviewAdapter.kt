package com.example.godparttimejob.ui.reportedreviews

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.godparttimejob.R
import com.google.firebase.firestore.FirebaseFirestore

data class ReportedReview(
    val id: String = "",
    val userName: String = "",
    val companyName: String = "",
    val content: String = "",
    val imageUrl: String? = null,
    val userId: String = "",
    val companyId: String = "",
    var reportCount: Long = 0
)

class ReportedReviewAdapter(
    private val reportedReviews: MutableList<ReportedReview>,
    private val db: FirebaseFirestore
) : RecyclerView.Adapter<ReportedReviewAdapter.ReportedReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportedReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reported_review, parent, false)
        return ReportedReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportedReviewViewHolder, position: Int) {
        val review = reportedReviews[position]
        holder.bind(review)
    }

    override fun getItemCount(): Int = reportedReviews.size

    inner class ReportedReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textUserName: TextView = itemView.findViewById(R.id.textUserName)
        private val textCompanyName: TextView = itemView.findViewById(R.id.textCompanyName)
        private val textReviewContent: TextView = itemView.findViewById(R.id.textReviewContent)
        private val imageReviewImage: ImageView = itemView.findViewById(R.id.imageReviewImage)
        private val buttonApprove: Button = itemView.findViewById(R.id.buttonApprove)
        private val buttonDelete: Button = itemView.findViewById(R.id.buttonDelete)

        fun bind(review: ReportedReview) {
            // 리뷰 정보 바인딩
            textUserName.text = review.userName
            textCompanyName.text = review.companyName
            textReviewContent.text = review.content

            if (!review.imageUrl.isNullOrEmpty()) {
                imageReviewImage.visibility = View.VISIBLE
                Glide.with(itemView.context).load(review.imageUrl).into(imageReviewImage)
            } else {
                imageReviewImage.visibility = View.GONE
            }

            // 승인 버튼 클릭 시
            buttonApprove.setOnClickListener {
                db.collection("companies").document(review.companyId)
                    .collection("reviews").document(review.id)
                    .update("reportCount", 0)
                    .addOnSuccessListener {
                        Toast.makeText(itemView.context, "신고가 초기화되었습니다.", Toast.LENGTH_SHORT).show()
                        removeReviewFromList(review)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(itemView.context, "신고 초기화 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }

            // 삭제 버튼 클릭 시
            buttonDelete.setOnClickListener {
                db.collection("companies").document(review.companyId)
                    .collection("reviews").document(review.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(itemView.context, "리뷰가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        updateUserReviewCount(review.userId)
                        removeReviewFromList(review)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(itemView.context, "리뷰 삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        private fun updateUserReviewCount(userId: String) {
            val userRef = db.collection("users").document(userId)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val currentCount = document.getLong("reviews") ?: 0L
                        if (currentCount > 0) {
                            userRef.update("reviews", currentCount - 1)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(itemView.context, "리뷰 수 업데이트 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        private fun removeReviewFromList(review: ReportedReview) {
            val position = reportedReviews.indexOf(review)
            if (position != -1) {
                reportedReviews.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }
}
