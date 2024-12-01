package com.example.godparttimejob.ui.reviews

data class UserReview(
    val id: String = "",  // 리뷰의 고유 ID
    val userName: String = "",  // 작성자 이름
    val workIntensity: Int = 0,  // 근무 강도 점수
    val salary: Int = 0,  // 급여 점수
    val environment: Int = 0,  // 환경 점수
    val comment: String = "",  // 리뷰 내용
    val createdAt: Long = 0L,  // 작성 시간 (타임스탬프)
    val likes: Int = 0 // 좋아요 수
)
