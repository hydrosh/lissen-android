package org.grakovne.lissen.automotive.screen

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.car.app.model.Template
import androidx.car.app.model.MessageTemplate
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import org.grakovne.lissen.R
import org.grakovne.lissen.automotive.LissenCarEntryPoint
import org.grakovne.lissen.domain.DetailedItem
import org.grakovne.lissen.playback.MediaRepository

class PlayerScreen(
    carContext: CarContext,
    private val book: DetailedItem
) : Screen(carContext), DefaultLifecycleObserver {

    private lateinit var mediaRepository: MediaRepository

    private var isPlaying = false
    private var currentPosition = 0.0
    private var totalDuration = 0.0

    private val playingObserver = Observer<Boolean> { playing ->
        isPlaying = playing
        invalidate()
    }

    private val positionObserver = Observer<Double> { position ->
        currentPosition = position
        invalidate()
    }

    init {
        lifecycle.addObserver(this)
        
        // Initialize dependencies
        val entryPoint = EntryPointAccessors.fromApplication(
            carContext,
            LissenCarEntryPoint::class.java
        )
        
        mediaRepository = entryPoint.mediaRepository()
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        
        // Observe playback state
        mediaRepository.isPlaying.observe(this, playingObserver)
        mediaRepository.totalPosition.observe(this, positionObserver)
        
        // Calculate total duration
        totalDuration = book.chapters.sumOf { it.duration }
        
        // Start playback if needed
        lifecycleScope.launch {
            val currentBook = mediaRepository.playingBook.value
            if (currentBook?.id != book.id) {
                mediaRepository.preparePlayback(book.id)
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        mediaRepository.isPlaying.removeObserver(playingObserver)
        mediaRepository.totalPosition.removeObserver(positionObserver)
        super.onDestroy(owner)
    }

    override fun onGetTemplate(): Template {
        val playPauseAction = if (isPlaying) {
            Action.Builder()
                .setTitle("Pause")
                .setOnClickListener {
                    mediaRepository.togglePlayPause()
                }
                .build()
        } else {
            Action.Builder()
                .setTitle("Play")
                .setOnClickListener {
                    mediaRepository.togglePlayPause()
                }
                .build()
        }

        val rewindAction = Action.Builder()
            .setTitle("Rewind")
            .setOnClickListener {
                mediaRepository.rewind()
            }
            .build()

        val forwardAction = Action.Builder()
            .setTitle("Forward")
            .setOnClickListener {
                mediaRepository.forward()
            }
            .build()

        val progressText = if (totalDuration > 0) {
            val progressPercent = ((currentPosition / totalDuration) * 100).toInt()
            "Progress: $progressPercent%"
        } else {
            "Playing: ${book.title}"
        }

        return MessageTemplate.Builder(progressText)
            .setTitle(book.title)
            .setHeaderAction(Action.BACK)
            .addAction(rewindAction)
            .addAction(playPauseAction)
            .addAction(forwardAction)
            .build()
    }
}
