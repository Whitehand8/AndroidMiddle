package com.example.godparttimejob.ui.login

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        emailEditText = view.findViewById(R.id.editEmail)
        passwordEditText = view.findViewById(R.id.editPassword)
        loginButton = view.findViewById(R.id.buttonLogin)
        registerButton = view.findViewById(R.id.buttonJoin)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(requireContext(), "이메일과 비밀번호를 입력해주세요!", Toast.LENGTH_SHORT).show()
            }
        }

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

        return view
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
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
                            "role" to "user",
                            "createdAt" to System.currentTimeMillis()
                        )

                        db.collection("users").document(it.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "회원가입 성공!", Toast.LENGTH_LONG).show()
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

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    db.collection("admins")
                        .whereEqualTo("email", email)
                        .whereEqualTo("password", password)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                Toast.makeText(requireContext(), "운영자로 로그인하였습니다.", Toast.LENGTH_LONG).show()
                                navigateToAdminDashboard()
                            } else {
                                Toast.makeText(requireContext(), "사용자로 로그인하였습니다.", Toast.LENGTH_LONG).show()
                                navigateToUserDashboard()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "데이터베이스 확인 중 오류 발생!", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "로그인 실패!", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun navigateToAdminDashboard() {
        val intent = Intent(requireContext(), MainActivity::class.java) // 운영자 전용 화면으로 변경 가능
        startActivity(intent)
        requireActivity().finish()
    }

    private fun navigateToUserDashboard() {
        val intent = Intent(requireContext(), MainActivity::class.java) // 사용자 전용 화면으로 변경 가능
        startActivity(intent)
        requireActivity().finish()
    }
}
