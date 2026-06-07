package com.example.gamevault.onboarding.personal

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.gamevault.core.FragmentCommunicator
import com.example.gamevault.core.ResponseService
import com.example.gamevault.databinding.FragmentPersonalInfoBinding
import com.example.gamevault.home.HomeActivity
import com.example.gamevault.onboarding.MainActivity
import com.example.gamevault.onboarding.personal.model.UserProfile
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import androidx.navigation.fragment.findNavController

class PersonalInfoFragment : Fragment() {

    private var _binding: FragmentPersonalInfoBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<PersonalInfoViewModel>()
    private lateinit var communicator: FragmentCommunicator

    private var selectedImagePath: String? = null

    private val pickMedia = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImagePath = copyImageToInternal(uri)
            Glide.with(this).load(uri).centerCrop().into(binding.imgAvatar)
            binding.imgAvatar.setPadding(0, 0, 0, 0)
            binding.tvImageError.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
        communicator = requireActivity() as FragmentCommunicator
        setupDatePicker()
        setupClickListeners()
        observeState()
        return binding.root
    }

    private fun setupDatePicker() {
        val showPicker = {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    binding.etFecha.setText("%02d/%02d/%04d".format(day, month + 1, year))
                },
                cal.get(Calendar.YEAR) - 18,
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
        }
        binding.etFecha.setOnClickListener { showPicker() }
        binding.tilFecha.setEndIconOnClickListener { showPicker() }
    }

    private fun setupClickListeners() {
        binding.btnPickPhoto.setOnClickListener {
            pickMedia.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.btnContinuar.setOnClickListener { onContinue() }

        binding.btnCancelar.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Cancelar registro")
                .setMessage("¿Seguro que quieres cancelar? Tendrás que iniciar sesión de nuevo.")
                .setPositiveButton("Sí, cancelar") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    startActivity(
                        Intent(requireContext(), MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    )
                    requireActivity().finish()
                }
                .setNegativeButton("Volver", null)
                .show()
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }


    private fun onContinue() {
        val nombre = binding.etNombre.text.toString().trim()
        val segNombre = binding.etSegundoNombre.text.toString().trim()
        val primerApellido = binding.etPrimerApellido.text.toString().trim()
        val segApellido = binding.etSegundoApellido.text.toString().trim()
        val usuario = binding.etNombreUsuario.text.toString().trim()
        val telefono = binding.etTelefono.text.toString().trim()
        val fecha = binding.etFecha.text.toString().trim()

        val errNombre = viewModel.validateRequiredName(nombre)
        val errSegNombre = viewModel.validateOptionalName(segNombre)
        val errPrimerAp = viewModel.validateRequiredName(primerApellido)
        val errSegAp = viewModel.validateOptionalName(segApellido)
        val errUsuario = viewModel.validateUsername(usuario)
        val errTel = viewModel.validatePhone(telefono)
        val errFecha = viewModel.validateFecha(fecha)

        binding.tilNombre.error = errNombre
        binding.tilSegundoNombre.error = errSegNombre
        binding.tilPrimerApellido.error = errPrimerAp
        binding.tilSegundoApellido.error = errSegAp
        binding.tilNombreUsuario.error = errUsuario
        binding.tilTelefono.error = errTel
        binding.tilFecha.error = errFecha

        val imageMissing = selectedImagePath == null
        binding.tvImageError.visibility = if (imageMissing) View.VISIBLE else View.GONE

        val hasError = listOf(
            errNombre, errSegNombre, errPrimerAp, errSegAp, errUsuario, errTel, errFecha
        ).any { it != null } || imageMissing

        if (hasError) return

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Snackbar.make(binding.root, "Sesión inválida", Snackbar.LENGTH_LONG).show()
            return
        }

        viewModel.saveProfile(
            UserProfile(
                id = uid,
                nombre = nombre,
                segundoNombre = segNombre,
                primerApellido = primerApellido,
                segundoApellido = segApellido,
                nombreUsuario = usuario,
                telefono = telefono,
                fechaNacimiento = fecha,
                fotoUrl = selectedImagePath ?: ""
            )
        )
    }

    private fun copyImageToInternal(uri: Uri): String {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "user"
        val file = File(requireContext().filesDir, "avatar_$uid.jpg")
        requireContext().contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file.absolutePath
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> {
                            communicator.manageLoader(true)
                            binding.btnContinuar.isEnabled = false
                        }
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)
                            startActivity(
                                Intent(requireContext(), HomeActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                            )
                        }
                        is ResponseService.Error -> {
                            communicator.manageLoader(false)
                            binding.btnContinuar.isEnabled = true
                            Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                        }
                        null -> Unit
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}