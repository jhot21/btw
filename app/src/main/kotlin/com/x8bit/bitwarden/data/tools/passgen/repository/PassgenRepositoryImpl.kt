package com.x8bit.bitwarden.data.tools.passgen.repository

import com.bitwarden.core.data.manager.dispatcher.DispatcherManager
import com.bitwarden.vault.PasswordHistoryView
import com.x8bit.bitwarden.data.tools.generator.repository.GeneratorRepository
import com.x8bit.bitwarden.data.tools.passgen.algorithm.PassgenAlgorithm
import com.x8bit.bitwarden.data.tools.passgen.datasource.disk.PassgenSettingsDiskSource
import com.x8bit.bitwarden.data.tools.passgen.model.GeneratedPassgenResult
import com.x8bit.bitwarden.data.tools.passgen.model.PassgenOptions
import com.x8bit.bitwarden.data.tools.passgen.model.PassgenSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import java.time.Clock

private const val RANDOM_PASSPHRASE_BYTES = 16

/**
 * Default implementation of [PassgenRepository].
 */
class PassgenRepositoryImpl(
    private val diskSource: PassgenSettingsDiskSource,
    private val generatorRepository: GeneratorRepository,
    private val clock: Clock,
    private val dispatcherManager: DispatcherManager,
) : PassgenRepository {
    private val scope = CoroutineScope(dispatcherManager.io)
    private val secureRandom = SecureRandom()
    private var lastRecordedPassword: String? = null

    override fun getSettings(): PassgenSettings = diskSource.getSettings() ?: PassgenSettings()

    override fun saveSettings(settings: PassgenSettings) {
        diskSource.storeSettings(settings)
    }

    override suspend fun generate(options: PassgenOptions): Result<GeneratedPassgenResult> =
        withContext(dispatcherManager.default) {
            val passphrase = options.passphrase.ifEmpty { newRandomPassphrase() }
            runCatching {
                GeneratedPassgenResult(
                    password = PassgenAlgorithm.generate(options.copy(passphrase = passphrase)),
                    passphraseUsed = passphrase,
                )
            }
        }

    override fun recordCopied(password: String) {
        if (password == lastRecordedPassword) return
        lastRecordedPassword = password
        val view = PasswordHistoryView(password = password, lastUsedDate = clock.instant())
        scope.launch { generatorRepository.storePasswordHistory(view) }
    }

    private fun newRandomPassphrase(): String {
        val bytes = ByteArray(RANDOM_PASSPHRASE_BYTES).also(secureRandom::nextBytes)
        return bytes.joinToString(separator = "") { "%02x".format(it) }
    }
}
