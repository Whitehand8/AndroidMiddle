package com.example.godparttimejob.ui.mypage

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.godparttimejob.databinding.FragmentMypageBinding
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

        // 사용자 정보 가져오기
        loadUserInfo()

        // 지역 업데이트 버튼 리스너
        binding.buttonUpdateLocation.setOnClickListener {
            showUpdateDialog("지역", "location")
        }

        // 전화번호 업데이트 버튼 리스너
        binding.buttonUpdateTele.setOnClickListener {
            showUpdateDialog("전화번호", "tele")
        }

        // 생일 업데이트 버튼 리스너
        binding.buttonUpdateBirthday.setOnClickListener {
            showUpdateDialog("생일", "birthday")
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
                        val location = document.getString("location") ?: "지역 정보 없음"
                        val tele = document.getString("tele") ?: "전화번호 없음"
                        val birthday = document.getString("birthday") ?: "생일 정보 없음"

                        // 버튼 텍스트 업데이트
                        binding.buttonUpdateLocation.text = location
                        binding.buttonUpdateTele.text = tele
                        binding.buttonUpdateBirthday.text = birthday
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "사용자 정보 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showUpdateDialog(fieldName: String, fieldKey: String) {
        val context = requireContext() // requireContext를 변수에 저장하여 안정적으로 호출
        val editText = EditText(context).apply {
            hint = "$fieldName 입력"
        }

        AlertDialog.Builder(context)
            .setTitle("$fieldName 업데이트")
            .setView(editText)
            .setPositiveButton("확인") { _, _ ->
                val input = editText.text.toString().trim()
                if (input.isNotEmpty()) {
                    updateField(fieldKey, input)
                } else {
                    Toast.makeText(context, "$fieldName 을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .create()
            .show()
    }

    private fun updateField(fieldKey: String, value: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(currentUser.uid)
            .update(fieldKey, value)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "정보가 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
                loadUserInfo() // 업데이트된 정보 반영
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "업데이트 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
