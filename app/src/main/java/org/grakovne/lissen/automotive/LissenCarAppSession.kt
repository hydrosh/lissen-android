package org.grakovne.lissen.automotive

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.EntryPointAccessors
import org.grakovne.lissen.automotive.screen.LibraryScreen
import org.grakovne.lissen.content.LissenMediaProvider
import org.grakovne.lissen.persistence.preferences.LissenSharedPreferences
import org.grakovne.lissen.playback.MediaRepository
import javax.inject.Inject

class LissenCarAppSession : Session(), DefaultLifecycleObserver {

    private lateinit var preferences: LissenSharedPreferences
    private lateinit var mediaProvider: LissenMediaProvider
    private lateinit var mediaRepository: MediaRepository

    override fun onCreateScreen(intent: Intent): Screen {
        lifecycle.addObserver(this)
        
        // Initialize dependencies manually since we can't use @AndroidEntryPoint
        val entryPoint = EntryPointAccessors.fromApplication(
            carContext,
            LissenCarEntryPoint::class.java
        )
        
        preferences = entryPoint.preferences()
        mediaProvider = entryPoint.mediaProvider()
        mediaRepository = entryPoint.mediaRepository()
        
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
