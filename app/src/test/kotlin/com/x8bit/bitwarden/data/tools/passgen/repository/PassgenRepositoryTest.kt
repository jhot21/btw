package com.x8bit.bitwarden.data.tools.passgen.repository

import com.bitwarden.core.data.manager.dispatcher.FakeDispatcherManager
import com.bitwarden.vault.PasswordHistoryView
import com.x8bit.bitwarden.data.tools.generator.repository.GeneratorRepository
import com.x8bit.bitwarden.data.tools.passgen.algorithm.PassgenAlgorithm
import com.x8bit.bitwarden.data.tools.passgen.algorithm.PassgenException
import com.x8bit.bitwarden.data.tools.passgen.datasource.disk.PassgenSettingsDiskSource
import com.x8bit.bitwarden.data.tools.passgen.model.PassgenOptions
import com.x8bit.bitwarden.data.tools.passgen.model.PassgenSettings
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class PassgenRepositoryTest {
    private val clock = Clock.fixed(Instant.parse("2026-09-23T00:00:00Z"), ZoneOffset.UTC)
    private val diskSource: PassgenSettingsDiskSource = mockk {
        every { getSettings() } returns null
        every { storeSettings(any()) } just runs
    }
    private val generatorRepository: GeneratorRepository = mockk {
        coEvery { storePasswordHistory(any()) } just runs
    }
    private val repository = PassgenRepositoryImpl(
        diskSource = diskSource,
        generatorRepository = generatorRepository,
        clock = clock,
        dispatcherManager = FakeDispatcherManager(),
    )

    @Test
    fun `getSettings returns defaults when nothing stored`() {
        assertEquals(PassgenSettings(), repository.getSettings())
    }

    @Test
    fun `saveSettings writes through to disk`() {
        val settings = PassgenSettings(salt = "s")
        repository.saveSettings(settings)
        verify { diskSource.storeSettings(settings) }
    }

    @Test
    fun `generate with typed passphrase matches the algorithm and echoes the passphrase`() =
        runTest {
            val options = PassgenOptions(passphrase = "hunter2", salt = "salt", length = 14)
            val result = repository.generate(options).getOrThrow()
            assertEquals(PassgenAlgorithm.generate(options), result.password)
            assertEquals("hunter2", result.passphraseUsed)
        }

    @Test
    fun `generate with empty passphrase uses random hex passphrase reproducing the password`() =
        runTest {
            val options = PassgenOptions(passphrase = "", length = 20)
            val first = repository.generate(options).getOrThrow()
            val second = repository.generate(options).getOrThrow()

            assertTrue(first.passphraseUsed.matches(Regex("[0-9a-f]{32}")))
            assertNotEquals(first.passphraseUsed, second.passphraseUsed)
            assertEquals(
                first.password,
                PassgenAlgorithm.generate(options.copy(passphrase = first.passphraseUsed)),
            )
        }

    @Test
    fun `generate returns failure for invalid options`() = runTest {
        val result = repository.generate(PassgenOptions(passphrase = "p", length = 0))
        assertTrue(result.exceptionOrNull() is PassgenException)
    }

    @Test
    fun `recordCopied stores history once per distinct password`() {
        repository.recordCopied("pw1")
        repository.recordCopied("pw1")
        repository.recordCopied("pw2")

        coVerify(exactly = 1) {
            generatorRepository.storePasswordHistory(
                PasswordHistoryView(password = "pw1", lastUsedDate = clock.instant()),
            )
        }
        coVerify(exactly = 1) {
            generatorRepository.storePasswordHistory(
                PasswordHistoryView(password = "pw2", lastUsedDate = clock.instant()),
            )
        }
    }

    @Test
    fun `consumePassgenTabRequest returns false when nothing requested`() {
        assertFalse(repository.consumePassgenTabRequest())
    }

    @Test
    fun `consumePassgenTabRequest returns true after a request`() {
        repository.requestPassgenTab()
        assertTrue(repository.consumePassgenTabRequest())
    }

    @Test
    fun `consumePassgenTabRequest returns false on a second consume`() {
        repository.requestPassgenTab()
        repository.consumePassgenTabRequest()
        assertFalse(repository.consumePassgenTabRequest())
    }

    @Test
    fun `repeated requests are consumed once`() {
        repository.requestPassgenTab()
        repository.requestPassgenTab()
        assertTrue(repository.consumePassgenTabRequest())
        assertFalse(repository.consumePassgenTabRequest())
    }
}
