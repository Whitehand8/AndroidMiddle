package com.example.godparttimejob

import com.example.godparttimejob.ui.login.LoginFragment
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // SharedPreferences에서 로그인 상태 확인
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            // 메인 화면으로 이동
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // 로그인 화면으로 이동
            startActivity(Intent(this, LoginFragment::class.java))
        }

        finish() // Splash 화면 종료
    }
}
