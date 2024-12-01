package com.example.godparttimejob.ui.mypage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.godparttimejob.databinding.FragmentMypageBinding
import com.example.godparttimejob.ui.settings.SettingsActivity
import com.example.godparttimejob.ui.reviews.UserReviewsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MypageFragment : Fragment() {

    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        val view = binding.root

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadUserInfo()

        binding.buttonSettings.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }

        binding.buttonUpdateLocation.setOnClickListener {
            val intent = Intent(requireContext(), UserReviewsActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun loadUserInfo() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val reviewsCount = document.getLong("reviews") ?: 0L
                        val likesCount = document.getLong("likes") ?: 0L
                        binding.textReviewsCount.text = "작성한 리뷰: $reviewsCount개, 받은 좋아요: $likesCount개"
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "사용자 정보 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
