package com.example.godparttimejob.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.godparttimejob.R
import com.google.firebase.firestore.FirebaseFirestore

class ReportedReviewsFragment : Fragment() {

    private lateinit var recyclerReportedReviews: RecyclerView
    private lateinit var db: FirebaseFirestore
    private val reportedReviewsList = mutableListOf<ReportedReview>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reported_reviews, container, false)

        db = FirebaseFirestore.getInstance()
        recyclerReportedReviews = view.findViewById(R.id.recyclerReportedReviews)
        recyclerReportedReviews.layoutManager = LinearLayoutManager(requireContext())

        loadReportedReviews()

        return view
    }

    private fun loadReportedReviews() {
        db.collection("reported_reviews")
            .whereGreaterThanOrEqualTo("reportCount", 15)
            .get()
            .addOnSuccessListener { querySnapshot ->
                reportedReviewsList.clear()
                for (document in querySnapshot) {
                    val reportedReview = document.toObject(ReportedReview::class.java)
                    reportedReview.id = document.id
                    reportedReviewsList.add(reportedReview)
                }
                recyclerReportedReviews.adapter = ReportedReviewsAdapter(reportedReviewsList)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "신고된 리뷰를 로드할 수 없습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    inner class ReportedReviewsAdapter(
        private val reportedReviews: List<ReportedReview>
    ) : RecyclerView.Adapter<ReportedReviewsAdapter.ReportedReviewViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportedReviewViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_reported_review, parent, false)
            return ReportedReviewViewHolder(view)
        }

        override fun onBindViewHolder(holder: ReportedReviewViewHolder, position: Int) {
            holder.bind(reportedReviews[position])
        }

        override fun getItemCount(): Int = reportedReviews.size

        inner class ReportedReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val imageProfile: ImageView = itemView.findViewById(R.id.imageProfile)
            private val textUserName: TextView = itemView.findViewById(R.id.textUserName)
            private val textCompanyName: TextView = itemView.findViewById(R.id.textCompanyName)
            private val textReviewContent: TextView = itemView.findViewById(R.id.textReviewContent)
            private val imageReviewImage: ImageView = itemView.findViewById(R.id.imageReviewImage)
            private val buttonApprove: Button = itemView.findViewById(R.id.buttonApprove)
            private val buttonDelete: Button = itemView.findViewById(R.id.buttonDelete)

            fun bind(reportedReview: ReportedReview) {
                textUserName.text = reportedReview.userName
                textCompanyName.text = reportedReview.companyName
                textReviewContent.text = reportedReview.content

                Glide.with(itemView.context)
                    .load(reportedReview.profileImageUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(imageProfile)

                if (!reportedReview.imageUrl.isNullOrEmpty()) {
                    Glide.with(itemView.context)
                        .load(reportedReview.imageUrl)
                        .into(imageReviewImage)
                }

                buttonApprove.setOnClickListener { approveReview(reportedReview) }
                buttonDelete.setOnClickListener { deleteReview(reportedReview) }
            }

            private fun approveReview(reportedReview: ReportedReview) {
                val reviewRef = db.collection("companies")
                    .document(reportedReview.companyId)
                    .collection("reviews")
                    .document(reportedReview.reviewId)

                reviewRef.update("reportCount", 0)
                    .addOnSuccessListener {
                        db.collection("reported_reviews").document(reportedReview.id!!)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(itemView.context, "리뷰가 승인되었습니다.", Toast.LENGTH_SHORT).show()
                                loadReportedReviews() // 업데이트된 목록 로드
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(itemView.context, "승인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }

            private fun deleteReview(reportedReview: ReportedReview) {
                val reviewRef = db.collection("companies")
                    .document(reportedReview.companyId)
                    .collection("reviews")
                    .document(reportedReview.reviewId)

                reviewRef.delete()
                    .addOnSuccessListener {
                        db.collection("users").document(reportedReview.userId)
                            .update("reviews", reportedReview.userReviewCount - 1)
                            .addOnSuccessListener {
                                db.collection("reported_reviews").document(reportedReview.id!!)
                                    .delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(itemView.context, "리뷰가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                        loadReportedReviews() // 업데이트된 목록 로드
                                    }
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(itemView.context, "삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}

data class ReportedReview(
    var id: String? = null,
    val userId: String = "",
    val userName: String = "",
    val companyId: String = "",
    val companyName: String = "",
    val content: String = "",
    val imageUrl: String? = null,
    val profileImageUrl: String? = null,
    val reportCount: Int = 0,
    val reviewId: String = "",
    val userReviewCount: Long = 0 // 유저가 작성한 리뷰 수
)
