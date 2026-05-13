package com.example.gamevault.core.repositories

import com.example.gamevault.core.ResponseService
import com.example.gamevault.onboarding.personal.model.UserProfile

interface UserService {
    suspend fun saveUserInfo(userProfile: UserProfile): ResponseService<Unit>
}
