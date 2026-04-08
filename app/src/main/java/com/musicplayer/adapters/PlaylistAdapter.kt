package com.musicplayer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.databinding.ItemPlaylistBinding
import com.musicplayer.models.Playlist

class PlaylistAdapter(
    private val playlists: MutableList<Playlist>,
    private val onPlaylistClick: (Playlist) -> Unit,
    private val onDeleteClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    inner class PlaylistViewHolder(private val binding: ItemPlaylistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            try {
                binding.apply {
                    playlistName.text = playlist.name
                    
                    root.apply {
                        setOnClickListener { onPlaylistClick(playlist) }
                        isClickable = true
                        isFocusable = true
                    }
                    
                    deleteButton.apply {
                        setOnClickListener { onDeleteClick(playlist) }
                        isClickable = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        try {
            val binding = ItemPlaylistBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return PlaylistViewHolder(binding)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        try {
            if (position >= 0 && position < playlists.size) {
                holder.bind(playlists[position])
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount() = playlists.size

    fun updatePlaylists(newPlaylists: List<Playlist>) {
        try {
            val diffCallback = PlaylistDiffCallback(playlists, newPlaylists)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            
            playlists.clear()
            playlists.addAll(newPlaylists)
            diffResult.dispatchUpdatesTo(this)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to notifyDataSetChanged if DiffUtil fails
            playlists.clear()
            playlists.addAll(newPlaylists)
            notifyDataSetChanged()
        }
    }

    private class PlaylistDiffCallback(
        private val oldList: List<Playlist>,
        private val newList: List<Playlist>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}