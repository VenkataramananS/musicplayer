package com.musicplayer.repositories

import android.content.Context
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import com.musicplayer.database.SongDao
import com.musicplayer.models.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SongRepository(private val songDao: SongDao, private val context: Context) {

    fun getAllSongs(): LiveData<List<Song>> = songDao.getAllSongs()

    fun searchSongs(query: String): LiveData<List<Song>> = songDao.searchSongs(query)

    fun getFavoriteSongs(): LiveData<List<Song>> = songDao.getFavoriteSongs()

    fun getRecentSongs(): LiveData<List<Song>> = songDao.getRecentSongs()

    suspend fun updateFavorite(songId: Long, isFavorite: Boolean) {
        withContext(Dispatchers.IO) {
            try {
                songDao.updateFavorite(songId, isFavorite)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun loadSongsFromDevice() {
        withContext(Dispatchers.IO) {
            try {
                val songs = mutableListOf<Song>()
                val projection = arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.DATA
                )

                val cursor = context.contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    null
                )

                cursor?.use { c ->
                    val idColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val titleColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val artistColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val albumColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    val durationColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    val dataColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                    while (c.moveToNext()) {
                        val title = c.getString(titleColumn) ?: "Unknown"
                        val artist = c.getString(artistColumn) ?: "Unknown Artist"
                        val album = c.getString(albumColumn) ?: "Unknown Album"
                        val path = c.getString(dataColumn) ?: ""

                        // Only add songs with valid paths
                        if (path.isNotEmpty()) {
                            val song = Song(
                                id = c.getLong(idColumn),
                                title = title,
                                artist = artist,
                                album = album,
                                duration = c.getLong(durationColumn),
                                path = path
                            )
                            songs.add(song)
                        }
                    }
                }

                if (songs.isNotEmpty()) {
                    songDao.insertAllSongs(songs)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}