package com.musicplayer.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.musicplayer.models.Song

@Dao
interface SongDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSongs(songs: List<Song>)

    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): LiveData<List<Song>>

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%'")
    fun searchSongs(query: String): LiveData<List<Song>>

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteSongs(): LiveData<List<Song>>

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :songId")
    suspend fun updateFavorite(songId: Long, isFavorite: Boolean)

    @Query("SELECT * FROM songs ORDER BY dateAdded DESC LIMIT 10")
    fun getRecentSongs(): LiveData<List<Song>>

    @Delete
    suspend fun deleteSong(song: Song)

    @Query("SELECT * FROM songs WHERE id = :songId")
    fun getSongById(songId: Long): LiveData<Song>
}