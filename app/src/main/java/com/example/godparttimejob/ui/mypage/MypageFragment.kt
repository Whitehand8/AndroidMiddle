package com.example.godparttimejob.ui.mypage

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.godparttimejob.R
import com.example.godparttimejob.databinding.FragmentMypageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class MypageFragment : Fragment() {

    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var imageUri: Uri? = null

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                uploadProfileImage(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        val view = binding.root

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        loadUserInfo()

        binding.imageProfile.setOnClickListener {
            selectProfileImage()
        }

        binding.buttonUpdateLocation.setOnClickListener {
            showInputDialog("지역 정보", "location")
        }

        binding.buttonUpdateTele.setOnClickListener {
            showInputDialog("전화번호", "tele")
        }

        binding.buttonUpdateBirthday.setOnClickListener {
            showInputDialog("생일", "birthday")
        }

        binding.buttonDeleteAccount.setOnClickListener {
            confirmAccountDeletion()
        }

        return view
    }

    private fun loadUserInfo() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val email = document.getString("email") ?: "이메일 없음"
                    val userName = document.getString("userName") ?: userId // userName 없으면 UID 표시
                    val reviewsCount = document.getLong("reviews") ?: 0L
                    val likesCount = document.getLong("likes") ?: 0L

                    // 이메일 대신 사용자 이름 표시
                    binding.textEmail.text = userName
                    binding.textReviewsCount.text =
                        "작성한 리뷰: ${reviewsCount}개, 받은 좋아요: ${likesCount}개"
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "사용자 정보 로드 실패.", Toast.LENGTH_SHORT).show()
            }
    }



    private fun selectProfileImage() {
        imagePickerLauncher.launch("image/*")
    }

    private fun uploadProfileImage(uri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        val filename = UUID.randomUUID().toString()
        val ref = storage.reference.child("profile_images/$filename")

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { url ->
                    db.collection("users").document(userId)
                        .update("profileImageUrl", url.toString())
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "프로필 이미지 업데이트 완료!", Toast.LENGTH_SHORT).show()
                            loadUserInfo()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "프로필 이미지 저장 실패.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "이미지 업로드 실패.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showInputDialog(title: String, field: String) {
        val userId = auth.currentUser?.uid ?: return
        val editText = EditText(requireContext())
        editText.hint = "$title 입력"

        AlertDialog.Builder(requireContext())
            .setTitle("$title 변경")
            .setView(editText)
            .setPositiveButton("저장") { _, _ ->
                val newValue = editText.text.toString().trim()
                if (newValue.isNotEmpty()) {
                    db.collection("users").document(userId)
                        .update(field, newValue)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "$title 업데이트 완료!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "업데이트 실패.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "$title 을(를) 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun confirmAccountDeletion() {
        AlertDialog.Builder(requireContext())
            .setTitle("계정 삭제")
            .setMessage("정말 계정을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun deleteAccount() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .delete()
            .addOnSuccessListener {
                auth.currentUser?.delete()
                    ?.addOnSuccessListener {
                        Toast.makeText(requireContext(), "계정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        requireActivity().finish()
                    }
                    ?.addOnFailureListener {
                        Toast.makeText(requireContext(), "계정 삭제 실패.", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "데이터 삭제 실패.", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
