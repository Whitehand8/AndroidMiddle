package com.example.godparttimejob.ui.morehistory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.godparttimejob.R
import com.example.godparttimejob.ui.companydetails.JobHistory
import com.example.godparttimejob.ui.companydetails.JobHistoryAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MoreHistoryFragment : Fragment() {

    private lateinit var recyclerMoreHistory: RecyclerView
    private lateinit var db: FirebaseFirestore
    private var companyId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_more_history, container, false)

        recyclerMoreHistory = view.findViewById(R.id.recyclerMoreHistory)
        recyclerMoreHistory.layoutManager = LinearLayoutManager(requireContext())
        db = FirebaseFirestore.getInstance()

        companyId = arguments?.getString("companyId")
        companyId?.let { loadAllHistory(it) }

        return view
    }

    private fun loadAllHistory(companyId: String) {
        db.collection("companies").document(companyId).collection("history")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val historyList = querySnapshot.toObjects(JobHistory::class.java)
                recyclerMoreHistory.adapter = JobHistoryAdapter(historyList)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "공고 내역을 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }
}
