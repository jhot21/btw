package com.x8bit.bitwarden.ui.platform.util

import android.content.Intent

/**
 * Returns `true` if the [intent] is a deeplink to the Passgen generator, `false` otherwise.
 */
fun isPassgenShortcut(intent: Intent): Boolean = intent.dataString == "bitwarden://passgen"
