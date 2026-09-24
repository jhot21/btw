@file:Suppress("MatchingDeclarationName") // PASSGEN: file name follows Generator*.kt convention

package com.x8bit.bitwarden.ui.tools.feature.generator

import android.os.Parcel
import com.x8bit.bitwarden.data.tools.passgen.model.PassgenOptions
import com.x8bit.bitwarden.data.tools.passgen.model.PassgenSettings
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

/**
 * State for the fork-only passgen generator type. See FORK.md.
 *
 * [passphrase] and [passphraseUsed] are secrets: the custom [Parceler] never
 * writes them, so they are never in saved instance state.
 */
@Parcelize
data class PassgenMainType(
    val version: Int = 3,
    val length: Int = 40,
    val useUppers: Boolean = true,
    val useNumbers: Boolean = true,
    val useSpecials: Boolean = true,
    val customSpecials: String = "",
    val avoidAmbiguous: Boolean = false,
    val customAmbiguous: String = "",
    val salt: String = "",
    val passphrase: String = "",
    val passphraseUsed: String = "",
    val errorMessage: String? = null,
    val isUserInteracting: Boolean = false,
) : GeneratorState.MainType() {
    override val mainTypeOption: GeneratorState.MainTypeOption
        get() = GeneratorState.MainTypeOption.PASSGEN

    override fun toString(): String = "PassgenMainType(version=$version, length=$length)"

    @Suppress("UndocumentedPublicClass")
    companion object : Parceler<PassgenMainType> {
        const val LENGTH_MIN: Int = 5
        const val LENGTH_MAX: Int = 128

        override fun create(parcel: Parcel): PassgenMainType = PassgenMainType(
            version = parcel.readInt(),
            length = parcel.readInt(),
            useUppers = parcel.readInt() == 1,
            useNumbers = parcel.readInt() == 1,
            useSpecials = parcel.readInt() == 1,
            customSpecials = parcel.readString().orEmpty(),
            avoidAmbiguous = parcel.readInt() == 1,
            customAmbiguous = parcel.readString().orEmpty(),
            salt = parcel.readString().orEmpty(),
            errorMessage = parcel.readString(),
        )

        override fun PassgenMainType.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(version)
            parcel.writeInt(length)
            parcel.writeInt(if (useUppers) 1 else 0)
            parcel.writeInt(if (useNumbers) 1 else 0)
            parcel.writeInt(if (useSpecials) 1 else 0)
            parcel.writeString(customSpecials)
            parcel.writeInt(if (avoidAmbiguous) 1 else 0)
            parcel.writeString(customAmbiguous)
            parcel.writeString(salt)
            parcel.writeString(errorMessage)
            // passphrase and passphraseUsed are intentionally not written.
        }
    }
}

/**
 * Applies an option-changing [action]. Copy actions are handled by the ViewModel
 * and leave the state unchanged here.
 */
fun PassgenMainType.reduce(action: PassgenAction): PassgenMainType {
    val base = copy(isUserInteracting = false)
    return when (action) {
        is PassgenAction.VersionChange -> base.copy(version = action.version)
        is PassgenAction.LengthChange -> base.copy(
            length = action.length.coerceIn(PassgenMainType.LENGTH_MIN, PassgenMainType.LENGTH_MAX),
            isUserInteracting = action.isUserInteracting,
        )

        is PassgenAction.ToggleUppers -> base.copy(useUppers = action.isEnabled)
        is PassgenAction.ToggleNumbers -> base.copy(useNumbers = action.isEnabled)
        is PassgenAction.ToggleSpecials -> base.copy(useSpecials = action.isEnabled)
        is PassgenAction.CustomSpecialsChange -> base.copy(customSpecials = action.value)
        is PassgenAction.ToggleAvoidAmbiguous -> base.copy(avoidAmbiguous = action.isEnabled)
        is PassgenAction.CustomAmbiguousChange -> base.copy(customAmbiguous = action.value)
        is PassgenAction.SaltChange -> base.copy(salt = action.value)
        is PassgenAction.PassphraseChange -> base.copy(
            passphrase = action.value,
            // Clearing the field must switch to a fresh random passphrase, not reuse the typed one.
            passphraseUsed = if (action.value.isEmpty()) "" else base.passphraseUsed,
        )

        PassgenAction.CopyPassphraseUsedClick,
        PassgenAction.CopySaltClick,
            -> this
    }
}

/**
 * Builds algorithm options. With an empty typed passphrase, the previous random
 * passphrase is reused so option changes don't reseed; [forceNewRandom] (Regenerate)
 * requests a new one.
 */
fun PassgenMainType.toPassgenOptions(forceNewRandom: Boolean): PassgenOptions = PassgenOptions(
    version = version,
    passphrase = passphrase.ifEmpty { if (forceNewRandom) "" else passphraseUsed },
    salt = salt,
    length = length,
    noUppers = !useUppers,
    noNumbers = !useNumbers,
    noSpecials = !useSpecials,
    customSpecials = customSpecials,
    noAmbiguous = avoidAmbiguous,
    customAmbiguous = if (avoidAmbiguous) customAmbiguous else "",
)

/** Non-secret settings to persist. */
fun PassgenMainType.toSettings(): PassgenSettings = PassgenSettings(
    version = version,
    length = length,
    useUppers = useUppers,
    useNumbers = useNumbers,
    useSpecials = useSpecials,
    customSpecials = customSpecials,
    avoidAmbiguous = avoidAmbiguous,
    customAmbiguous = customAmbiguous,
    salt = salt,
)

/** Restores state from persisted settings (no passphrase). */
fun PassgenSettings.toPassgenMainType(): PassgenMainType = PassgenMainType(
    version = version,
    length = length.coerceIn(PassgenMainType.LENGTH_MIN, PassgenMainType.LENGTH_MAX),
    useUppers = useUppers,
    useNumbers = useNumbers,
    useSpecials = useSpecials,
    customSpecials = customSpecials,
    avoidAmbiguous = avoidAmbiguous,
    customAmbiguous = customAmbiguous,
    salt = salt,
)
