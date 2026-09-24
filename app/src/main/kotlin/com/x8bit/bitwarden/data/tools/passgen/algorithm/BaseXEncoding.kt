@file:Suppress("MagicNumber")

package com.x8bit.bitwarden.data.tools.passgen.algorithm

/**
 * Port of github.com/eknkc/basex `Encode`, which passgen v2 uses. Alphabets are
 * ASCII-only (enforced by [PassgenAlgorithm]), so chars and runes coincide.
 */
internal class BaseXEncoding(alphabet: String) {
    private val chars = alphabet.toCharArray()
    private val base = chars.size

    init {
        if (chars.toSet().size != chars.size) throw PassgenException("Ambiguous alphabet.")
        if (base < 2) throw PassgenException("character set must contain at least 2 characters")
    }

    fun encode(source: ByteArray): String {
        if (source.isEmpty()) return ""
        val digits = mutableListOf(0)
        for (byte in source) {
            var carry = byte.toInt() and 0xFF
            for (j in digits.indices) {
                carry += digits[j] shl 8
                digits[j] = carry % base
                carry /= base
            }
            while (carry > 0) {
                digits.add(carry % base)
                carry /= base
            }
        }
        val result = StringBuilder()
        var k = 0
        while (source[k] == 0.toByte() && k < source.size - 1) {
            result.append(chars[0])
            k++
        }
        for (q in digits.indices.reversed()) result.append(chars[digits[q]])
        return result.toString()
    }
}
