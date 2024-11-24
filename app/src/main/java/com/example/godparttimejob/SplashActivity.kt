package com.example.godparttimejob

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 로그인 상태 확인 (SharedPreferences 사용)
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            // 메인 화면으로 이동
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // 로그인 화면이 MainActivity에 포함됨
            startActivity(Intent(this, MainActivity::class.java))
        }

        // SplashActivity 종료
        finish()
    }
}
