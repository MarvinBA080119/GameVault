package com.example.gamevault.onboarding.personal

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.gamevault.core.FragmentCommunicator
import com.example.gamevault.core.ResponseService
import com.example.gamevault.databinding.FragmentPersonalInfoBinding
import com.example.gamevault.home.HomeActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar

class PersonalInfoFragment : Fragment() {

    private var _binding: FragmentPersonalInfoBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<PersonalInfoViewModel>()
    private lateinit var communicator: FragmentCommunicator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
        communicator = requireActivity() as FragmentCommunicator
        setupValidation()
        setupDatePicker()
        setupClickListeners()
        observeState()
        return binding.root
    }

    private fun setupValidation() {
        binding.btnContinuar.isEnabled = false
        binding.etNombre.addTextChangedListener { validateAndEnable() }
        binding.etApellidos.addTextChangedListener { validateAndEnable() }
        binding.etCelular.addTextChangedListener { validateAndEnable() }
        binding.etFecha.addTextChangedListener { validateAndEnable() }
    }

    private fun validateAndEnable() {
        val nombre = binding.etNombre.text.toString().trim()
        val apellidos = binding.etApellidos.text.toString().trim()
        val celular = binding.etCelular.text.toString().trim()
        val fecha = binding.etFecha.text.toString().trim()

        binding.tilNombre.error = viewModel.validateNombre(nombre)
        binding.tilApellidos.error = viewModel.validateApellidos(apellidos)
        binding.tilCelular.error = viewModel.validateCelular(celular)
        binding.tilFecha.error = viewModel.validateFecha(fecha)

        binding.btnContinuar.isEnabled = viewModel.isFormValid(nombre, apellidos, celular, fecha)
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
        binding.btnContinuar.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                Snackbar.make(binding.root, "Sesión inválida", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            viewModel.saveProfile(
                uid = uid,
                nombre = binding.etNombre.text.toString().trim(),
                apellidos = binding.etApellidos.text.toString().trim(),
                celular = binding.etCelular.text.toString().trim(),
                fecha = binding.etFecha.text.toString().trim()
            )
        }
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
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
