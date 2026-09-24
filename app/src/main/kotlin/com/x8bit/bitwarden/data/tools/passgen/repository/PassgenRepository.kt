package com.x8bit.bitwarden.data.tools.passgen.repository

import com.x8bit.bitwarden.data.tools.passgen.model.GeneratedPassgenResult
import com.x8bit.bitwarden.data.tools.passgen.model.PassgenOptions
import com.x8bit.bitwarden.data.tools.passgen.model.PassgenSettings

/**
 * Generates deterministic passgen passwords and manages their settings.
 */
interface PassgenRepository {
    /** Returns the stored settings, or defaults. */
    fun getSettings(): PassgenSettings

    /** Persists [settings]. */
    fun saveSettings(settings: PassgenSettings)

    /**
     * Generates a password. An empty passphrase is replaced by a new random one,
     * returned in [GeneratedPassgenResult.passphraseUsed].
     */
    suspend fun generate(options: PassgenOptions): Result<GeneratedPassgenResult>

    /** Records [password] in password history unless it was the last one recorded. */
    fun recordCopied(password: String)
}
