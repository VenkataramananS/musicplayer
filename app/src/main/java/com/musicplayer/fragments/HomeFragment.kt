package com.musicplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicplayer.adapters.SongAdapter
import com.musicplayer.databinding.FragmentHomeBinding
import com.musicplayer.viewmodels.MusicViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val musicViewModel: MusicViewModel by viewModels()
    private lateinit var songAdapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            _binding = FragmentHomeBinding.inflate(inflater, container, false)
            return binding.root
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            setupRecyclerView()
            observeData()
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Failed to setup home fragment: ${e.message}")
        }
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            mutableListOf(),
            onSongClick = { song ->
                try {
                    musicViewModel.setCurrentSong(song)
                    showMessage("Playing: ${song.title}")
                } catch (e: Exception) {
                    e.printStackTrace()
                    showError("Failed to play song: ${e.message}")
                }
            },
            onFavoriteClick = { song ->
                try {
                    musicViewModel.toggleFavorite(song)
                } catch (e: Exception) {
                    e.printStackTrace()
                    showError("Failed to update favorite: ${e.message}")
                }
            }
        )

        binding.recentSongsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter
        }
    }

    private fun observeData() {
        try {
            musicViewModel.recentSongs.observe(viewLifecycleOwner) { songs ->
                try {
                    if (songs.isNotEmpty()) {
                        songAdapter.updateSongs(songs)
                        binding.emptyStateText?.visibility = View.GONE
                    } else {
                        binding.emptyStateText?.apply {
                            text = "No recent songs"
                            visibility = View.VISIBLE
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    showError("Failed to load songs: ${e.message}")
                }
            }

            musicViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                try {
                    binding.loadingProgressBar?.visibility = 
                        if (isLoading) View.VISIBLE else View.GONE
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            musicViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
                try {
                    if (error.isNotEmpty()) {
                        showError(error)
                        musicViewModel.clearError()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Failed to observe data: ${e.message}")
        }
    }

    private fun showError(message: String) {
        try {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showMessage(message: String) {
        try {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}