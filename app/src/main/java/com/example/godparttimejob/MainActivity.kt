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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = findNavController(R.id.nav_host_fragment_activity_main)

        setupUserRoleBasedNavigation()

        // ActionBar 및 BottomNavigationView 제어
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.loginFragment) {
                supportActionBar?.hide()
                binding.navView.visibility = View.GONE
            } else {
                supportActionBar?.show()
                binding.navView.visibility = View.VISIBLE
            }
        }

        if (intent.getBooleanExtra("navigateToLogin", false)) {
            navController.navigate(R.id.loginFragment)
        }
    }

    private fun setupUserRoleBasedNavigation() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val role = document.getString("role") ?: "user"
                if (role == "admin") {
                    setupAdminNavigation()
                } else {
                    setupUserNavigation()
                }
            }
            .addOnFailureListener {
                // 실패 시 기본 사용자 메뉴 설정
                setupUserNavigation()
            }
    }

    private fun setupNavigation(menuRes: Int, appBarDestinations: Set<Int>) {
        val appBarConfiguration = AppBarConfiguration(appBarDestinations)
        binding.navView.menu.clear()  // 기존 메뉴 삭제
        binding.navView.inflateMenu(menuRes) // 새로운 메뉴 로드
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController) // 네비게이션 컨트롤러 연결
    }

    private fun setupUserNavigation() {
        setupNavigation(
            menuRes = R.menu.bottom_nav_menu,
            appBarDestinations = setOf(R.id.nav_home, R.id.nav_search, R.id.nav_favorite, R.id.nav_mypage)
        )
    }

    private fun setupAdminNavigation() {
        setupNavigation(
            menuRes = R.menu.admin_nav_menu,
            appBarDestinations = setOf(R.id.nav_home, R.id.nav_search, R.id.nav_admin_reported, R.id.nav_admin_company)
        )
    }
}
