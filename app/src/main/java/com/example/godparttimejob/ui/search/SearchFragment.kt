package com.example.godparttimejob.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.godparttimejob.R
import com.google.firebase.firestore.FirebaseFirestore

class SearchFragment : Fragment() {

    private lateinit var recyclerSearchResults: RecyclerView
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var editSearch: EditText

    private val db = FirebaseFirestore.getInstance()
    private val companyList = mutableListOf<Company>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        recyclerSearchResults = view.findViewById(R.id.recyclerSearchResults)
        editSearch = view.findViewById(R.id.editSearch)

        searchAdapter = SearchAdapter(companyList)
        recyclerSearchResults.layoutManager = LinearLayoutManager(requireContext())
        recyclerSearchResults.adapter = searchAdapter

        // 검색 텍스트 입력 시 데이터 필터링
        editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 필요 시 구현
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                fetchSearchResults(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // 필요 시 구현
            }
        })

        return view
    }

    private fun fetchSearchResults(query: String) {
        db.collection("companies")
            .orderBy("name")
            .startAt(query)
            .endAt(query + "\uf8ff") // Firestore의 문자열 검색
            .get()
            .addOnSuccessListener { snapshot ->
                companyList.clear()
                for (document in snapshot) {
                    val company = document.toObject(Company::class.java)
                    company.id = document.id
                    companyList.add(company)
                }
                searchAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "검색 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
