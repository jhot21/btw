package com.x8bit.bitwarden.ui.platform.util

import android.content.Intent
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PassgenShortcutUtilsTest {
    @Test
    fun `isPassgenShortcut should return true when dataString is passgen deeplink`() {
        assertTrue(isPassgenShortcut(intentWith("bitwarden://passgen")))
    }

    @Test
    fun `isPassgenShortcut should return false for the password generator deeplink`() {
        assertFalse(isPassgenShortcut(intentWith("bitwarden://password_generator")))
    }

    @Test
    fun `isPassgenShortcut should return false for the my vault deeplink`() {
        assertFalse(isPassgenShortcut(intentWith("bitwarden://my_vault")))
    }

    @Test
    fun `isPassgenShortcut should return false when dataString is null`() {
        assertFalse(isPassgenShortcut(intentWith(null)))
    }

    @Test
    fun `isPassgenShortcut should return false for lookalike deeplinks`() {
        listOf(
            "bitwarden://passgen/extra",
            "bitwarden://passgen?x=1",
            "bitwarden://passgen2",
            "bitwarden://PASSGEN",
        ).forEach { assertFalse(isPassgenShortcut(intentWith(it)), it) }
    }

    private fun intentWith(data: String?): Intent = mockk {
        every { dataString } returns data
    }
}
