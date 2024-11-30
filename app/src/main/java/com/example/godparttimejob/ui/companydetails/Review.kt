package com.example.godparttimejob.ui.companydetails

data class Review(
    val id: String? = null, // Firestore에서 가져올 문서 ID
    val userName: String = "",
    val workIntensity: Int = 0,
    val salary: Int = 0,
    val environment: Int = 0,
    val comment: String = "",
    val createdAt: Long = 0L
)
