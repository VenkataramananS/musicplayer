package com.musicplayer.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.musicplayer.MainActivity
import com.musicplayer.R
import com.musicplayer.models.Song

class MusicService : Service(), MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private var mediaPlayer: MediaPlayer? = null
    private var currentSong: Song? = null
    private var isPlaying = false
    private var queue = mutableListOf<Song>()
    private var currentIndex = 0
    private var isShuffle = false
    private var repeatMode = RepeatMode.OFF
    private var wakeLock: PowerManager.WakeLock? = null
    private val binder = MusicBinder()

    enum class RepeatMode {
        OFF, ALL, ONE
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onCreate() {
        super.onCreate()
        try {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "MusicPlayer::WakeLock"
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            when (intent?.action) {
                ACTION_PLAY -> {
                    val song = intent.getParcelableExtra<Song>(EXTRA_SONG)
                    queue = intent.getParcelableArrayListExtra(EXTRA_QUEUE) ?: mutableListOf()
                    currentIndex = intent.getIntExtra(EXTRA_CURRENT_INDEX, 0)
                    playSong(song)
                }
                ACTION_PAUSE -> pauseSong()
                ACTION_RESUME -> resumeSong()
                ACTION_NEXT -> nextSong()
                ACTION_PREVIOUS -> previousSong()
                ACTION_STOP -> stopSong()
                ACTION_SET_QUEUE -> {
                    queue = intent.getParcelableArrayListExtra(EXTRA_QUEUE) ?: mutableListOf()
                    currentIndex = intent.getIntExtra(EXTRA_CURRENT_INDEX, 0)
                }
                ACTION_SHUFFLE -> toggleShuffle()
                ACTION_REPEAT -> toggleRepeat()
                ACTION_SEEK -> {
                    val position = intent.getIntExtra(EXTRA_SEEK_POSITION, 0)
                    seekTo(position)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return START_STICKY
    }

    private fun playSong(song: Song?) {
        try {
            if (song == null) return

            currentSong = song
            
            // Release old MediaPlayer
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }

            // Create new MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setDataSource(song.path)
                setOnCompletionListener(this@MusicService)
                setOnErrorListener(this@MusicService)
                prepareAsync()
                setOnPreparedListener { mp ->
                    mp.start()
                    this@MusicService.isPlaying = true
                    acquireWakeLock()
                    showNotification()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stopSong()
        }
    }

    private fun pauseSong() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    pause()
                    this@MusicService.isPlaying = false
                    releaseWakeLock()
                    showNotification()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun resumeSong() {
        try {
            mediaPlayer?.apply {
                if (!isPlaying) {
                    start()
                    this@MusicService.isPlaying = true
                    acquireWakeLock()
                    showNotification()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun nextSong() {
        try {
            if (queue.isEmpty()) return

            currentIndex = if (isShuffle) {
                (0 until queue.size).random()
            } else {
                (currentIndex + 1) % queue.size
            }

            playSong(queue[currentIndex])
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun previousSong() {
        try {
            if (queue.isEmpty()) return

            currentIndex = if (currentIndex > 0) currentIndex - 1 else queue.size - 1
            playSong(queue[currentIndex])
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopSong() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
            mediaPlayer = null
            isPlaying = false
            releaseWakeLock()
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun seekTo(position: Int) {
        try {
            mediaPlayer?.seekTo(position)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toggleShuffle() {
        isShuffle = !isShuffle
    }

    private fun toggleRepeat() {
        repeatMode = when (repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    private fun showNotification() {
        try {
            createNotificationChannel()

            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(currentSong?.title ?: "Unknown")
                .setContentText(currentSong?.artist ?: "Unknown Artist")
                .setSmallIcon(R.drawable.ic_music)
                .setContentIntent(pendingIntent)
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle())
                .build()

            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Player",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun acquireWakeLock() {
        try {
            if (wakeLock?.isHeld == false) {
                wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        try {
            when (repeatMode) {
                RepeatMode.OFF -> nextSong()
                RepeatMode.ALL -> nextSong()
                RepeatMode.ONE -> {
                    currentSong?.let { playSong(it) }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        try {
            when (what){
                MediaPlayer.MEDIA_ERROR_UNKNOWN -> {
                    android.util.Log.e("MusicService","Unknown media error")
                }

                MediaPlayer.MEDIA_ERROR_SERVER_DIED -> {
                    android.util.Log.e("MusicService","server died")
                }
            }
            nextSong()
            return true
        }


        catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
            mediaPlayer = null
            releaseWakeLock()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    fun getDuration(): Int = mediaPlayer?.duration ?: 0

    fun isPlayerPlaying(): Boolean = isPlaying

    companion object {
        const val ACTION_PLAY = "com.musicplayer.PLAY"
        const val ACTION_PAUSE = "com.musicplayer.PAUSE"
        const val ACTION_RESUME = "com.musicplayer.RESUME"
        const val ACTION_NEXT = "com.musicplayer.NEXT"
        const val ACTION_PREVIOUS = "com.musicplayer.PREVIOUS"
        const val ACTION_STOP = "com.musicplayer.STOP"
        const val ACTION_SET_QUEUE = "com.musicplayer.SET_QUEUE"
        const val ACTION_SHUFFLE = "com.musicplayer.SHUFFLE"
        const val ACTION_REPEAT = "com.musicplayer.REPEAT"
        const val ACTION_SEEK = "com.musicplayer.SEEK"

        const val EXTRA_SONG = "song"
        const val EXTRA_QUEUE = "queue"
        const val EXTRA_CURRENT_INDEX = "current_index"
        const val EXTRA_SEEK_POSITION = "seek_position"

        const val CHANNEL_ID = "music_channel"
        const val NOTIFICATION_ID = 1
    }
}