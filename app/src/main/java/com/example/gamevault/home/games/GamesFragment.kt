package com.example.gamevault.home.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.gamevault.R
import com.example.gamevault.core.FragmentCommunicator
import com.example.gamevault.core.ResponseService
import com.example.gamevault.databinding.FragmentGamesBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class GamesFragment : Fragment() {

    private var _binding: FragmentGamesBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<GamesViewModel>()
    private lateinit var communicator: FragmentCommunicator

    private val adapter = GamesAdapter { game ->
        findNavController().navigate(
            R.id.action_global_gameDetail,
            bundleOf("gameId" to game.id)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGamesBinding.inflate(inflater, container, false)
        communicator = requireActivity() as FragmentCommunicator
        binding.rvGames.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvGames.adapter = adapter
        setupSearch()
        setupRefresh()
        observeState()
        return binding.root
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            viewModel.onQueryChanged(text.toString())
        }
    }

    private fun setupRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary)
        binding.swipeRefresh.setOnRefreshListener { viewModel.load() }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.gameState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> {
                            if (!binding.swipeRefresh.isRefreshing) communicator.manageLoader(true)
                        }
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)
                            binding.swipeRefresh.isRefreshing = false
                            adapter.submitList(state.data)
                            binding.layoutEmpty.visibility =
                                if (state.data.isEmpty()) View.VISIBLE else View.GONE
                        }
                        is ResponseService.Error -> {
                            communicator.manageLoader(false)
                            binding.swipeRefresh.isRefreshing = false
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