package com.example.gamevault.home.gameDetail

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
import com.bumptech.glide.Glide
import com.example.gamevault.R
import com.example.gamevault.core.FragmentCommunicator
import com.example.gamevault.core.ResponseService
import com.example.gamevault.core.model.GameDetail
import com.example.gamevault.databinding.FragmentGameDetailBinding
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class GameDetailFragment : Fragment() {

    private var _binding: FragmentGameDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<GameDetailViewModel>()
    private lateinit var communicator: FragmentCommunicator

    private var gameId: Int = 0
    private var currentDetail: GameDetail? = null
    private var descriptionExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameId = requireArguments().getInt("gameId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameDetailBinding.inflate(inflater, container, false)
        communicator = requireActivity() as FragmentCommunicator
        setupListeners()
        observeState()
        viewModel.load(gameId)
        return binding.root
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        val toggle = View.OnClickListener {
            currentDetail?.let { viewModel.toggleFavorite(it) }
        }
        binding.btnWishToggle.setOnClickListener(toggle)
        binding.btnAddToWishlist.setOnClickListener(toggle)

        binding.tvSeeMore.setOnClickListener {
            descriptionExpanded = !descriptionExpanded
            binding.tvDescription.maxLines = if (descriptionExpanded) Int.MAX_VALUE else 5
            binding.tvSeeMore.text = if (descriptionExpanded) "Ver menos" else "Ver más"
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.detailState.collect { state ->
                        when (state) {
                            is ResponseService.Loading -> communicator.manageLoader(true)
                            is ResponseService.Success -> {
                                communicator.manageLoader(false)
                                currentDetail = state.data
                                bindDetail(state.data)
                            }
                            is ResponseService.Error -> {
                                communicator.manageLoader(false)
                                Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                            }
                            null -> Unit
                        }
                    }
                }
                launch {
                    viewModel.isFavorite.collect { fav -> renderFavorite(fav) }
                }
                launch {
                    viewModel.toast.collect { msg ->
                        if (msg != null) {
                            Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
                            viewModel.clearToast()
                        }
                    }
                }
            }
        }
    }

    private fun bindDetail(d: GameDetail) {
        binding.tvTitle.text = d.name
        binding.tvMeta.text = "Lanzado el ${d.released ?: "—"} · ${d.developerName()}"
        binding.tvRating.text = "${d.rating} (${d.ratingsCount})"
        binding.tvDevValue.text = d.developerName()
        binding.tvPlaytimeValue.text = "~ ${d.playtime} horas"
        binding.tvDescription.text = d.descriptionRaw ?: "Sin descripción disponible."

        Glide.with(binding.imgHero).load(d.backgroundImage).centerCrop().into(binding.imgHero)

        // Metacritic
        if (d.metacritic != null) {
            binding.tvMetacritic.visibility = View.VISIBLE
            binding.tvMetacritic.text = d.metacritic.toString()
        } else binding.tvMetacritic.visibility = View.GONE

        // ESRB
        if (d.esrbRating != null) {
            binding.tvEsrb.visibility = View.VISIBLE
            binding.tvEsrb.text = d.esrbRating.name
        } else binding.tvEsrb.visibility = View.GONE

        // Géneros (chips informativos)
        binding.chipGroupGenres.removeAllViews()
        d.genres.forEach { g ->
            binding.chipGroupGenres.addView(makeChip(g.name, genre = true))
        }
        // Plataformas
        binding.chipGroupPlatforms.removeAllViews()
        d.platforms.forEach { p ->
            binding.chipGroupPlatforms.addView(makeChip(p.platform.shortName(), genre = false))
        }
    }

    private fun makeChip(text: String, genre: Boolean): Chip {
        return Chip(requireContext()).apply {
            this.text = text
            isClickable = false
            isCheckable = false
            chipMinHeight = 64f
            if (genre) {
                setChipBackgroundColorResource(R.color.chip_genre_bg)
                setTextColor(resources.getColor(R.color.primary, null))
            } else {
                setChipBackgroundColorResource(R.color.platform_pill_bg)
                setTextColor(resources.getColor(R.color.text_primary, null))
            }
            textSize = 12f
        }
    }

    private fun renderFavorite(fav: Boolean) {
        val icon = if (fav) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        binding.btnWishToggle.setImageResource(icon)
        binding.btnAddToWishlist.setIconResource(icon)
        binding.btnAddToWishlist.text =
            if (fav) "Quitar de mi lista" else "Agregar a mi lista"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}