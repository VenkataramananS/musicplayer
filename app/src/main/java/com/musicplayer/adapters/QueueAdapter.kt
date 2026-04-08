package com.musicplayer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.databinding.ItemQueueBinding
import com.musicplayer.models.Song
import com.musicplayer.utils.TimeFormatter

class QueueAdapter(
    private val songs: MutableList<Song>,
    private val onRemoveClick: (Int) -> Unit,
    private val onReorderStart: (RecyclerView.ViewHolder) -> Unit
) : RecyclerView.Adapter<QueueAdapter.QueueViewHolder>() {

    inner class QueueViewHolder(private val binding: ItemQueueBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song, position: Int) {
            try {
                binding.apply {
                    queuePosition.text = (position + 1).toString()
                    queueSongTitle.text = song.title
                    queueSongArtist.text = song.artist
                    
                    removeButton.apply {
                        setOnClickListener { onRemoveClick(position) }
                        isClickable = true
                    }
                    
                    dragHandle.apply {
                        setOnLongClickListener {
                            onReorderStart(this@QueueViewHolder)
                            true
                        }
                        isLongClickable = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueViewHolder {
        try {
            val binding = ItemQueueBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return QueueViewHolder(binding)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override fun onBindViewHolder(holder: QueueViewHolder, position: Int) {
        try {
            if (position >= 0 && position < songs.size) {
                holder.bind(songs[position], position)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount() = songs.size

    fun updateQueue(newSongs: List<Song>) {
        try {
            val diffCallback = QueueDiffCallback(songs, newSongs)
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

    fun moveItem(from: Int, to: Int) {
        try {
            if (from >= 0 && from < songs.size && to >= 0 && to < songs.size && from != to) {
                val song = songs.removeAt(from)
                songs.add(to, song)
                notifyItemMoved(from, to)
                
                // Update position numbers for affected items
                val minPos = minOf(from, to)
                val maxPos = maxOf(from, to)
                notifyItemRangeChanged(minPos, maxPos - minPos + 1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun removeItem(position: Int) {
        try {
            if (position >= 0 && position < songs.size) {
                songs.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, songs.size - position)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private class QueueDiffCallback(
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