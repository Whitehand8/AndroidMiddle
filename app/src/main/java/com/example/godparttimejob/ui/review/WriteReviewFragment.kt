package com.example.godparttimejob.ui.review

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.godparttimejob.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class WriteReviewFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var ratingWorkIntensity: RatingBar
    private lateinit var ratingSalary: RatingBar
    private lateinit var ratingEnvironment: RatingBar
    private lateinit var editReviewComment: EditText
    private lateinit var buttonUploadImage1: Button
    private lateinit var buttonUploadImage2: Button
    private lateinit var buttonSubmitReview: Button

    private var imageUri1: Uri? = null
    private var imageUri2: Uri? = null
    private var imageUrl1: String? = null
    private var imageUrl2: String? = null

    private var companyId: String? = null // 전달받은 회사 ID

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_write_review, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // View 초기화
        ratingWorkIntensity = view.findViewById(R.id.ratingWorkIntensity)
        ratingSalary = view.findViewById(R.id.ratingSalary)
        ratingEnvironment = view.findViewById(R.id.ratingEnvironment)
        editReviewComment = view.findViewById(R.id.editReviewComment)
        buttonUploadImage1 = view.findViewById(R.id.buttonUploadImage1)
        buttonUploadImage2 = view.findViewById(R.id.buttonUploadImage2)
        buttonSubmitReview = view.findViewById(R.id.buttonSubmitReview)

        companyId = arguments?.getString("companyId")

        // 이미지 업로드 버튼
        buttonUploadImage1.setOnClickListener { selectImage(true) }
        buttonUploadImage2.setOnClickListener { selectImage(false) }

        // 리뷰 등록 버튼
        buttonSubmitReview.setOnClickListener { submitReview() }

        return view
    }

    private fun selectImage(isFirstImage: Boolean) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        val launcher = if (isFirstImage) imagePickerLauncher1 else imagePickerLauncher2
        launcher.launch(intent)
    }

    private val imagePickerLauncher1 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri1 = result.data?.data
        }
    }

    private val imagePickerLauncher2 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri2 = result.data?.data
        }
    }

    private fun uploadImageToStorage(uri: Uri?, onComplete: (String?) -> Unit) {
        if (uri == null) {
            onComplete(null)
            return
        }

        val filename = UUID.randomUUID().toString() + ".jpg"
        val ref = storage.reference.child("review_images/$filename")

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { url ->
                    onComplete(url.toString())
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    private fun submitReview() {
        val userId = auth.currentUser?.uid ?: return // 로그인된 사용자 ID 가져오기
        val workIntensity = ratingWorkIntensity.rating.toInt()
        val salary = ratingSalary.rating.toInt()
        val environment = ratingEnvironment.rating.toInt()
        val comment = editReviewComment.text.toString().trim()

        if (comment.isEmpty() || workIntensity == 0 || salary == 0 || environment == 0) {
            Toast.makeText(requireContext(), "모든 정보를 입력하세요!", Toast.LENGTH_SHORT).show()
            return
        }

        val review = hashMapOf(
            "userId" to userId,
            "userName" to "사용자 이름", // 실제 사용자 이름으로 대체
            "workIntensity" to workIntensity,
            "salary" to salary,
            "environment" to environment,
            "averageRating" to (workIntensity + salary + environment) / 3.0,
            "comment" to comment,
            "createdAt" to System.currentTimeMillis()
        )

        uploadImageToStorage(imageUri1) { url1 ->
            imageUrl1 = url1
            uploadImageToStorage(imageUri2) { url2 ->
                imageUrl2 = url2

                if (url1 != null) review["image1"] = url1
                if (url2 != null) review["image2"] = url2

                companyId?.let { id ->
                    db.collection("companies").document(id).collection("reviews")
                        .add(review)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "리뷰가 등록되었습니다!", Toast.LENGTH_SHORT).show()
                            updateReviewCount(userId) // 리뷰 수 업데이트
                            requireActivity().onBackPressed()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "리뷰 등록 실패!", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }

    private fun updateReviewCount(userId: String) {
        val userRef = db.collection("users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val currentReviews = document.getLong("reviews") ?: 0L
                userRef.update("reviews", currentReviews + 1)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "리뷰 수가 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "리뷰 수 업데이트 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "사용자 데이터를 가져오는 데 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
