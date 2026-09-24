package com.x8bit.bitwarden.data.tools.passgen.datasource.disk

import android.content.SharedPreferences
import com.bitwarden.core.data.util.decodeFromStringOrNull
import com.bitwarden.data.datasource.disk.BaseDiskSource
import com.x8bit.bitwarden.data.tools.passgen.model.PassgenSettings
import kotlinx.serialization.json.Json

private const val PASSGEN_SETTINGS_KEY = "passgenSettings"

/**
 * Primary implementation of [PassgenSettingsDiskSource].
 */
class PassgenSettingsDiskSourceImpl(
    sharedPreferences: SharedPreferences,
    private val json: Json,
) : BaseDiskSource(sharedPreferences),
    PassgenSettingsDiskSource {

    override fun getSettings(): PassgenSettings? =
        getString(PASSGEN_SETTINGS_KEY)?.let { json.decodeFromStringOrNull(it) }

    override fun storeSettings(settings: PassgenSettings?) {
        putString(PASSGEN_SETTINGS_KEY, settings?.let { json.encodeToString(it) })
    }
}
