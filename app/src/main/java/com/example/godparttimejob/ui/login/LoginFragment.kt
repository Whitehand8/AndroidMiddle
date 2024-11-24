package com.example.godparttimejob.ui.login

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.godparttimejob.MainActivity
import com.example.godparttimejob.R
//import com.example.godparttimejob.ui.admin.AdminDashboardActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        // Firebase 초기화
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // View 초기화
        emailEditText = view.findViewById(R.id.editEmail)
        passwordEditText = view.findViewById(R.id.editPassword)
        loginButton = view.findViewById(R.id.buttonLogin)

        // 로그인 버튼 클릭 리스너
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(requireContext(), "이메일과 비밀번호를 입력해주세요!", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        // Firestore에서 사용자 역할 확인
                        db.collection("users").document(user.uid)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document != null && document.exists()) {
                                    val role = document.getString("role") ?: "user"

                                    // SharedPreferences에 로그인 상태 및 역할 저장
                                    val sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", MODE_PRIVATE)
                                    val editor = sharedPreferences.edit()
                                    editor.putBoolean("isLoggedIn", true) // 로그인 상태 저장
                                    editor.putString("userRole", role) // 사용자 역할 저장
                                    editor.apply()

                                    if (role == "admin") {
                                        Toast.makeText(
                                            requireContext(),
                                            "운영자로 로그인하였습니다.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        navigateToAdminDashboard()
                                    } else {
                                        Toast.makeText(
                                            requireContext(),
                                            "일반 사용자로 로그인하였습니다.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        navigateToUserDashboard()
                                    }
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "사용자 데이터를 찾을 수 없습니다.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    requireContext(),
                                    "Firestore 데이터 가져오기 실패: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "이메일 인증이 필요합니다. 이메일을 확인해주세요.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "로그인 실패!", Toast.LENGTH_LONG).show()
                }
            }
    }

    // 운영자용 대시보드 이동
    private fun navigateToAdminDashboard() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    // 사용자용 대시보드 이동
    private fun navigateToUserDashboard() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}
