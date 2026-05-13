package com.example.arcadia.onboarding.personal

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
import com.example.arcadia.core.FragmentCommunicator
import com.example.arcadia.core.ResponseService
import com.example.arcadia.databinding.FragmentPersonalInfoBinding
import com.example.arcadia.home.HomeActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar

class PersonalInfoFragment : Fragment() {
    private var _binding: FragmentPersonalInfoBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<PersonalInfoViewModel>()
    private lateinit var communicator: FragmentCommunicator

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
        communicator = requireActivity() as FragmentCommunicator
        setupValidation()
        setupDatePicker()
        setupClickListeners()
        observeState()
        return binding.root
    }

    private fun setupValidation() {
        binding.saveButton.isEnabled = false
        binding.firstNameTiet.addTextChangedListener { validateAndEnable() }
        binding.lastNameTiet.addTextChangedListener { validateAndEnable() }
        binding.usernameTiet.addTextChangedListener { validateAndEnable() }
        binding.phoneTiet.addTextChangedListener { validateAndEnable() }
        binding.birthDateTiet.addTextChangedListener { validateAndEnable() }
    }

    private fun validateAndEnable() {
        val firstName = binding.firstNameTiet.text.toString().trim()
        val lastName = binding.lastNameTiet.text.toString().trim()
        val username = binding.usernameTiet.text.toString().trim()
        val phone = binding.phoneTiet.text.toString().trim()
        val birthDate = binding.birthDateTiet.text.toString().trim()
        binding.firstNameTil.error = viewModel.validateFirstName(firstName)
        binding.lastNameTil.error = viewModel.validateLastName(lastName)
        binding.usernameTil.error = viewModel.validateUsername(username)
        binding.phoneTil.error = viewModel.validatePhone(phone)
        binding.birthDateTil.error = viewModel.validateBirthDate(birthDate)
        binding.saveButton.isEnabled = viewModel.isFormValid(firstName, lastName, username, phone, birthDate)
    }

    private fun setupDatePicker() {
        binding.birthDateTiet.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                val formatted = "%04d-%02d-%02d".format(year, month + 1, day)
                binding.birthDateTiet.setText(formatted)
            }, cal.get(Calendar.YEAR) - 18, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .apply { datePicker.maxDate = System.currentTimeMillis() }.show()
        }
    }

    private fun setupClickListeners() {
        binding.saveButton.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) { Snackbar.make(binding.root, "Sesión inválida", Snackbar.LENGTH_LONG).show(); return@setOnClickListener }
            viewModel.saveProfile(uid, binding.firstNameTiet.text.toString().trim(), binding.lastNameTiet.text.toString().trim(), binding.usernameTiet.text.toString().trim(), binding.phoneTiet.text.toString().trim(), binding.birthDateTiet.text.toString().trim())
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> { communicator.manageLoader(true); binding.saveButton.isEnabled = false }
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)
                            val intent = Intent(requireContext(), HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        is ResponseService.Error -> { communicator.manageLoader(false); binding.saveButton.isEnabled = true; Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show() }
                        null -> Unit
                    }
                }
            }
        }
    }
}
