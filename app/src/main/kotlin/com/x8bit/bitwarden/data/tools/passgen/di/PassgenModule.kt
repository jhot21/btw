package com.x8bit.bitwarden.data.tools.passgen.di

import android.content.SharedPreferences
import com.bitwarden.core.data.manager.dispatcher.DispatcherManager
import com.bitwarden.data.datasource.disk.di.UnencryptedPreferences
import com.x8bit.bitwarden.data.tools.generator.repository.GeneratorRepository
import com.x8bit.bitwarden.data.tools.passgen.datasource.disk.PassgenSettingsDiskSource
import com.x8bit.bitwarden.data.tools.passgen.datasource.disk.PassgenSettingsDiskSourceImpl
import com.x8bit.bitwarden.data.tools.passgen.repository.PassgenRepository
import com.x8bit.bitwarden.data.tools.passgen.repository.PassgenRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import java.time.Clock
import javax.inject.Singleton

/**
 * Provides passgen dependencies. Fork-only module; see FORK.md.
 */
@Module
@InstallIn(SingletonComponent::class)
object PassgenModule {

    @Provides
    @Singleton
    fun providePassgenSettingsDiskSource(
        @UnencryptedPreferences sharedPreferences: SharedPreferences,
        json: Json,
    ): PassgenSettingsDiskSource = PassgenSettingsDiskSourceImpl(
        sharedPreferences = sharedPreferences,
        json = json,
    )

    @Provides
    @Singleton
    fun providePassgenRepository(
        diskSource: PassgenSettingsDiskSource,
        generatorRepository: GeneratorRepository,
        clock: Clock,
        dispatcherManager: DispatcherManager,
    ): PassgenRepository = PassgenRepositoryImpl(
        diskSource = diskSource,
        generatorRepository = generatorRepository,
        clock = clock,
        dispatcherManager = dispatcherManager,
    )
}
