package com.x8bit.bitwarden.data.tools.passgen.algorithm

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test

class ChaCha20StreamTest {

    @Test
    fun `block matches RFC 8439 section 2_3_2 test vector`() {
        val key = ByteArray(32) { it.toByte() }
        val nonce = hex("000000090000004a00000000")
        val stream = ChaCha20Stream(key = key, nonce = nonce, initialCounter = 1)
        val block = ByteArray(64) { stream.nextByte().toByte() }
        assertArrayEquals(
            hex(
                "10f1e7e4d13b5915500fdd1fa32071c4c7d1f4c733c068030422aa9ac3d46c4e" +
                    "d2826446079faa0914c2d705d98b02a2b5129cd1de164eb9cbd083e8a2503c4e",
            ),
            block,
        )
    }

    @Test
    fun `byte-wise keystream from counter 0 matches Go across block boundaries`() {
        // Expected bytes produced by Go: chacha20.NewUnauthenticatedCipher(key, zero nonce),
        // XORKeyStream one zero byte at a time — exactly how passgen v3 consumes it.
        val key = ByteArray(32) { (it * 7 + 3).toByte() }
        val stream = ChaCha20Stream(key = key, nonce = ByteArray(12))
        val ours = ByteArray(200) { stream.nextByte().toByte() }
        assertArrayEquals(
            hex(
                "7a54c09c1d27b11a193beb07387ac35bac93ff795f093b4522afa1c6da6bd62b" +
                    "98b08b2d1b31fbc85a16b8afa5ce913dc801eb28b071c4cfffd4bcd95ea3ac0c" +
                    "dc77fd6a5b0079008ab0f8192c48c10223255c95cff5ee8d022e2030413996fa" +
                    "81c4afa0f38958e4de87b9fb1afb32966d95ea83db797563ea5aa234df96bb10" +
                    "fee8c99600a0184a8caaa7d1a15c195ac1b188bc1c3a4e088827dcab643bf450" +
                    "04ff3b70742549de63dae37cecfd5c7767f78c31c04327f635fa0b25ebe43e68" +
                    "95482431deed874a",
            ),
            ours,
        )
    }

    private fun hex(s: String): ByteArray =
        ByteArray(s.length / 2) { s.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}
