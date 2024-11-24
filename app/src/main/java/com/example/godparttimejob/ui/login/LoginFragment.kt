package com.example.godparttimejob.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.* // 버튼, 텍스트뷰, 체크박스 등 위젯 사용
import com.example.godparttimejob.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var joinButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        // Firebase Auth 및 Firestore 초기화
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // View 초기화
        emailEditText = view.findViewById(R.id.editEmail)
        passwordEditText = view.findViewById(R.id.editPassword)
        loginButton = view.findViewById(R.id.buttonLogin)
        joinButton = view.findViewById(R.id.buttonJoin)

        // 회원가입 버튼 클릭 리스너
        joinButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                if (password.length < 6) {
                    Toast.makeText(requireContext(), "비밀번호는 최소 6자 이상이어야 합니다.", Toast.LENGTH_LONG)
                        .show()
                } else {
                    createUser(email, password)
                }
            } else {
                Toast.makeText(requireContext(), "회원가입을 위해 이메일과 비밀번호를 입력해주세요!", Toast.LENGTH_LONG)
                    .show()
            }
        }

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

    private fun createAdmin(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val adminData = hashMapOf(
                            "email" to it.email,
                            "role" to "admin", // 운영자 role 설정
                            "createdAt" to System.currentTimeMillis()
                        )

                        db.collection("users").document(it.uid)
                            .set(adminData)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    requireContext(),
                                    "운영자 계정 생성 성공!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    requireContext(),
                                    "Firestore 저장 실패: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        // 이메일 인증 발송
                        it.sendEmailVerification()
                            .addOnCompleteListener { emailTask ->
                                if (!emailTask.isSuccessful) {
                                    Toast.makeText(
                                        requireContext(),
                                        "이메일 인증 메일 발송 실패!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    }
                } else {
                    Toast.makeText(requireContext(), "운영자 계정 생성 실패!", Toast.LENGTH_LONG).show()
                }
            }
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
                                    if (role == "admin") {
                                        Toast.makeText(
                                            requireContext(),
                                            "운영자로 로그인하였습니다.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        // 운영자용 페이지로 이동
                                        //navigateToAdminDashboard()
                                    } else {
                                        Toast.makeText(
                                            requireContext(),
                                            "일반 사용자로 로그인하였습니다.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        // 사용자 메인 페이지로 이동
                                        //navigateToUserDashboard()
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


    private fun createUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        // 기본적으로 일반 사용자로 설정 (role: "user")
                        val userData = hashMapOf(
                            "email" to it.email,
                            "role" to "user", // 기본 role 설정
                            "createdAt" to System.currentTimeMillis()
                        )

                        db.collection("users").document(it.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    requireContext(),
                                    "회원가입 성공! 이메일을 확인해주세요.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    requireContext(),
                                    "Firestore 저장 실패: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        // 이메일 인증 발송
                        it.sendEmailVerification()
                            .addOnCompleteListener { emailTask ->
                                if (!emailTask.isSuccessful) {
                                    Toast.makeText(
                                        requireContext(),
                                        "이메일 인증 메일 발송 실패!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    }
                } else {
                    Toast.makeText(requireContext(), "회원가입 실패!", Toast.LENGTH_LONG).show()
                }
            }
    }
}
