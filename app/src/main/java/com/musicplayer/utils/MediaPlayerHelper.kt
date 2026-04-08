package com.musicplayer.utils

import android.media.MediaPlayer
import android.util.Log

object MediaPlayerHelper {

    private const val TAG = "MediaPlayerHelper"

    fun createMediaPlayer(): MediaPlayer {
        return try {
            MediaPlayer()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create MediaPlayer: ${e.message}")
            throw e
        }
    }

    fun prepareAsync(
        mediaPlayer: MediaPlayer,
        filePath: String,
        onPrepared: (MediaPlayer) -> Unit,
        onError: (Int, Int) -> Unit
    ) {
        try {
            mediaPlayer.apply {
                setDataSource(filePath)
                setOnPreparedListener { mp ->
                    try {
                        onPrepared(mp)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in onPrepared: ${e.message}")
                    }
                }
                setOnErrorListener { mp, what, extra ->
                    try {
                        onError(what, extra)
                        true
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in onError: ${e.message}")
                        false
                    }
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to prepare media: ${e.message}")
            onError(-1, -1)
        }
    }

    fun play(mediaPlayer: MediaPlayer?): Boolean {
        return try {
            if (mediaPlayer != null && !mediaPlayer.isPlaying) {
                mediaPlayer.start()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play: ${e.message}")
            false
        }
    }

    fun pause(mediaPlayer: MediaPlayer?): Boolean {
        return try {
            if (mediaPlayer != null && mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pause: ${e.message}")
            false
        }
    }

    fun stop(mediaPlayer: MediaPlayer?): Boolean {
        return try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop: ${e.message}")
            false
        }
    }

    fun release(mediaPlayer: MediaPlayer?) {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release: ${e.message}")
        }
    }

    fun seekTo(mediaPlayer: MediaPlayer?, position: Int): Boolean {
        return try {
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(position)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to seek: ${e.message}")
            false
        }
    }

    fun getCurrentPosition(mediaPlayer: MediaPlayer?): Int {
        return try {
            mediaPlayer?.currentPosition ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get current position: ${e.message}")
            0
        }
    }

    fun getDuration(mediaPlayer: MediaPlayer?): Int {
        return try {
            mediaPlayer?.duration ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get duration: ${e.message}")
            0
        }
    }

    fun isPlaying(mediaPlayer: MediaPlayer?): Boolean {
        return try {
            mediaPlayer?.isPlaying ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check if playing: ${e.message}")
            false
        }
    }

    fun setVolume(mediaPlayer: MediaPlayer?, leftVolume: Float, rightVolume: Float): Boolean {
        return try {
            if (mediaPlayer != null) {
                val volume = when {
                    leftVolume < 0f -> 0f
                    leftVolume > 1f -> 1f
                    else -> leftVolume
                }
                mediaPlayer.setVolume(volume, volume)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set volume: ${e.message}")
            false
        }
    }

    fun setLooping(mediaPlayer: MediaPlayer?, looping: Boolean): Boolean {
        return try {
            if (mediaPlayer != null) {
                mediaPlayer.isLooping = looping
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set looping: ${e.message}")
            false
        }
    }

    fun getFormattedTime(milliseconds: Long): String {
        return try {
            TimeFormatter.formatMillisToTime(milliseconds)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to format time: ${e.message}")
            "00:00"
        }
    }
}