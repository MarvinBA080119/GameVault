package com.example.gamevault.onboarding.signin

import android.content.Intent
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
import com.example.gamevault.databinding.FragmentLoginBinding
import com.example.gamevault.home.HomeActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<SignInViewModel>()
    private lateinit var communicator: FragmentCommunicator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        communicator = requireActivity() as FragmentCommunicator
        setupClickListeners()
        observeState()
        return binding.root
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmailEdit.text.toString().trim()
            val password = binding.etPasswordEdit.text.toString().trim()

            val emailErr = viewModel.validateEmail(email)
            val passErr = viewModel.validatePassword(password)
            binding.etEmail.error = emailErr
            binding.etPassword.error = passErr

            if (emailErr == null && passErr == null) {
                viewModel.requestLogin(email, password)
            }
        }
        binding.tvGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmailEdit.text.toString().trim()
            if (email.isBlank()) {
                binding.etEmail.error = "Ingresa tu correo primero"
            } else {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Snackbar.make(binding.root, "Enlace enviado a $email", Snackbar.LENGTH_LONG).show()
                    }
                    .addOnFailureListener {
                        Snackbar.make(binding.root, "No se pudo enviar el enlace", Snackbar.LENGTH_LONG).show()
                    }
            }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signInState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> {
                            communicator.manageLoader(true)
                            binding.btnLogin.isEnabled = false
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
                            binding.btnLogin.isEnabled = true
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