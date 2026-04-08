package com.musicplayer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.databinding.ItemSongBinding
import com.musicplayer.models.Song
import com.musicplayer.utils.TimeFormatter

class SongAdapter(
    private val songs: MutableList<Song>,
    private val onSongClick: (Song) -> Unit,
    private val onFavoriteClick: (Song) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    inner class SongViewHolder(private val binding: ItemSongBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song) {
            try {
                binding.apply {
                    songTitle.text = song.title
                    songArtist.text = song.artist
                    songDuration.text = TimeFormatter.formatMillisToTime(song.duration)
                    
                    // Set favorite icon based on state
                    favoriteIcon.apply {
                        isSelected = song.isFavorite
                        setImageResource(
                            if (song.isFavorite) 
                                android.R.drawable.ic_menu_view 
                            else 
                                android.R.drawable.ic_menu_add
                        )
                    }

                    // Set click listeners
                    root.apply {
                        setOnClickListener { onSongClick(song) }
                        isClickable = true
                        isFocusable = true
                    }
                    
                    favoriteIcon.setOnClickListener { 
                        onFavoriteClick(song)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        try {
            val binding = ItemSongBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return SongViewHolder(binding)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        try {
            if (position >= 0 && position < songs.size) {
                holder.bind(songs[position])
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount() = songs.size

    fun updateSongs(newSongs: List<Song>) {
        try {
            val diffCallback = SongDiffCallback(songs, newSongs)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            
            songs.clear()
            songs.addAll(newSongs)
            diffResult.dispatchUpdatesTo(this)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to notifyDataSetChanged if DiffUtil fails
            songs.clear()
            songs.addAll(newSongs)
            notifyDataSetChanged()
        }
    }

    private class SongDiffCallback(
        private val oldList: List<Song>,
        private val newList: List<Song>
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