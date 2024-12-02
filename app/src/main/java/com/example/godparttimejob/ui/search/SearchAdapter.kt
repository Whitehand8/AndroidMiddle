package com.example.godparttimejob.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.godparttimejob.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SearchAdapter(private val companyList: List<Company>) :
    RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val company = companyList[position]
        holder.bind(company)
    }

    override fun getItemCount() = companyList.size

    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageCompanyIcon: ImageView = itemView.findViewById(R.id.imageCompanyIcon)
        private val textCompanyName: TextView = itemView.findViewById(R.id.textCompanyName)
        private val textCompanyStatus: TextView = itemView.findViewById(R.id.textCompanyStatus)
        private val textCompanyRating: TextView = itemView.findViewById(R.id.textCompanyRating)
        private val imageFavorite: ImageView = itemView.findViewById(R.id.imageFavorite)

        fun bind(company: Company) {
            textCompanyName.text = company.name
            textCompanyStatus.text = if (company.isRecruiting) "모집 중" else "모집 X"
            textCompanyRating.text = "별점 평균: ${company.averageRating} (${company.reviewCount})"

            // 회사 아이콘 설정
            if (company.iconImageUrl.isNullOrEmpty()) {
                imageCompanyIcon.setImageResource(R.drawable.ic_no_image)
            } else {
                Glide.with(itemView.context).load(company.iconImageUrl).into(imageCompanyIcon)
            }

            // Firebase에서 즐겨찾기 상태 확인
            userId?.let { uid ->
                db.collection("users").document(uid).collection("favorites")
                    .document(company.id ?: "").get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            imageFavorite.setImageResource(R.drawable.ic_star_selected)
                        } else {
                            imageFavorite.setImageResource(R.drawable.ic_star_outline)
                        }
                    }
            }

            // 회사 카드 클릭 시 상세 정보로 이동
            itemView.setOnClickListener {
                val bundle = Bundle().apply {
                    putString("companyId", company.id)
                }
                itemView.findNavController().navigate(R.id.action_nav_search_to_companyDetailFragment, bundle)
            }

            // 즐겨찾기 아이콘 클릭 이벤트
            imageFavorite.setOnClickListener {
                userId?.let { uid ->
                    val favoriteDocRef = db.collection("users").document(uid)
                        .collection("favorites").document(company.id ?: "")
                    favoriteDocRef.get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            // 이미 즐겨찾기인 경우 삭제
                            favoriteDocRef.delete().addOnSuccessListener {
                                imageFavorite.setImageResource(R.drawable.ic_star_outline)
                            }
                        } else {
                            // 즐겨찾기에 추가
                            val favoriteData = hashMapOf(
                                "companyId" to company.id,
                                "name" to company.name,
                                "iconImageUrl" to company.iconImageUrl
                            )
                            favoriteDocRef.set(favoriteData).addOnSuccessListener {
                                imageFavorite.setImageResource(R.drawable.ic_star_selected)
                            }
                        }
                    }
                }
            }
        }
    }
}
