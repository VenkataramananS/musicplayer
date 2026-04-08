package com.musicplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicplayer.adapters.SongAdapter
import com.musicplayer.databinding.FragmentSearchBinding
import com.musicplayer.viewmodels.MusicViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    
    private val musicViewModel: MusicViewModel by viewModels()
    private lateinit var songAdapter: SongAdapter
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            _binding = FragmentSearchBinding.inflate(inflater, container, false)
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
            setupSearch()
            observeData()
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Failed to setup search fragment: ${e.message}")
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

        binding.searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter
        }
    }

    private fun setupSearch() {
        try {
            binding.searchEditText.addTextChangedListener { text ->
                try {
                    // Cancel previous search job
                    searchJob?.cancel()
                    
                    // Debounce search with 500ms delay
                    searchJob = lifecycleScope.launch {
                        delay(500)
                        val query = text.toString().trim()
                        
                        if (query.isNotEmpty()) {
                            binding.emptyStateText?.visibility = View.GONE
                            musicViewModel.searchSongs(query)
                        } else {
                            songAdapter.updateSongs(emptyList())
                            binding.emptyStateText?.apply {
                                this.text = "Type to search songs"
                                visibility = View.VISIBLE
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    showError("Search error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Failed to setup search: ${e.message}")
        }
    }

    private fun observeData() {
        try {
            musicViewModel.searchResults.observe(viewLifecycleOwner) { songs ->
                try {
                    if (songs.isNotEmpty()) {
                        songAdapter.updateSongs(songs)
                        binding.emptyStateText?.visibility = View.GONE
                    } else {
                        val query = binding.searchEditText.text.toString()
                        if (query.isNotEmpty()) {
                            binding.emptyStateText?.apply {
                                text = "No results found for \"$query\""
                                visibility = View.VISIBLE
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    showError("Failed to display results: ${e.message}")
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
        searchJob?.cancel()
        _binding = null
    }
}