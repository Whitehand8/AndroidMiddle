package com.example.godparttimejob.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    db.collection("admins")
                        .whereEqualTo("email", email)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                navigateToAdminDashboard()
                            } else {
                                navigateToUserDashboard()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "운영자 확인 중 오류 발생!", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "로그인 실패: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
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

    private fun navigateToAdminDashboard() {
        findNavController().navigate(R.id.nav_admin)
    }

    private fun navigateToUserDashboard() {
        findNavController().navigate(R.id.nav_home)
    }
}
