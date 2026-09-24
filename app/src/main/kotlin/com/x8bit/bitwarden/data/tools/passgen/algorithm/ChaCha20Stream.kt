package com.x8bit.bitwarden.data.tools.passgen.algorithm

/**
 * A ChaCha20 (RFC 8439) keystream that yields one byte at a time.
 *
 * Pure Kotlin so the output is identical on the JVM and on device; it must match
 * Go's `chacha20.NewUnauthenticatedCipher` byte for byte.
 */
internal class ChaCha20Stream(
    key: ByteArray,
    nonce: ByteArray,
    initialCounter: Int = 0,
) {
    private val state = IntArray(16)
    private val working = IntArray(16)
    private val block = ByteArray(64)
    private var position = 64

    init {
        require(key.size == 32) { "key must be 32 bytes" }
        require(nonce.size == 12) { "nonce must be 12 bytes" }
        state[0] = 0x61707865
        state[1] = 0x3320646e
        state[2] = 0x79622d32
        state[3] = 0x6b206574
        for (i in 0 until 8) state[4 + i] = key.readIntLe(i * 4)
        state[12] = initialCounter
        for (i in 0 until 3) state[13 + i] = nonce.readIntLe(i * 4)
    }

    /** Returns the next keystream byte as an Int in 0..255. */
    fun nextByte(): Int {
        if (position == 64) refill()
        return block[position++].toInt() and 0xFF
    }

    private fun refill() {
        state.copyInto(working)
        repeat(10) {
            quarterRound(0, 4, 8, 12)
            quarterRound(1, 5, 9, 13)
            quarterRound(2, 6, 10, 14)
            quarterRound(3, 7, 11, 15)
            quarterRound(0, 5, 10, 15)
            quarterRound(1, 6, 11, 12)
            quarterRound(2, 7, 8, 13)
            quarterRound(3, 4, 9, 14)
        }
        for (i in 0 until 16) {
            val v = working[i] + state[i]
            block[i * 4] = v.toByte()
            block[i * 4 + 1] = (v ushr 8).toByte()
            block[i * 4 + 2] = (v ushr 16).toByte()
            block[i * 4 + 3] = (v ushr 24).toByte()
        }
        state[12]++
        position = 0
    }

    private fun quarterRound(a: Int, b: Int, c: Int, d: Int) {
        val x = working
        x[a] += x[b]
        x[d] = (x[d] xor x[a]).rotateLeft(16)
        x[c] += x[d]
        x[b] = (x[b] xor x[c]).rotateLeft(12)
        x[a] += x[b]
        x[d] = (x[d] xor x[a]).rotateLeft(8)
        x[c] += x[d]
        x[b] = (x[b] xor x[c]).rotateLeft(7)
    }

    private fun ByteArray.readIntLe(offset: Int): Int =
        (this[offset].toInt() and 0xFF) or
            ((this[offset + 1].toInt() and 0xFF) shl 8) or
            ((this[offset + 2].toInt() and 0xFF) shl 16) or
            ((this[offset + 3].toInt() and 0xFF) shl 24)
}
