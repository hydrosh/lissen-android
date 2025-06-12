package org.grakovne.lissen.automotive

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.grakovne.lissen.content.LissenMediaProvider
import org.grakovne.lissen.persistence.preferences.LissenSharedPreferences
import org.grakovne.lissen.playback.MediaRepository

@EntryPoint
@InstallIn(SingletonComponent::class)
interface LissenCarEntryPoint {
    fun preferences(): LissenSharedPreferences
    fun mediaProvider(): LissenMediaProvider
    fun mediaRepository(): MediaRepository
}
