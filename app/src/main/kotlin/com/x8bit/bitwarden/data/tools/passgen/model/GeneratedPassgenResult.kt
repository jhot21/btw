package com.x8bit.bitwarden.data.tools.passgen.model

/**
 * A generated passgen password and the passphrase that produced it.
 */
data class GeneratedPassgenResult(
    val password: String,
    val passphraseUsed: String,
) {
    override fun toString(): String = "GeneratedPassgenResult(<redacted>)"
}
