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

        // Handle visibility of ActionBar and BottomNavigationView
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.loginFragment) {
                supportActionBar?.hide()
                binding.navView.visibility = View.GONE
            } else {
                supportActionBar?.show()
                binding.navView.visibility = View.VISIBLE
            }
        }

        // Navigate to login if required
        if (intent.getBooleanExtra("navigateToLogin", false)) {
            navController.navigate(R.id.loginFragment)
        }
    }

    private fun setupUserRoleBasedNavigation() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            setupUserNavigation()
            return
        }

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
                setupUserNavigation()
            }
    }

    private fun setupNavigation(menuRes: Int, appBarDestinations: Set<Int>) {
        val appBarConfiguration = AppBarConfiguration(appBarDestinations)
        binding.navView.menu.clear()
        binding.navView.inflateMenu(menuRes)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
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
