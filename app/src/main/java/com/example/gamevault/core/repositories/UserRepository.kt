package com.example.gamevault.core.repositories

import android.net.Uri
import com.example.gamevault.core.ResponseService
import com.example.gamevault.onboarding.personal.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRepository : UserService {
    private val firestore = FirebaseFirestore.getInstance()
    private val userCollection = firestore.collection("users")
    private val storage = FirebaseStorage.getInstance()

    override suspend fun saveUserInfo(userProfile: UserProfile): ResponseService<Unit> =
        withContext(Dispatchers.IO) {
            try {
                userCollection.document(userProfile.id).set(userProfile).await()
                ResponseService.Success(Unit)
            } catch (e: Exception) {
                ResponseService.Error("No se pudo guardar el perfil: ${e.localizedMessage}")
            }
        }

    override suspend fun getUserInfo(uid: String): ResponseService<UserProfile> =
        withContext(Dispatchers.IO) {
            try {
                val doc = userCollection.document(uid).get().await()
                val profile = doc.toObject(UserProfile::class.java)
                if (profile != null) ResponseService.Success(profile)
                else ResponseService.Error("No se encontró el perfil")
            } catch (e: Exception) {
                ResponseService.Error("No se pudo cargar el perfil")
            }
        }

    override suspend fun updateUserAvatar(uid: String, avatarUrl: String): ResponseService<Unit> =
        withContext(Dispatchers.IO) {
            try {
                userCollection.document(uid)
                    .update("fotoUrl", avatarUrl)
                    .await()
                ResponseService.Success(Unit)
            } catch (e: Exception) {
                ResponseService.Error("No se pudo actualizar la foto: ${e.localizedMessage}")
            }
        }

    /**
     * Sube la imagen a Firebase Storage en avatars/{uid}.jpg y devuelve la URL de descarga.
     * Se usa la referencia del bucket por defecto del proyecto (google-services.json).
     */
    suspend fun uploadAvatarImage(uid: String, imageUri: Uri): ResponseService<String> =
        withContext(Dispatchers.IO) {
            try {
                val ref = storage.reference.child("avatars/$uid.jpg")
                val metadata = StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .build()
                // Esperar a que TERMINE la subida antes de pedir la URL de descarga
                ref.putFile(imageUri, metadata).await()
                val url = ref.downloadUrl.await().toString()
                ResponseService.Success(url)
            } catch (e: Exception) {
                ResponseService.Error("No se pudo subir la imagen: ${e.localizedMessage ?: "error desconocido"}")
            }
        }
}
