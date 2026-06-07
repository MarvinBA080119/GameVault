package com.example.gamevault.onboarding.personal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamevault.core.ResponseService
import com.example.gamevault.core.repositories.UserRepository
import com.example.gamevault.onboarding.personal.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PersonalInfoViewModel : ViewModel() {
    private val repository = UserRepository()

    private val _saveState = MutableStateFlow<ResponseService<Unit>?>(null)
    val saveState: StateFlow<ResponseService<Unit>?> = _saveState.asStateFlow()

    // Campos obligatorios (nombre, primer apellido)
    fun validateRequiredName(v: String): String? {
        if (v.isBlank()) return "Este campo es obligatorio"
        if (v.length < 2) return "Mínimo 2 caracteres"
        if (!v.all { it.isLetter() || it.isWhitespace() }) return "Solo se permiten letras"
        return null
    }

    // Campos opcionales (segundo nombre, segundo apellido)
    fun validateOptionalName(v: String): String? {
        if (v.isBlank()) return null
        if (!v.all { it.isLetter() || it.isWhitespace() }) return "Solo se permiten letras"
        return null
    }

    fun validateUsername(v: String): String? {
        if (v.isBlank()) return "El usuario es obligatorio"
        if (v.length < 4) return "Mínimo 4 caracteres"
        if (!v.matches(Regex("^[a-zA-Z0-9_.]+$"))) return "Solo letras, números, _ y ."
        return null
    }

    fun validatePhone(v: String): String? {
        if (v.isBlank()) return "El teléfono es obligatorio"
        if (!v.all { it.isDigit() }) return "Solo números"
        if (v.length !in 10..14) return "Entre 10 y 14 dígitos"
        return null
    }

    fun validateFecha(v: String): String? =
        if (v.isBlank()) "Selecciona tu fecha de nacimiento" else null

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            _saveState.value = ResponseService.Loading
            _saveState.value = repository.saveUserInfo(profile)
        }
    }
}