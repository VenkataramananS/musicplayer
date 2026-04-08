package com.musicplayer.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.musicplayer.database.MusicDatabase
import com.musicplayer.models.Song
import com.musicplayer.repositories.SongRepository
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var songRepository: SongRepository
    lateinit var allSongs: LiveData<List<Song>>
    lateinit var favoriteSongs: LiveData<List<Song>>
    lateinit var recentSongs: LiveData<List<Song>>

    private val _searchResults = MutableLiveData<List<Song>>()
    val searchResults: LiveData<List<Song>> = _searchResults

    private val _currentSong = MutableLiveData<Song>()
    val currentSong: LiveData<Song> = _currentSong

    private val _sortOption = MutableLiveData<String>("title")
    val sortOption: LiveData<String> = _sortOption

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        try {
            val database = MusicDatabase.getDatabase(application)
            val songDao = database.songDao()
            songRepository = SongRepository(songDao, application)

            allSongs = songRepository.getAllSongs()
            favoriteSongs = songRepository.getFavoriteSongs()
            recentSongs = songRepository.getRecentSongs()

            loadSongsFromDevice()
        } catch (e: Exception) {
            _errorMessage.value = "Failed to initialize database: ${e.message}"
            e.printStackTrace()
        }
    }

    fun loadSongsFromDevice() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                songRepository.loadSongsFromDevice()
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load songs: ${e.message}"
                _isLoading.value = false
                e.printStackTrace()
            }
        }
    }

    fun searchSongs(query: String) {
        try {
            if (query.isEmpty()) {
                _searchResults.value = emptyList()
            } else {
                viewModelScope.launch {
                    try {
                        val results = songRepository.searchSongs(query)
                        results.observeForever { songs ->
                            _searchResults.value = songs
                        }
                    } catch (e: Exception) {
                        _errorMessage.value = "Search failed: ${e.message}"
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            _errorMessage.value = "Search error: ${e.message}"
            e.printStackTrace()
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            try {
                val newFavoriteState = !song.isFavorite
                songRepository.updateFavorite(song.id, newFavoriteState)
                
                // Update current song if it's the same
                if (_currentSong.value?.id == song.id) {
                    _currentSong.value = song.copy(isFavorite = newFavoriteState)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update favorite: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    fun setCurrentSong(song: Song) {
        _currentSong.value = song
    }

    fun setSortOption(option: String) {
        _sortOption.value = option
    }

    fun clearError() {
        _errorMessage.value = ""
    }
}