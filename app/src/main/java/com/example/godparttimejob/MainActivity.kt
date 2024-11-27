package com.example.godparttimejob

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.godparttimejob.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_search, R.id.nav_favorite, R.id.nav_mypage)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.navView.setupWithNavController(navController)

        // Fragment에 따라 ActionBar와 BottomNavigationView 제어
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.loginFragment) {
                // 로그인 화면에서는 숨김
                supportActionBar?.hide()
                binding.navView.visibility = View.GONE
            } else {
                // 다른 화면에서는 표시
                supportActionBar?.show()
                binding.navView.visibility = View.VISIBLE
            }
        }

        // SplashActivity에서 전달된 "navigateToLogin" 처리
        if (intent.getBooleanExtra("navigateToLogin", false)) {
            navController.navigate(R.id.loginFragment)
        }
    }
}
