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
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.godparttimejob.MainActivity
import com.example.godparttimejob.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var adminLoginButton: Button // 운영자 로그인 버튼

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
        registerButton = view.findViewById(R.id.buttonJoin)
        adminLoginButton = view.findViewById(R.id.buttonAdminLogin)

        // 일반 로그인 버튼
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(requireContext(), "이메일과 비밀번호를 입력해주세요!", Toast.LENGTH_SHORT).show()
            }
        }

        // 회원가입 버튼
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                if (password.length < 6) {
                    Toast.makeText(requireContext(), "비밀번호는 최소 6자 이상이어야 합니다.", Toast.LENGTH_LONG).show()
                } else {
                    registerUser(email, password)
                }
            } else {
                Toast.makeText(requireContext(), "회원가입을 위해 이메일과 비밀번호를 입력해주세요!", Toast.LENGTH_LONG).show()
            }
        }

        // 운영자 로그인 버튼
        adminLoginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val adminCode = "1124" // 운영자 코드

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password, adminCode)
            } else {
                Toast.makeText(requireContext(), "운영자 이메일과 비밀번호를 입력해주세요!", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // 툴바 숨기기
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 툴바 다시 표시
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val userData = hashMapOf(
                            "email" to it.email,
                            "role" to "user", // 기본적으로 일반 사용자로 설정
                            "location" to null, // 초기화 시 null 값 설정
                            "tele" to null,     // 초기화 시 null 값 설정
                            "birthday" to null, // 초기화 시 null 값 설정
                            "reviews" to 0,     // 초기 리뷰 수는 0으로 초기화
                            "createdAt" to System.currentTimeMillis()
                        )

                        // Firestore에 사용자 정보 저장
                        db.collection("users").document(it.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                // 이메일 인증 메일 발송
                                user.sendEmailVerification() // 수정된 부분
                                    .addOnCompleteListener { emailTask ->
                                        if (emailTask.isSuccessful) {
                                            Toast.makeText(requireContext(), "회원가입 성공! 이메일 인증을 진행해주세요.", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(requireContext(), "이메일 인증 메일 발송 실패!", Toast.LENGTH_LONG).show()
                                        }
                                    }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Firestore 저장 실패: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                } else {
                    Toast.makeText(requireContext(), "회원가입 실패!", Toast.LENGTH_LONG).show()
                }
            }
    }


    private fun loginUser(email: String, password: String, adminCode: String? = null) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        db.collection("users").document(user.uid)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document != null && document.exists()) {
                                    val role = document.getString("role") ?: "user"
                                    if (adminCode != null && role == "admin") {
                                        Toast.makeText(requireContext(), "운영자로 로그인하였습니다.", Toast.LENGTH_LONG).show()
                                        navigateToAdminDashboard()
                                    } else if (adminCode == null) {
                                        Toast.makeText(requireContext(), "일반 사용자로 로그인하였습니다.", Toast.LENGTH_LONG).show()
                                        navigateToUserDashboard()
                                    } else {
                                        Toast.makeText(requireContext(), "운영자 코드가 잘못되었습니다.", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    Toast.makeText(requireContext(), "사용자 데이터를 찾을 수 없습니다.", Toast.LENGTH_LONG).show()
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Firestore 데이터 가져오기 실패: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(requireContext(), "이메일 인증이 필요합니다. 이메일을 확인해주세요.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "로그인 실패!", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun navigateToAdminDashboard() {
        val intent = Intent(requireContext(), MainActivity::class.java) // 운영자 전용 Activity 변경 가능
        startActivity(intent)
        requireActivity().finish()
    }

    private fun navigateToUserDashboard() {
        val intent = Intent(requireContext(), MainActivity::class.java) // 사용자용 Activity 변경 가능
        startActivity(intent)
        requireActivity().finish()
    }
}
