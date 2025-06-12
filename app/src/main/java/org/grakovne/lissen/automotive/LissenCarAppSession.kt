package org.grakovne.lissen.automotive

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.AndroidEntryPoint
import org.grakovne.lissen.automotive.screen.LibraryScreen
import org.grakovne.lissen.content.LissenMediaProvider
import org.grakovne.lissen.persistence.preferences.LissenSharedPreferences
import org.grakovne.lissen.playback.MediaRepository
import javax.inject.Inject

@AndroidEntryPoint
class LissenCarAppSession : Session(), DefaultLifecycleObserver {

    @Inject
    lateinit var preferences: LissenSharedPreferences

    @Inject
    lateinit var mediaProvider: LissenMediaProvider

    @Inject
    lateinit var mediaRepository: MediaRepository

    override fun onCreateScreen(intent: Intent): Screen {
        lifecycle.addObserver(this)
        
        return when {
            !preferences.hasCredentials() -> {
                // If not logged in, show a message screen
                LibraryScreen(carContext, "Please configure Lissen on your phone first")
            }
            else -> {
                LibraryScreen(carContext)
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        // Initialize any necessary components for the car session
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        // Clean up resources
    }
}
