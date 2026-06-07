package com.example.gamevault.home.account

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.gamevault.core.FragmentCommunicator
import com.example.gamevault.core.ResponseService
import com.example.gamevault.databinding.FragmentAccountBinding
import com.example.gamevault.onboarding.MainActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.bumptech.glide.Glide
import java.io.File

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<AccountViewModel>()
    private lateinit var communicator: FragmentCommunicator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        communicator = requireActivity() as FragmentCommunicator
        setupListeners()
        observeState()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.load() // refresca el conteo de favoritos al volver
    }

    private fun setupListeners() {
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

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        startActivity(
            Intent(requireContext(), MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        requireActivity().finish()
    }

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

    private fun bindData(data: AccountData) {
        val p = data.profile
        val fullName = p.nombreCompleto()
        binding.tvUserName.text = fullName.ifBlank { p.nombreUsuario.ifBlank { "Gamer" } }
        binding.tvUserEmail.text = data.email
        binding.tvFullName.text = fullName.ifBlank { "—" }
        binding.tvPhone.text = p.telefono.ifBlank { "—" }
        binding.tvFavCount.text = "${data.favoritesCount} títulos guardados"
        binding.tvInitials.text = (p.nombre.firstOrNull()?.uppercase() ?: "G")

        if (p.fotoUrl.isNotBlank() && File(p.fotoUrl).exists()) {
            binding.imgAvatar.visibility = android.view.View.VISIBLE
            binding.tvInitials.visibility = android.view.View.GONE
            Glide.with(this).load(File(p.fotoUrl)).centerCrop().into(binding.imgAvatar)
        } else {
            binding.imgAvatar.visibility = android.view.View.GONE
            binding.tvInitials.visibility = android.view.View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}