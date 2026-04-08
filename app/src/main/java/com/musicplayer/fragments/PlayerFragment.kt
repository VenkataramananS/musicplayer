package com.musicplayer.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.musicplayer.adapters.QueueAdapter
import com.musicplayer.databinding.FragmentPlayerBinding
import com.musicplayer.models.Song
import com.musicplayer.services.MusicService
import com.musicplayer.utils.TimeFormatter
import com.musicplayer.viewmodels.MusicViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerFragment : Fragment(), ServiceConnection {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    
    private val musicViewModel: MusicViewModel by viewModels()
    private lateinit var queueAdapter: QueueAdapter
    private var updateJob: Job? = null
    private var queue = mutableListOf<Song>()
    private var currentIndex = 0
    private var isShuffle = false
    private var repeatMode = MusicService.RepeatMode.OFF
    private var sleepTimerMinutes = 0
    private var musicService: MusicService? = null
    private var isServiceBound = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            _binding = FragmentPlayerBinding.inflate(inflater, container, false)
            return binding.root
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            setupQueue()
            setupControls()
            setupSeekBar()
            observeData()
            bindMusicService()
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Failed to setup player: ${e.message}")
        }
    }

    private fun bindMusicService() {
        try {
            val intent = Intent(requireContext(), MusicService::class.java)
            requireContext().bindService(intent, this, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        try {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isServiceBound = true
            startUpdateTimer()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        isServiceBound = false
        musicService = null
    }

    private fun setupQueue() {
        try {
            queueAdapter = QueueAdapter(
                mutableListOf(),
                onRemoveClick = { position ->
                    try {
                        queue.removeAt(position)
                        queueAdapter.updateQueue(queue)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showError("Failed to remove song: ${e.message}")
                    }
                },
                onReorderStart = { holder ->
                    // Implement drag and drop if needed
                }
            )

            binding.queueRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = queueAdapter
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Failed to setup queue: ${e.message}")
        }
    }

    private fun setupControls() {
        try {
            binding.apply {
                playButton.setOnClickListener { playSong() }
                pauseButton.setOnClickListener { pauseSong() }
                nextButton.setOnClickListener { nextSong() }
                previousButton.setOnClickListener { previousSong() }
                shuffleButton.setOnClickListener { toggleShuffle() }
                repeatButton.setOnClickListener { toggleRepeat() }
                favoriteButton.setOnClickListener { toggleFavorite() }
                sleepTimerButton.setOnClickListener { showSleepTimerDialog() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Failed to setup controls: ${e.message}")
        }
    }

    private fun setupSeekBar() {
        try {
            binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    try {
                        if (fromUser) {
                            binding.currentTimeText.text = TimeFormatter.formatSecondsToTime(progress.toLong() / 1000)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    try {
                        updateJob?.cancel()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    try {
                        if (seekBar != null) {
                            seekTo(seekBar.progress)
                            startUpdateTimer()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Failed to setup seekbar: ${e.message}")
        }
    }

    private fun playSong() {
        try {
            musicViewModel.currentSong.value?.let { song ->
                val intent = Intent(requireContext(), MusicService::class.java).apply {
                    action = MusicService.ACTION_PLAY
                    putExtra(MusicService.EXTRA_SONG, song)
                    putParcelableArrayListExtra(MusicService.EXTRA_QUEUE, ArrayList(queue))
                    putExtra(MusicService.EXTRA_CURRENT_INDEX, currentIndex)
                }
                requireContext().startService(intent)
                binding.playButton.visibility = View.GONE
                binding.pauseButton.visibility = View.VISIBLE
                startUpdateTimer()
            } ?: showError("No song selected")
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Failed to play: ${e.message}")
        }
    }

    private fun pauseSong() {
        try {
            val intent = Intent(requireContext(), MusicService::class.java).apply {
                action = MusicService.ACTION_PAUSE
            }
            requireContext().startService(intent)
            binding.playButton.visibility = View.VISIBLE
            binding.pauseButton.visibility = View.GONE
            updateJob?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Failed to pause: ${e.message}")
        }
    }

    private fun nextSong() {
        try {
            val intent = Intent(requireContext(), MusicService::class.java).apply {
                action = MusicService.ACTION_NEXT
            }
            requireContext().startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Failed to skip: ${e.message}")
        }
    }

    private fun previousSong() {
        try {
            val intent = Intent(requireContext(), MusicService::class.java).apply {
                action = MusicService.ACTION_PREVIOUS
            }
            requireContext().startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Failed to go back: ${e.message}")
        }
    }

    private fun seekTo(position: Int) {
        try {
            val intent = Intent(requireContext(), MusicService::class.java).apply {
                action = MusicService.ACTION_SEEK
                putExtra(MusicService.EXTRA_SEEK_POSITION, position)
            }
            requireContext().startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toggleShuffle() {
        try {
            isShuffle = !isShuffle
            binding.shuffleButton.isSelected = isShuffle
            val intent = Intent(requireContext(), MusicService::class.java).apply {
                action = MusicService.ACTION_SHUFFLE
            }
            requireContext().startService(intent)
            showMessage("Shuffle: ${if (isShuffle) "ON" else "OFF"}")
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Failed to toggle shuffle: ${e.message}")
        }
    }

    private fun toggleRepeat() {
        try {
            repeatMode = when (repeatMode) {
                MusicService.RepeatMode.OFF -> MusicService.RepeatMode.ALL
                MusicService.RepeatMode.ALL -> MusicService.RepeatMode.ONE
                MusicService.RepeatMode.ONE -> MusicService.RepeatMode.OFF
            }
            updateRepeatButtonUI()
            val intent = Intent(requireContext(), MusicService::class.java).apply {
                action = MusicService.ACTION_REPEAT
            }
            requireContext().startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Failed to toggle repeat: ${e.message}")
        }
    }

    private fun toggleFavorite() {
        try {
            musicViewModel.currentSong.value?.let { song ->
                musicViewModel.toggleFavorite(song)
                binding.favoriteButton.isSelected = !binding.favoriteButton.isSelected
                showMessage(if (binding.favoriteButton.isSelected) "Added to favorites" else "Removed from favorites")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Failed to update favorite: ${e.message}")
        }
    }

    private fun showSleepTimerDialog() {
        try {
            val options = arrayOf("Off", "5 min", "10 min", "15 min", "30 min")
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Sleep Timer")
                .setItems(options) { _, which ->
                    try {
                        sleepTimerMinutes = when (which) {
                            0 -> 0
                            1 -> 5
                            2 -> 10
                            3 -> 15
                            4 -> 30
                            else -> 0
                        }
                        if (sleepTimerMinutes > 0) {
                            startSleepTimer()
                            showMessage("Sleep timer set for $sleepTimerMinutes minutes")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showError("Timer error: ${e.message}")
                    }
                }
                .show()
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Failed to show timer dialog: ${e.message}")
        }
    }

    private fun startSleepTimer() {
        try {
            lifecycleScope.launch {
                delay((sleepTimerMinutes * 60 * 1000).toLong())
                pauseSong()
                showMessage("Sleep timer ended")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startUpdateTimer() {
        try {
            updateJob?.cancel()
            updateJob = lifecycleScope.launch {
                while (true) {
                    try {
                        delay(1000)
                        updateSeekBar()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        break
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateSeekBar() {
        try {
            if (isServiceBound && musicService != null) {
                val currentPosition = musicService!!.getCurrentPosition()
                binding.apply {
                    seekBar.progress = currentPosition
                    currentTimeText.text = TimeFormatter.formatSecondsToTime(currentPosition.toLong() / 1000)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateRepeatButtonUI() {
        try {
            binding.repeatButton.contentDescription = when (repeatMode) {
                MusicService.RepeatMode.OFF -> "OFF"
                MusicService.RepeatMode.ALL -> "ALL"
                MusicService.RepeatMode.ONE -> "ONE"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun observeData() {
        try {
            musicViewModel.currentSong.observe(viewLifecycleOwner) { song ->
                try {
                    binding.apply {
                        songTitle.text = song.title
                        songArtist.text = song.artist
                        totalTimeText.text = TimeFormatter.formatMillisToTime(song.duration)
                        seekBar.max = song.duration.toInt()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            musicViewModel.allSongs.observe(viewLifecycleOwner) { songs ->
                try {
                    queue.clear()
                    queue.addAll(songs)
                    queueAdapter.updateQueue(queue)
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
        try {
            updateJob?.cancel()
            if (isServiceBound) {
                requireContext().unbindService(this)
                isServiceBound = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _binding = null
    }
}