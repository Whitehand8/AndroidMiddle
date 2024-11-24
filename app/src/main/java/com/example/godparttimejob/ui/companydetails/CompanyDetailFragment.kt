package com.example.godparttimejob.ui.companydetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.godparttimejob.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class CompanyDetailFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var imageLargeCompany: ImageView
    private lateinit var textCompanyName: TextView
    private lateinit var textCompanyDescription: TextView
    private lateinit var textRecruitingStatus: TextView
    private lateinit var recyclerReviews: RecyclerView
    private lateinit var editReviewComment: EditText
    private lateinit var buttonSubmitReview: Button

    private var companyId: String? = null // 전달받은 회사 ID

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_company_detail, container, false)

        db = FirebaseFirestore.getInstance()

        // View 초기화
        imageLargeCompany = view.findViewById(R.id.imageLargeCompany)
        textCompanyName = view.findViewById(R.id.textCompanyName)
        textCompanyDescription = view.findViewById(R.id.textCompanyDescription)
        textRecruitingStatus = view.findViewById(R.id.textRecruitingStatus)
        recyclerReviews = view.findViewById(R.id.recyclerReviews)
        editReviewComment = view.findViewById(R.id.editReviewComment)
        buttonSubmitReview = view.findViewById(R.id.buttonSubmitReview)

        recyclerReviews.layoutManager = LinearLayoutManager(requireContext())

        // 회사 데이터 가져오기
        companyId = arguments?.getString("companyId")
        companyId?.let { loadCompanyDetails(it) }

        // 리뷰 작성 버튼 클릭
        buttonSubmitReview.setOnClickListener { submitReview() }

        return view
    }

    private fun loadCompanyDetails(companyId: String) {
        db.collection("companies").document(companyId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name")
                    val description = document.getString("description")
                    val largeImageUrl = document.getString("largeImageUrl")
                    val isRecruiting = document.getBoolean("isRecruiting") ?: false

                    textCompanyName.text = name
                    textCompanyDescription.text = description
                    textRecruitingStatus.text = if (isRecruiting) "모집 중" else "모집 종료"

                    // 이미지 로드
                    // Glide.with(this).load(largeImageUrl).into(imageLargeCompany)

                    // 리뷰 리스트 가져오기
                    loadReviews(companyId)
                }
            }
    }

    private fun loadReviews(companyId: String) {
        db.collection("companies").document(companyId).collection("reviews")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val reviews = querySnapshot.toObjects(Review::class.java)
                recyclerReviews.adapter = ReviewAdapter(reviews)
            }
    }

    private fun submitReview() {
        val comment = editReviewComment.text.toString().trim()
        if (comment.isNotEmpty()) {
            val review = hashMapOf(
                "userId" to "USER_ID", // 실제 사용자 ID로 대체
                "userName" to "사용자 이름", // 실제 사용자 이름으로 대체
                "rating" to 5, // 별점은 고정, 추가 구현 가능
                "comment" to comment,
                "createdAt" to System.currentTimeMillis()
            )

            companyId?.let { id ->
                db.collection("companies").document(id).collection("reviews")
                    .add(review)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "리뷰가 등록되었습니다!", Toast.LENGTH_SHORT).show()
                        editReviewComment.text.clear()
                        loadReviews(id)
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "리뷰 등록 실패!", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(requireContext(), "리뷰 내용을 입력하세요!", Toast.LENGTH_SHORT).show()
        }
    }
}
