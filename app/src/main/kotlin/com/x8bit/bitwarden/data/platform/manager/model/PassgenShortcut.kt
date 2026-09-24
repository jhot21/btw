package com.x8bit.bitwarden.data.platform.manager.model

import kotlinx.parcelize.Parcelize

/**
 * The app was launched via deeplink to the fork-only Passgen generator. See FORK.md.
 */
@Parcelize
data object PassgenShortcut : SpecialCircumstance()
