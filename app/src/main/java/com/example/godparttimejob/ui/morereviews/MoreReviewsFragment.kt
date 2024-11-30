package com.example.godparttimejob.ui.moreReviews

import ReviewAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.godparttimejob.R
import com.example.godparttimejob.ui.companydetails.Review
import com.example.godparttimejob.ui.companydetails.ReviewAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MoreReviewsFragment : Fragment() {

    private lateinit var recyclerMoreReviews: RecyclerView
    private lateinit var db: FirebaseFirestore
    private var companyId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_more_reviews, container, false)

        recyclerMoreReviews = view.findViewById(R.id.recyclerMoreReviews)
        recyclerMoreReviews.layoutManager = LinearLayoutManager(requireContext())
        db = FirebaseFirestore.getInstance()

        companyId = arguments?.getString("companyId")
        companyId?.let { loadAllReviews(it) }

        return view
    }

    private fun loadAllReviews(companyId: String) {
        db.collection("companies").document(companyId).collection("reviews")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val reviews = querySnapshot.toObjects(Review::class.java)
                recyclerMoreReviews.adapter = ReviewAdapter(reviews, db, companyId)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "리뷰를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }
}
