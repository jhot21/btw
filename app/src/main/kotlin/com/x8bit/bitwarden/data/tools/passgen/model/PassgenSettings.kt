package com.x8bit.bitwarden.data.tools.passgen.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Non-secret passgen settings persisted on this device. The passphrase is never stored.
 */
@Serializable
data class PassgenSettings(
    @SerialName("version") val version: Int = 3,
    @SerialName("length") val length: Int = 40,
    @SerialName("useUppers") val useUppers: Boolean = true,
    @SerialName("useNumbers") val useNumbers: Boolean = true,
    @SerialName("useSpecials") val useSpecials: Boolean = true,
    @SerialName("customSpecials") val customSpecials: String = "",
    @SerialName("avoidAmbiguous") val avoidAmbiguous: Boolean = false,
    @SerialName("customAmbiguous") val customAmbiguous: String = "",
    @SerialName("salt") val salt: String = "",
)
