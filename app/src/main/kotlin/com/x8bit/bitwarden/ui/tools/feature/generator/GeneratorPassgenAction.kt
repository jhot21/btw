@file:Suppress("MatchingDeclarationName") // PASSGEN: file name follows Generator*.kt convention

package com.x8bit.bitwarden.ui.tools.feature.generator

/**
 * Actions for the fork-only passgen generator type. See FORK.md.
 */
sealed class PassgenAction : GeneratorAction.MainType() {
    /** User selected algorithm [version] (2 or 3). */
    data class VersionChange(val version: Int) : PassgenAction()

    /** User moved the length slider. */
    data class LengthChange(val length: Int, val isUserInteracting: Boolean) : PassgenAction()

    /** User toggled uppercase letters. */
    data class ToggleUppers(val isEnabled: Boolean) : PassgenAction()

    /** User toggled numbers. */
    data class ToggleNumbers(val isEnabled: Boolean) : PassgenAction()

    /** User toggled special characters. */
    data class ToggleSpecials(val isEnabled: Boolean) : PassgenAction()

    /** User edited the custom special characters. */
    data class CustomSpecialsChange(val value: String) : PassgenAction()

    /** User toggled ambiguous-character avoidance. */
    data class ToggleAvoidAmbiguous(val isEnabled: Boolean) : PassgenAction()

    /** User edited the custom ambiguous characters. */
    data class CustomAmbiguousChange(val value: String) : PassgenAction()

    /** User edited the salt. */
    data class SaltChange(val value: String) : PassgenAction()

    /** User edited the passphrase. */
    data class PassphraseChange(val value: String) : PassgenAction() {
        override fun toString(): String = "PassphraseChange(<redacted>)"
    }

    /** User tapped copy on the random passphrase used. */
    data object CopyPassphraseUsedClick : PassgenAction()

    /** User tapped copy on the salt. */
    data object CopySaltClick : PassgenAction()
}
