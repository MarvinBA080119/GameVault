package com.example.gamevault.onboarding.signin

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamevault.core.AuthRepository
import com.example.gamevault.core.ResponseService
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignInViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _signInState = MutableStateFlow<ResponseService<FirebaseUser>?>(null)
    val signInState: StateFlow<ResponseService<FirebaseUser>?> = _signInState.asStateFlow()

    fun validateEmail(email: String): String? {
        if (email.isBlank()) return "El correo es requerido"
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Correo inválido"
        return null
    }

    fun validatePassword(password: String): String? {
        if (password.isBlank()) return "La contraseña es requerida"
        if (password.length < 6) return "Mínimo 6 caracteres"
        return null
    }

    fun isLoginFormValid(email: String, password: String) =
        validateEmail(email) == null && validatePassword(password) == null

    fun requestLogin(email: String, password: String) {
        viewModelScope.launch {
            _signInState.value = ResponseService.Loading
            _signInState.value = repository.requestLogin(email, password)
        }
    }
}
