package com.example.godparttimejob

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.godparttimejob.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        setupBottomNavigationView(navController)

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

    private fun setupBottomNavigationView(navController: NavController) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")
                    if (role == "admin") {
                        // 운영자 메뉴로 변경
                        binding.navView.menu.clear()
                        binding.navView.inflateMenu(R.menu.admin_nav_menu)
                    } else {
                        // 일반 사용자 메뉴
                        binding.navView.menu.clear()
                        binding.navView.inflateMenu(R.menu.bottom_nav_menu)
                    }
                    binding.navView.setupWithNavController(navController)
                }
            }
            .addOnFailureListener {
                binding.navView.setupWithNavController(navController)
            }
    }
}
