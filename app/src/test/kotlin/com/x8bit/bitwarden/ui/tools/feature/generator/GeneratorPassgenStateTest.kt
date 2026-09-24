package com.x8bit.bitwarden.ui.tools.feature.generator

import android.os.Parcel
import com.x8bit.bitwarden.data.tools.passgen.model.PassgenSettings
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GeneratorPassgenStateTest {

    @Test
    fun `settings round trip through state`() {
        val settings = PassgenSettings(
            version = 2,
            length = 20,
            useUppers = false,
            useNumbers = true,
            useSpecials = false,
            customSpecials = "-",
            avoidAmbiguous = true,
            customAmbiguous = "l",
            salt = "s",
        )
        assertEquals(settings, settings.toPassgenMainType().toSettings())
    }

    @Test
    fun `reduce applies each option action`() {
        val s = PassgenMainType()
        assertEquals(2, s.reduce(PassgenAction.VersionChange(2)).version)
        assertEquals(
            PassgenMainType(length = 30, isUserInteracting = true),
            s.reduce(PassgenAction.LengthChange(30, isUserInteracting = true)),
        )
        assertFalse(s.reduce(PassgenAction.ToggleUppers(false)).useUppers)
        assertFalse(s.reduce(PassgenAction.ToggleNumbers(false)).useNumbers)
        assertFalse(s.reduce(PassgenAction.ToggleSpecials(false)).useSpecials)
        assertEquals("-_", s.reduce(PassgenAction.CustomSpecialsChange("-_")).customSpecials)
        assertTrue(s.reduce(PassgenAction.ToggleAvoidAmbiguous(true)).avoidAmbiguous)
        assertEquals("l1", s.reduce(PassgenAction.CustomAmbiguousChange("l1")).customAmbiguous)
        assertEquals("salt", s.reduce(PassgenAction.SaltChange("salt")).salt)
        assertEquals("pw", s.reduce(PassgenAction.PassphraseChange("pw")).passphrase)
    }

    @Test
    fun `length is clamped to the slider range`() {
        val s = PassgenMainType()
        assertEquals(5, s.reduce(PassgenAction.LengthChange(1, false)).length)
        assertEquals(128, s.reduce(PassgenAction.LengthChange(500, false)).length)
    }

    @Test
    fun `clearing passphrase resets passphraseUsed`() {
        val s = PassgenMainType(passphrase = "typed", passphraseUsed = "typed")
        val cleared = s.reduce(PassgenAction.PassphraseChange(""))
        assertEquals("", cleared.passphraseUsed)
        assertEquals("", cleared.toPassgenOptions(forceNewRandom = false).passphrase)
    }

    @Test
    fun `toPassgenOptions reuses passphraseUsed unless forced`() {
        val s = PassgenMainType(passphrase = "", passphraseUsed = "abc123")
        assertEquals("abc123", s.toPassgenOptions(forceNewRandom = false).passphrase)
        assertEquals("", s.toPassgenOptions(forceNewRandom = true).passphrase)

        val typed = PassgenMainType(passphrase = "typed", passphraseUsed = "typed")
        assertEquals("typed", typed.toPassgenOptions(forceNewRandom = true).passphrase)
    }

    @Test
    fun `toPassgenOptions maps flags and drops custom ambiguous when not avoiding`() {
        val options = PassgenMainType(
            useUppers = false,
            useNumbers = false,
            useSpecials = false,
            customAmbiguous = "l",
            avoidAmbiguous = false,
        ).toPassgenOptions(forceNewRandom = false)
        assertTrue(options.noUppers && options.noNumbers && options.noSpecials)
        assertFalse(options.noAmbiguous)
        assertEquals("", options.customAmbiguous)
    }

    @Test
    fun `parceler never writes passphrase or passphraseUsed`() {
        val parcel = mockk<Parcel>(relaxed = true)
        with(PassgenMainType) {
            PassgenMainType(passphrase = "secret-typed", passphraseUsed = "secret-used", salt = "s")
                .write(parcel, 0)
        }
        verify(exactly = 0) { parcel.writeString("secret-typed") }
        verify(exactly = 0) { parcel.writeString("secret-used") }
        verify { parcel.writeString("s") }
    }
}
