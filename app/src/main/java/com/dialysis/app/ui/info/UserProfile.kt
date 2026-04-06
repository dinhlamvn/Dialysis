package com.dialysis.app.ui.info

data class UserProfile(
    val gender: Int,
    val weight: Int,
    val age: Int,
    val name: String,
    val phone: String,
    val dialysisStartYear: Int,
    val dialysisFreqWeek: Int,
    val dailyUrineMl: Int,
)

fun InfoState.toUserProfile(): UserProfile {
    return UserProfile(
        gender = gender,
        weight = weight,
        age = age,
        name = name,
        phone = phone,
        dialysisStartYear = dialysisStartYear,
        dialysisFreqWeek = dialysisFreqWeek,
        dailyUrineMl = dailyUrineMl
    )
}
