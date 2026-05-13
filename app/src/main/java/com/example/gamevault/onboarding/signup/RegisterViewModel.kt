package com.example.gamevault.onboarding.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamevault.core.AuthRepository
import com.example.gamevault.core.ResponseService
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _registerState = MutableStateFlow<ResponseService<FirebaseUser>?>(null)
    val registerState: StateFlow<ResponseService<FirebaseUser>?> = _registerState.asStateFlow()

    fun validateEmail(email: String): String? {
        if (email.isBlank()) return "El correo es requerido"
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Correo inválido"
        return null
    }

    fun validatePassword(password: String): String? {
        if (password.isBlank()) return "La contraseña es requerida"
        if (password.length < 6) return "Mínimo 6 caracteres"
        return null
    }

    fun validateConfirmPassword(password: String, confirm: String): String? {
        if (confirm.isBlank()) return "Confirma tu contraseña"
        if (password != confirm) return "Las contraseñas no coinciden"
        return null
    }

    fun isRegisterFormValid(email: String, password: String, confirm: String) =
        validateEmail(email) == null &&
                validatePassword(password) == null &&
                validateConfirmPassword(password, confirm) == null

    fun requestSignUp(email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = ResponseService.Loading
            _registerState.value = authRepository.requestSignUp(email, password)
        }
    }
}
