package com.example.arcadia.core.repositories

import com.example.arcadia.core.ResponseService
import com.example.arcadia.onboarding.personal.model.UserProfile

interface UserService {
    suspend fun saveUserInfo(userProfile: UserProfile): ResponseService<Unit>
}
