package com.musicplayer.repositories

import androidx.lifecycle.LiveData
import com.musicplayer.database.PlaylistDao
import com.musicplayer.models.Playlist
import com.musicplayer.models.PlaylistSong
import com.musicplayer.models.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlaylistRepository(private val playlistDao: PlaylistDao) {

    fun getAllPlaylists(): LiveData<List<Playlist>> = playlistDao.getAllPlaylists()

    fun getPlaylistSongs(playlistId: Int): LiveData<List<Song>> = 
        playlistDao.getPlaylistSongs(playlistId)

    suspend fun createPlaylist(name: String): Long {
        return withContext(Dispatchers.IO) {
            try {
                if (name.isBlank()) {
                    throw IllegalArgumentException("Playlist name cannot be empty")
                }
                playlistDao.insertPlaylist(Playlist(name = name.trim()))
            } catch (e: Exception) {
                e.printStackTrace()
                -1L
            }
        }
    }

    suspend fun addSongToPlaylist(playlistId: Int, songId: Long) {
        withContext(Dispatchers.IO) {
            try {
                playlistDao.addSongToPlaylist(PlaylistSong(playlistId, songId))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun removeSongFromPlaylist(playlistId: Int, songId: Long) {
        withContext(Dispatchers.IO) {
            try {
                playlistDao.removeSongFromPlaylist(playlistId, songId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        withContext(Dispatchers.IO) {
            try {
                playlistDao.deletePlaylist(playlist)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}