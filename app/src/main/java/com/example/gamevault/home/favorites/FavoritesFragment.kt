package com.example.gamevault.home.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gamevault.R
import com.example.gamevault.core.FragmentCommunicator
import com.example.gamevault.core.ResponseService
import com.example.gamevault.databinding.FragmentFavoritesBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<FavoritesViewModel>()
    private lateinit var communicator: FragmentCommunicator

    private val adapter = FavoritesAdapter(
        onItemClick = { fav ->
            findNavController().navigate(
                R.id.action_global_gameDetail,
                bundleOf("gameId" to fav.id)
            )
        },
        onRemoveClick = { fav ->
            AlertDialog.Builder(requireContext())
                .setTitle("Quitar de tu lista")
                .setMessage("¿Eliminar \"${fav.name}\" de tus favoritos?")
                .setPositiveButton("Eliminar") { _, _ -> viewModel.remove(fav.id) }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        communicator = requireActivity() as FragmentCommunicator
        binding.rvFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavorites.adapter = adapter
        binding.btnExplorar.setOnClickListener {
            findNavController().navigate(R.id.gamesFragment)
        }
        observeState()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.load() // recarga al volver (por si agregaste/quitaste en el detalle)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> communicator.manageLoader(true)
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)
                            adapter.submitList(state.data)
                            val empty = state.data.isEmpty()
                            binding.layoutEmpty.visibility = if (empty) View.VISIBLE else View.GONE
                            binding.rvFavorites.visibility = if (empty) View.GONE else View.VISIBLE
                            binding.tvCount.text = "${state.data.size} juegos guardados"
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}