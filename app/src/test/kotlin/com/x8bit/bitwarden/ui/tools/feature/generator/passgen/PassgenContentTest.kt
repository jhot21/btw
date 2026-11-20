package com.x8bit.bitwarden.ui.tools.feature.generator.passgen

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.x8bit.bitwarden.ui.platform.base.BitwardenComposeTest
import com.x8bit.bitwarden.ui.tools.feature.generator.PassgenMainType
import org.junit.Test

// PASSGEN: fork-only tests
class PassgenContentTest : BitwardenComposeTest() {

    private val lifecycleOwner = object : LifecycleOwner {
        override val lifecycle = LifecycleRegistry.createUnsafe(this)
    }

    @Test
    fun `salt is masked again when the app is paused`() {
        lifecycleOwner.lifecycle.currentState = Lifecycle.State.RESUMED
        setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                PassgenContent(state = PassgenMainType(salt = "salt"), onAction = {})
            }
        }

        composeTestRule
            .onNodeWithTag("PassgenSaltShowButton")
            .performClick()
            .assertContentDescriptionEquals("Hide")

        lifecycleOwner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag("PassgenSaltShowButton")
            .assertContentDescriptionEquals("Show")
    }
}
