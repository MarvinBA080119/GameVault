package com.example.gamevault.home.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamevault.core.ResponseService
import com.example.gamevault.core.repositories.FavoritesRepository
import com.example.gamevault.core.repositories.UserRepository
import com.example.gamevault.onboarding.personal.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AccountData(
    val profile: UserProfile,
    val email: String,
    val favoritesCount: Int
)

class AccountViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val favoritesRepository = FavoritesRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _accountState = MutableStateFlow<ResponseService<AccountData>?>(null)
    val accountState: StateFlow<ResponseService<AccountData>?> = _accountState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _accountState.value = ResponseService.Loading
            val uid = auth.currentUser?.uid
            if (uid == null) {
                _accountState.value = ResponseService.Error("Sesión inválida")
                return@launch
            }
            val profileResult = userRepository.getUserInfo(uid)
            if (profileResult is ResponseService.Error) {
                _accountState.value = ResponseService.Error(profileResult.error)
                return@launch
            }
            val profile = (profileResult as ResponseService.Success).data
            val favResult = favoritesRepository.getFavorites()
            val count = if (favResult is ResponseService.Success) favResult.data.size else 0

            _accountState.value = ResponseService.Success(
                AccountData(
                    profile = profile,
                    email = auth.currentUser?.email ?: "",
                    favoritesCount = count
                )
            )
        }
    }
}