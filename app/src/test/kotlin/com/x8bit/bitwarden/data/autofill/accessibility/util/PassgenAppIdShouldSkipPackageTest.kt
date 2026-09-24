package com.x8bit.bitwarden.data.autofill.accessibility.util

import android.view.accessibility.AccessibilityNodeInfo
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Fork-owned: accessibility autofill must keep skipping the fork's own application IDs.
 */
class PassgenAppIdShouldSkipPackageTest {

    private fun nodeFor(name: String): AccessibilityNodeInfo =
        mockk<AccessibilityNodeInfo> {
            every { packageName } returns name
        }

    @Test
    fun `shouldSkipPackage for the fork release id should return true`() {
        assertTrue(nodeFor("me.jhot.btw").shouldSkipPackage)
    }

    @Test
    fun `shouldSkipPackage for the fork dev id should return true`() {
        assertTrue(nodeFor("me.jhot.btw.dev").shouldSkipPackage)
    }

    @Test
    fun `shouldSkipPackage for the fork beta id should return true`() {
        assertTrue(nodeFor("me.jhot.btw.beta").shouldSkipPackage)
    }

    @Test
    fun `shouldSkipPackage for an unrelated package should return false`() {
        assertFalse(nodeFor("com.example.app").shouldSkipPackage)
    }

    @Test
    fun `shouldSkipPackage for the upstream dev id should still return true`() {
        assertTrue(nodeFor("com.x8bit.bitwarden.dev").shouldSkipPackage)
    }
}
