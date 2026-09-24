package com.x8bit.bitwarden.data.tools.passgen.datasource.disk

import com.x8bit.bitwarden.data.tools.passgen.model.PassgenSettings

/**
 * Persists device-wide passgen settings (not per user, so the salt survives logout).
 */
interface PassgenSettingsDiskSource {
    /** Returns the stored settings, or null if none were stored. */
    fun getSettings(): PassgenSettings?

    /** Stores [settings]; null clears them. */
    fun storeSettings(settings: PassgenSettings?)
}
