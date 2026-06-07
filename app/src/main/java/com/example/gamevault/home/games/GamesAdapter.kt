package com.example.gamevault.home.games

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gamevault.R
import com.example.gamevault.core.model.Game
import com.example.gamevault.databinding.ItemGameBinding

class GamesAdapter(
    private val onItemClick: (Game) -> Unit = {}
) : ListAdapter<Game, GamesAdapter.GameViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val binding = ItemGameBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return GameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GameViewHolder(
        private val binding: ItemGameBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(game: Game) {
            binding.tvTitle.text = game.name
            binding.tvGenres.text = game.genresText()
            binding.tvPlatforms.text = game.platformsText()
            binding.chipRating.text = "★ ${game.rating}"

            Glide.with(binding.imgCover)
                .load(game.backgroundImage)
                .centerCrop()
                .placeholder(R.color.bg_surface)
                .into(binding.imgCover)

            binding.root.setOnClickListener { onItemClick(game) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Game>() {
            override fun areItemsTheSame(o: Game, n: Game) = o.id == n.id
            override fun areContentsTheSame(o: Game, n: Game) = o == n
        }
    }
}