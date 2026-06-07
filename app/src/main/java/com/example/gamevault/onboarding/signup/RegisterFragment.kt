package com.example.gamevault.onboarding.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.gamevault.R
import com.example.gamevault.core.FragmentCommunicator
import com.example.gamevault.core.ResponseService
import com.example.gamevault.databinding.FragmentRegisterBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<RegisterViewModel>()
    private lateinit var communicator: FragmentCommunicator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        communicator = requireActivity() as FragmentCommunicator
        setupClickListeners()
        observeState()
        return binding.root
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmailEdit.text.toString().trim()
            val pass = binding.etPasswordEdit.text.toString().trim()
            val confirm = binding.etConfirmEdit.text.toString().trim()

            val emailErr = viewModel.validateEmail(email)
            val passErr = viewModel.validatePassword(pass)
            val confirmErr = viewModel.validateConfirmPassword(pass, confirm)
            binding.etEmail.error = emailErr
            binding.etPassword.error = passErr
            binding.etConfirmPassword.error = confirmErr

            if (emailErr == null && passErr == null && confirmErr == null) {
                viewModel.requestSignUp(email, pass)
            }
        }
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.tvGoToLogin.setOnClickListener { findNavController().popBackStack() }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registerState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> {
                            communicator.manageLoader(true)
                            binding.btnRegister.isEnabled = false
                        }
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)
                            findNavController().navigate(R.id.action_register_to_personal)
                        }
                        is ResponseService.Error -> {
                            communicator.manageLoader(false)
                            binding.btnRegister.isEnabled = true
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