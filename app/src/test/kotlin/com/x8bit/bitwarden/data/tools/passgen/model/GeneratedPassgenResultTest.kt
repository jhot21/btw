package com.x8bit.bitwarden.data.tools.passgen.model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class GeneratedPassgenResultTest {

    @Test
    fun `toString redacts the password and passphrase`() {
        val result = GeneratedPassgenResult(
            password = "pw-secret",
            passphraseUsed = "pp-secret",
        )

        val stringified = result.toString()

        assertFalse(stringified.contains("pw-secret"))
        assertFalse(stringified.contains("pp-secret"))
    }
}
