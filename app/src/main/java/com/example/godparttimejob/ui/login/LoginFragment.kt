package com.example.godparttimejob.ui.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.godparttimejob.R
import com.google.firebase.auth.FirebaseAuth

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {

    private lateinit var auth : FirebaseAuth
    private lateinit var emailEditText : EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var joinButton: Button
    private lateinit var googleLoginButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        //Firebase Auth 초기화
        auth = FirebaseAuth.getInstance()

        //View 초기화
        emailEditText = view.findViewById(R.id.editEmail)
        passwordEditText = view.findViewById(R.id.editPassword)
        loginButton = view.findViewById(R.id.buttonLogin)
        joinButton = view.findViewById(R.id.buttonJoin)
        googleLoginButton = view.findViewById(R.id.buttonGoogleLogin)

        //회원가입 버튼 클릭 리스너
        joinButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if(email.isNotEmpty() && password.isNotEmpty()) {
                createUser(email, password)
            } else {
                Toast.makeText(context, "회원가입을 위해 이메일과 비밀번호를 입력해주세요!", Toast.LENGTH_LONG).show()
            }
        }

        //로그인 버튼 클릭 리스너
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if(email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(context, "이메일과 비밀번호를 입력해주세요!", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(context, "로그인 성공", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(
                            context,
                            "이메일 인증이 필요합니다. 이메일을 확인해주세요.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(context, "로그인 실패!", Toast.LENGTH_LONG).show()
                }
            }
    }



    private fun createUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        it.sendEmailVerification() // 인증 이메일 발송
                            .addOnCompleteListener { emailTask ->
                                if (emailTask.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        "회원가입 성공! 이메일을 확인해주세요.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "이메일 인증 메일 발송 실패!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    }
                } else {
                    Toast.makeText(context, "회원가입 실패!", Toast.LENGTH_LONG).show()
                }
            }
    }

}