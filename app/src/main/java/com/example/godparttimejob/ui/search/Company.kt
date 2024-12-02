package com.example.godparttimejob.ui.search

data class Company(
    var id: String? = null,
    var name: String = "",
    var isRecruiting: Boolean = false,
    var averageRating: Float = 0.0f,
    var reviewCount: Int = 0,
    var iconImageUrl: String? = null
)
