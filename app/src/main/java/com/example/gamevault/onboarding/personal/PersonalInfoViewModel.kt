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

    fun validateNombre(v: String): String? {
        if (v.isBlank()) return "El nombre es requerido"
        if (v.length < 2) return "Mínimo 2 caracteres"
        if (!v.all { it.isLetter() || it.isWhitespace() }) return "Solo se permiten letras"
        return null
    }

    fun validateApellidos(v: String): String? {
        if (v.isBlank()) return "Los apellidos son requeridos"
        if (v.length < 2) return "Mínimo 2 caracteres"
        if (!v.all { it.isLetter() || it.isWhitespace() }) return "Solo se permiten letras"
        return null
    }

    fun validateCelular(v: String): String? {
        if (v.isBlank()) return "El celular es requerido"
        if (!v.all { it.isDigit() }) return "Solo números"
        if (v.length !in 10..14) return "Entre 10 y 14 dígitos"
        return null
    }

    fun validateFecha(v: String): String? {
        if (v.isBlank()) return "Selecciona tu fecha de nacimiento"
        return null
    }

    fun isFormValid(nombre: String, apellidos: String, celular: String, fecha: String) =
        validateNombre(nombre) == null &&
                validateApellidos(apellidos) == null &&
                validateCelular(celular) == null &&
                validateFecha(fecha) == null

    fun saveProfile(uid: String, nombre: String, apellidos: String, celular: String, fecha: String) {
        viewModelScope.launch {
            _saveState.value = ResponseService.Loading
            _saveState.value = repository.saveUserInfo(
                UserProfile(
                    id = uid,
                    nombre = nombre,
                    apellidos = apellidos,
                    celular = celular,
                    fechaNacimiento = fecha
                )
            )
        }
    }
}
