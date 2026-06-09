package com.example.gamevault.home.account

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.gamevault.core.FragmentCommunicator
import com.example.gamevault.core.ResponseService
import com.example.gamevault.databinding.FragmentAccountBinding
import com.example.gamevault.onboarding.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<AccountViewModel>()
    private lateinit var communicator: FragmentCommunicator

    // URI temporal donde la cámara guarda la foto
    private var cameraImageUri: Uri? = null

    // --- Launchers ---

    // Galería: devuelve el Uri de la imagen elegida
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadAvatar(it) }
    }

    // Cámara: indica si la captura fue exitosa (la imagen queda en cameraImageUri)
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) cameraImageUri?.let { viewModel.uploadAvatar(it) }
    }

    // Permiso de galería (READ_MEDIA_IMAGES / READ_EXTERNAL_STORAGE)
    private val galleryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openGallery()
        else Snackbar.make(binding.root, "Permiso de galería denegado", Snackbar.LENGTH_LONG).show()
    }

    // Permiso de cámara
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openCamera()
        else Snackbar.make(binding.root, "Permiso de cámara denegado", Snackbar.LENGTH_LONG).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        communicator = requireActivity() as FragmentCommunicator
        setupListeners()
        observeState()
        observeUpload()
        observeEdit()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.load() // refresca el conteo de favoritos al volver
    }

    private fun setupListeners() {
        binding.btnEditPhoto.setOnClickListener { showPhotoOptions() }

        binding.cardEditInfo.setOnClickListener { showEditProfileDialog() }

        binding.cardConfig.setOnClickListener {
            Snackbar.make(binding.root, "Próximamente", Snackbar.LENGTH_SHORT).show()
        }
        binding.cardLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Seguro que quieres salir?")
                .setPositiveButton("Salir") { _, _ -> logout() }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    // --- Selección de foto ---

    private fun showPhotoOptions() {
        val sheet = BottomSheetDialog(requireContext())
        val sheetBinding = com.example.gamevault.databinding.BottomsheetPhotoOptionsBinding.inflate(layoutInflater)
        sheetBinding.optionCamera.setOnClickListener {
            sheet.dismiss()
            requestCamera()
        }
        sheetBinding.optionGallery.setOnClickListener {
            sheet.dismiss()
            requestGallery()
        }
        sheet.setContentView(sheetBinding.root)
        sheet.show()
    }

    private fun requestGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(requireContext(), permission)
            == PackageManager.PERMISSION_GRANTED
        ) {
            openGallery()
        } else {
            galleryPermissionLauncher.launch(permission)
        }
    }

    private fun requestCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun openCamera() {
        val imageFile = File(requireContext().cacheDir, "images").apply { mkdirs() }
            .let { File(it, "avatar_${System.currentTimeMillis()}.jpg") }

        cameraImageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            imageFile
        )
        cameraImageUri?.let { cameraLauncher.launch(it) }
    }

    // --- Editar información personal ---

    private fun showEditProfileDialog() {
        val profile = viewModel.currentProfileOrNull()
        if (profile == null) {
            Snackbar.make(binding.root, "Cargando datos, intenta de nuevo", Snackbar.LENGTH_SHORT).show()
            return
        }

        val dialogBinding = com.example.gamevault.databinding.DialogEditProfileBinding.inflate(layoutInflater)
        dialogBinding.etEditNombre.setText(profile.nombre)
        dialogBinding.etEditApellido.setText(profile.primerApellido)
        dialogBinding.etEditTelefono.setText(profile.telefono)

        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = dialogBinding.etEditNombre.text.toString().trim()
                val apellido = dialogBinding.etEditApellido.text.toString().trim()
                val telefono = dialogBinding.etEditTelefono.text.toString().trim()

                when {
                    nombre.length < 2 ->
                        Snackbar.make(binding.root, "El nombre debe tener al menos 2 caracteres", Snackbar.LENGTH_LONG).show()
                    apellido.length < 2 ->
                        Snackbar.make(binding.root, "El apellido debe tener al menos 2 caracteres", Snackbar.LENGTH_LONG).show()
                    telefono.length !in 10..14 || !telefono.all { it.isDigit() } ->
                        Snackbar.make(binding.root, "Teléfono inválido (10 a 14 dígitos)", Snackbar.LENGTH_LONG).show()
                    else ->
                        viewModel.updateProfile(nombre, apellido, telefono)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        startActivity(
            Intent(requireContext(), MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        requireActivity().finish()
    }

    // --- Observadores ---

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.accountState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> communicator.manageLoader(true)
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)
                            bindData(state.data)
                        }
                        is ResponseService.Error -> {
                            communicator.manageLoader(false)
                            Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                        }
                        null -> Unit
                    }
                }
            }
        }
    }

    private fun observeUpload() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uploadState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> communicator.manageLoader(true)
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)
                            Snackbar.make(binding.root, "Foto actualizada", Snackbar.LENGTH_SHORT).show()
                            viewModel.clearUploadState()
                        }
                        is ResponseService.Error -> {
                            communicator.manageLoader(false)
                            Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                            viewModel.clearUploadState()
                        }
                        null -> Unit
                    }
                }
            }
        }
    }

    private fun observeEdit() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.editState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> communicator.manageLoader(true)
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)
                            Snackbar.make(binding.root, "Datos actualizados", Snackbar.LENGTH_SHORT).show()
                            viewModel.clearEditState()
                        }
                        is ResponseService.Error -> {
                            communicator.manageLoader(false)
                            Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                            viewModel.clearEditState()
                        }
                        null -> Unit
                    }
                }
            }
        }
    }

    private fun bindData(data: AccountData) {
        val p = data.profile
        val fullName = p.nombreCompleto()
        binding.tvUserName.text = fullName.ifBlank { p.nombreUsuario.ifBlank { "Gamer" } }
        binding.tvUserEmail.text = data.email
        binding.tvFullName.text = fullName.ifBlank { "—" }
        binding.tvPhone.text = p.telefono.ifBlank { "—" }
        binding.tvFavCount.text = "${data.favoritesCount} títulos guardados"
        binding.tvInitials.text = (p.nombre.firstOrNull()?.uppercase() ?: "G")

        // fotoUrl ahora puede ser una URL remota (Firebase Storage) o una ruta local antigua
        if (p.fotoUrl.isNotBlank()) {
            binding.imgAvatar.visibility = View.VISIBLE
            binding.tvInitials.visibility = View.GONE
            val source: Any =
                if (p.fotoUrl.startsWith("http")) p.fotoUrl else File(p.fotoUrl)
            Glide.with(this).load(source).centerCrop().into(binding.imgAvatar)
        } else {
            binding.imgAvatar.visibility = View.GONE
            binding.tvInitials.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
