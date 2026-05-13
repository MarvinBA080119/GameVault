package com.example.arcadia.onboarding.personal.model

data class UserProfile(
    val id: String,
    val firstName: String,
    val lastName: String,
    val userName: String,
    val phone: String,
    val birthDate: String
)
