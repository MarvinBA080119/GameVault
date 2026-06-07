package com.example.gamevault.home.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gamevault.R
import com.example.gamevault.core.model.FavoriteGame
import com.example.gamevault.databinding.ItemFavoriteBinding

class FavoritesAdapter(
    private val onItemClick: (FavoriteGame) -> Unit = {},
    private val onRemoveClick: (FavoriteGame) -> Unit = {}
) : ListAdapter<FavoriteGame, FavoritesAdapter.FavViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavViewHolder {
        val binding = ItemFavoriteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FavViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FavViewHolder(
        private val binding: ItemFavoriteBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FavoriteGame) {
            binding.tvTitle.text = item.name
            binding.tvMeta.text = "★ ${item.rating} · ${item.genres}"

            Glide.with(binding.imgCover)
                .load(item.backgroundImage)
                .centerCrop()
                .placeholder(R.color.bg_surface)
                .into(binding.imgCover)

            binding.root.setOnClickListener { onItemClick(item) }
            binding.btnRemove.setOnClickListener { onRemoveClick(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<FavoriteGame>() {
            override fun areItemsTheSame(o: FavoriteGame, n: FavoriteGame) = o.id == n.id
            override fun areContentsTheSame(o: FavoriteGame, n: FavoriteGame) = o == n
        }
    }
}