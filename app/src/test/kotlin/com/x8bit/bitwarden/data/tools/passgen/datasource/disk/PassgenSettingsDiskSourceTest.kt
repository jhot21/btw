package com.x8bit.bitwarden.data.tools.passgen.datasource.disk

import com.bitwarden.data.datasource.disk.base.FakeSharedPreferences
import com.x8bit.bitwarden.data.tools.passgen.model.PassgenSettings
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class PassgenSettingsDiskSourceTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val diskSource = PassgenSettingsDiskSourceImpl(
        sharedPreferences = FakeSharedPreferences(),
        json = json,
    )

    @Test
    fun `getSettings returns null when nothing stored`() {
        assertNull(diskSource.getSettings())
    }

    @Test
    fun `storeSettings round trips and null clears`() {
        val settings = PassgenSettings(
            version = 2,
            length = 20,
            useUppers = false,
            customSpecials = "-_",
            avoidAmbiguous = true,
            customAmbiguous = "l1",
            salt = "my salt",
        )
        diskSource.storeSettings(settings)
        assertEquals(settings, diskSource.getSettings())

        diskSource.storeSettings(null)
        assertNull(diskSource.getSettings())
    }
}
