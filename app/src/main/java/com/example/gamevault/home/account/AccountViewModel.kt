package com.example.gamevault.home.account

import android.net.Uri
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

    // Estado de subida de avatar: Loading mientras sube, Success(url) al terminar, Error si falla
    private val _uploadState = MutableStateFlow<ResponseService<String>?>(null)
    val uploadState: StateFlow<ResponseService<String>?> = _uploadState.asStateFlow()

    // Estado de edición de datos personales
    private val _editState = MutableStateFlow<ResponseService<Unit>?>(null)
    val editState: StateFlow<ResponseService<Unit>?> = _editState.asStateFlow()

    // Perfil actual en memoria (para editar sin perder los demás campos)
    private var currentProfile: UserProfile? = null

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
            currentProfile = profile
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

    /**
     * Sube la imagen seleccionada a Firebase Storage y guarda la URL en Firestore.
     * Emite Loading/Success/Error a través de uploadState.
     */
    fun uploadAvatar(imageUri: Uri) {
        viewModelScope.launch {
            _uploadState.value = ResponseService.Loading
            val uid = auth.currentUser?.uid
            if (uid == null) {
                _uploadState.value = ResponseService.Error("Sesión inválida")
                return@launch
            }

            // 1) Subir a Storage
            val uploadResult = userRepository.uploadAvatarImage(uid, imageUri)
            if (uploadResult is ResponseService.Error) {
                _uploadState.value = ResponseService.Error(uploadResult.error)
                return@launch
            }
            val url = (uploadResult as ResponseService.Success).data

            // 2) Guardar URL en Firestore (users/{uid}/fotoUrl)
            val updateResult = userRepository.updateUserAvatar(uid, url)
            if (updateResult is ResponseService.Error) {
                _uploadState.value = ResponseService.Error(updateResult.error)
                return@launch
            }

            _uploadState.value = ResponseService.Success(url)
            // Recargar datos para reflejar el nuevo avatar
            load()
        }
    }

    fun clearUploadState() {
        _uploadState.value = null
    }

    /**
     * Actualiza los datos personales editables (nombre, apellido, teléfono)
     * conservando el resto del perfil (usuario, fecha, fotoUrl).
     */
    fun updateProfile(
        nombre: String,
        primerApellido: String,
        telefono: String
    ) {
        viewModelScope.launch {
            _editState.value = ResponseService.Loading
            val base = currentProfile
            if (base == null) {
                _editState.value = ResponseService.Error("No se pudo cargar el perfil actual")
                return@launch
            }
            val updated = base.copy(
                nombre = nombre,
                primerApellido = primerApellido,
                telefono = telefono
            )
            val result = userRepository.saveUserInfo(updated)
            _editState.value = result
            if (result is ResponseService.Success) {
                currentProfile = updated
                load()
            }
        }
    }

    fun currentProfileOrNull(): UserProfile? = currentProfile

    fun clearEditState() {
        _editState.value = null
    }
}
