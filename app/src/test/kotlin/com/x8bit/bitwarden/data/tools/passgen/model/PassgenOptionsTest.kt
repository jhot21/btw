package com.x8bit.bitwarden.data.tools.passgen.model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class PassgenOptionsTest {

    @Test
    fun `toString redacts the passphrase`() {
        val options = PassgenOptions(passphrase = "hunter2-secret")

        val stringified = options.toString()

        assertFalse(stringified.contains("hunter2-secret"))
    }
}
