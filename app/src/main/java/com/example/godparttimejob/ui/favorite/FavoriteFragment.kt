package com.example.godparttimejob.ui.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.godparttimejob.R
import com.example.godparttimejob.ui.search.Company
import com.example.godparttimejob.ui.search.SearchAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoriteFragment : Fragment() {

    private lateinit var recyclerFavorite: RecyclerView
    private val companyList = mutableListOf<Company>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)

        recyclerFavorite = view.findViewById(R.id.recyclerFavorite)
        recyclerFavorite.layoutManager = LinearLayoutManager(requireContext())

        loadFavorites()

        return view
    }

    private fun loadFavorites() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .collection("favorites")
                .get()
                .addOnSuccessListener { snapshot ->
                    companyList.clear()
                    for (document in snapshot) {
                        val company = document.toObject(Company::class.java)
                        company.id = document.id
                        companyList.add(company)
                    }
                    recyclerFavorite.adapter = SearchAdapter(companyList)
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "즐겨찾기를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
