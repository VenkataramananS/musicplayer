package com.musicplayer.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.musicplayer.adapters.PlaylistAdapter
import com.musicplayer.databinding.FragmentPlaylistBinding
import com.musicplayer.viewmodels.PlaylistViewModel

class PlaylistFragment : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!
    
    private val playlistViewModel: PlaylistViewModel by viewModels()
    private lateinit var playlistAdapter: PlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
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
            setupCreateButton()
            observeData()
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Failed to setup playlist fragment: ${e.message}")
        }
    }

    private fun setupRecyclerView() {
        playlistAdapter = PlaylistAdapter(
            mutableListOf(),
            onPlaylistClick = { playlist ->
                try {
                    playlistViewModel.setCurrentPlaylist(playlist)
                    playlistViewModel.loadPlaylistSongs(playlist.id)
                    showMessage("Loaded: ${playlist.name}")
                } catch (e: Exception) {
                    e.printStackTrace()
                    showError("Failed to load playlist: ${e.message}")
                }
            },
            onDeleteClick = { playlist ->
                try {
                    showDeleteConfirmation(playlist)
                } catch (e: Exception) {
                    e.printStackTrace()
                    showError("Failed to delete playlist: ${e.message}")
                }
            }
        )

        binding.playlistRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = playlistAdapter
        }
    }

    private fun setupCreateButton() {
        try {
            binding.createPlaylistButton.setOnClickListener {
                try {
                    showCreatePlaylistDialog()
                } catch (e: Exception) {
                    e.printStackTrace()
                    showError("Failed to open dialog: ${e.message}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showCreatePlaylistDialog() {
        try {
            var input = ""
            val editText = EditText(requireContext()).apply {
                hint = "Playlist name"
                setPadding(48, 32, 48, 32)
            }

            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    input = s.toString().trim()
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Create Playlist")
                .setMessage("Enter a name for your new playlist")
                .setView(editText)
                .setPositiveButton("Create") { _, _ ->
                    try {
                        if (input.isNotEmpty()) {
                            playlistViewModel.createPlaylist(input)
                        } else {
                            showError("Playlist name cannot be empty")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showError("Failed to create playlist: ${e.message}")
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Dialog error: ${e.message}")
        }
    }

    private fun showDeleteConfirmation(playlist: com.musicplayer.models.Playlist) {
        try {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Playlist")
                .setMessage("Are you sure you want to delete \"${playlist.name}\"?")
                .setPositiveButton("Delete") { _, _ ->
                    try {
                        playlistViewModel.deletePlaylist(playlist)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showError("Failed to delete: ${e.message}")
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Confirmation error: ${e.message}")
        }
    }

    private fun observeData() {
        try {
            playlistViewModel.allPlaylists.observe(viewLifecycleOwner) { playlists ->
                try {
                    if (playlists.isNotEmpty()) {
                        playlistAdapter.updatePlaylists(playlists)
                        binding.emptyStateText?.visibility = View.GONE
                    } else {
                        binding.emptyStateText?.apply {
                            text = "No playlists yet. Create one!"
                            visibility = View.VISIBLE
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    showError("Failed to display playlists: ${e.message}")
                }
            }

            playlistViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                try {
                    binding.loadingProgressBar?.visibility = 
                        if (isLoading) View.VISIBLE else View.GONE
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            playlistViewModel.successMessage.observe(viewLifecycleOwner) { message ->
                try {
                    if (message.isNotEmpty()) {
                        showMessage(message)
                        playlistViewModel.clearMessages()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            playlistViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
                try {
                    if (error.isNotEmpty()) {
                        showError(error)
                        playlistViewModel.clearMessages()
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