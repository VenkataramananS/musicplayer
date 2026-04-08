package com.musicplayer.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.musicplayer.database.MusicDatabase
import com.musicplayer.models.Playlist
import com.musicplayer.models.Song
import com.musicplayer.repositories.PlaylistRepository
import kotlinx.coroutines.launch

class PlaylistViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var playlistRepository: PlaylistRepository
    lateinit var allPlaylists: LiveData<List<Playlist>>

    private val _currentPlaylistSongs = MutableLiveData<List<Song>>()
    val currentPlaylistSongs: LiveData<List<Song>> = _currentPlaylistSongs

    private val _currentPlaylist = MutableLiveData<Playlist>()
    val currentPlaylist: LiveData<Playlist> = _currentPlaylist

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    init {
        try {
            val database = MusicDatabase.getDatabase(application)
            val playlistDao = database.playlistDao()
            playlistRepository = PlaylistRepository(playlistDao)
            allPlaylists = playlistRepository.getAllPlaylists()
        } catch (e: Exception) {
            _errorMessage.value = "Failed to initialize playlists: ${e.message}"
            e.printStackTrace()
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val playlistId = playlistRepository.createPlaylist(name)
                _isLoading.value = false
                
                if (playlistId > 0) {
                    _successMessage.value = "Playlist created successfully"
                } else {
                    _errorMessage.value = "Failed to create playlist"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error creating playlist: ${e.message}"
                _isLoading.value = false
                e.printStackTrace()
            }
        }
    }

    fun loadPlaylistSongs(playlistId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val songsLiveData = playlistRepository.getPlaylistSongs(playlistId)
                songsLiveData.observeForever { songs ->
                    _currentPlaylistSongs.value = songs
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load playlist songs: ${e.message}"
                _isLoading.value = false
                e.printStackTrace()
            }
        }
    }

    fun addSongToPlaylist(playlistId: Int, songId: Long) {
        viewModelScope.launch {
            try {
                playlistRepository.addSongToPlaylist(playlistId, songId)
                _successMessage.value = "Song added to playlist"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add song: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    fun removeSongFromPlaylist(playlistId: Int, songId: Long) {
        viewModelScope.launch {
            try {
                playlistRepository.removeSongFromPlaylist(playlistId, songId)
                _successMessage.value = "Song removed from playlist"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to remove song: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            try {
                playlistRepository.deletePlaylist(playlist)
                _successMessage.value = "Playlist deleted"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete playlist: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    fun setCurrentPlaylist(playlist: Playlist) {
        _currentPlaylist.value = playlist
    }

    fun clearMessages() {
        _errorMessage.value = ""
        _successMessage.value = ""
    }
}