package com.example.godparttimejob.ui.companydetails

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
import com.google.firebase.firestore.Query

class CompanyDetailFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var imageLargeCompany: ImageView
    private lateinit var textCompanyName: TextView
    private lateinit var textCompanyDescription: TextView
    private lateinit var textRecruitingStatus: TextView
    private lateinit var recyclerReviews: RecyclerView
    private lateinit var buttonWriteReview: Button

    private var companyId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_company_detail, container, false)

        db = FirebaseFirestore.getInstance()

        imageLargeCompany = view.findViewById(R.id.imageLargeCompany)
        textCompanyName = view.findViewById(R.id.textCompanyName)
        textCompanyDescription = view.findViewById(R.id.textCompanyDescription)
        textRecruitingStatus = view.findViewById(R.id.textRecruitingStatus)
        recyclerReviews = view.findViewById(R.id.recyclerReviews)
        buttonWriteReview = view.findViewById(R.id.buttonWriteReview)

        recyclerReviews.layoutManager = LinearLayoutManager(requireContext())

        companyId = arguments?.getString("companyId")
        companyId?.let { loadCompanyDetails(it) }

        buttonWriteReview.setOnClickListener { navigateToWriteReview() }

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

                    if (!largeImageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(largeImageUrl).into(imageLargeCompany)
                    }

                    loadReviews(companyId)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "회사 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
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
            .addOnFailureListener {
                Toast.makeText(requireContext(), "리뷰를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToWriteReview() {
        val bundle = Bundle().apply {
            putString("companyId", companyId)
        }
        view?.let { currentView ->
            androidx.navigation.Navigation.findNavController(currentView)
                .navigate(R.id.action_companyDetailFragment_to_writeReviewFragment, bundle)
        }
    }
}
