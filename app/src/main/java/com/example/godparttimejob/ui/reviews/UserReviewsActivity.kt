package com.example.godparttimejob.ui.reviews

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.godparttimejob.databinding.ActivityUserReviewsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserReviewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserReviewsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserReviewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.recyclerUserReviews.layoutManager = LinearLayoutManager(this)

        loadUserReviews()
    }

    private fun loadUserReviews() {
        val currentUser = auth.currentUser ?: return
        db.collection("reviews")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val reviews = querySnapshot.toObjects(UserReview::class.java)
                binding.recyclerUserReviews.adapter = UserReviewsAdapter(reviews, db)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "리뷰를 불러오는 데 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
