@file:Suppress("MagicNumber")

package com.x8bit.bitwarden.data.tools.passgen.algorithm

import com.x8bit.bitwarden.data.tools.passgen.model.PassgenOptions
import java.security.MessageDigest

private const val UPPERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
private const val LOWERS = "abcdefghijklmnopqrstuvwxyz"
private const val NUMBERS = "0123456789"
private const val MAX_CLASS_SIZE = 256

/**
 * Kotlin port of passgen v2 and v3 (github.com/jhot21/passgen). Output must match
 * the Go library byte for byte; see PassgenAlgorithmTest golden vectors.
 *
 * Inputs Go would hang or panic on raise [PassgenException] instead (the Go library
 * has the same guards).
 */
@Suppress("TooManyFunctions")
object PassgenAlgorithm {
    const val DEFAULT_SPECIALS: String = "!@#$%^&*"
    const val DEFAULT_AMBIGUOUS: String = "Il1O05S"

    /** Generates a password, or throws [PassgenException] if [options] are invalid. */
    @Suppress("ThrowsCount")
    fun generate(options: PassgenOptions): String {
        if (options.passphrase.isEmpty()) throw PassgenException("passphrase must not be empty")
        if (options.length < 1) throw PassgenException("length must be at least 1")
        if (!options.customSpecials.isPrintableAscii() ||
            !options.customAmbiguous.isPrintableAscii()
        ) {
            throw PassgenException("custom character sets must be printable ASCII")
        }
        return when (options.version) {
            2 -> generateV2(options)
            3 -> generateV3(options)
            else -> throw PassgenException("unsupported version ${options.version} (valid: 2, 3)")
        }
    }

    // region v2

    private fun generateV2(o: PassgenOptions): String {
        val charset = createCharset(o)
        val encoding = BaseXEncoding(charset)
        val required = requiredClassesV2(o)
        if (required.any { cls -> cls.none { it in charset } }) {
            throw PassgenException("a required character class is empty after ambiguous filtering")
        }
        if (o.length < required.size) {
            throw PassgenException(
                "length ${o.length} is too short for ${required.size} required character classes",
            )
        }

        var out = ""
        var i = 0
        while (true) {
            if (out.length >= o.length) {
                if (containsRequired(out.substring(0, o.length), required)) break
                out = out.substring(out.length / 2)
            }
            val hash = sha256(o.passphrase.repeat(i + 1) + o.salt)
            out += encoding.encode(hash)
            i++
        }
        return out.substring(0, o.length)
    }

    private fun createCharset(o: PassgenOptions): String {
        var charset = ""
        if (!o.noUppers) charset += UPPERS
        charset += LOWERS
        if (!o.noNumbers) charset += NUMBERS
        if (!o.noSpecials) charset += o.specials()
        return if (o.noAmbiguous) charset.filterAmbiguous(o.ambiguous()) else charset
    }

    private fun requiredClassesV2(o: PassgenOptions): List<String> = buildList {
        if (!o.noNumbers) add(NUMBERS)
        if (!o.noUppers) add(UPPERS)
        if (!o.noSpecials) add(o.specials())
    }

    private fun containsRequired(s: String, required: List<String>): Boolean =
        required.all { cls -> s.any { it in cls } }

    // endregion v2

    // region v3

    private fun generateV3(o: PassgenOptions): String {
        val stream = ChaCha20Stream(key = sha256(o.passphrase + o.salt), nonce = ByteArray(12))
        val classes = buildEnabledClassesV3(o)
        if (classes.isEmpty()) {
            throw PassgenException("all character classes are empty after filtering")
        }
        if (classes.any { it.length > MAX_CLASS_SIZE }) {
            throw PassgenException("character class longer than $MAX_CLASS_SIZE characters")
        }

        val result = StringBuilder(o.length)
        val cycle = classes.toMutableList()
        while (result.length < o.length) {
            classes.forEachIndexed { index, cls -> cycle[index] = cls }
            for (k in cycle.size - 1 downTo 1) {
                val j = stream.sampleIndex(k + 1)
                val tmp = cycle[k]
                cycle[k] = cycle[j]
                cycle[j] = tmp
            }
            for (cls in cycle) {
                if (result.length >= o.length) break
                result.append(cls[stream.sampleIndex(cls.length)])
            }
        }
        return result.toString()
    }

    private fun buildEnabledClassesV3(o: PassgenOptions): List<String> {
        val ambiguous = if (o.noAmbiguous) o.ambiguous() else ""
        fun maybeFilter(cls: String) =
            if (ambiguous.isEmpty()) cls else cls.filterAmbiguous(ambiguous)
        return buildList {
            if (!o.noUppers) add(maybeFilter(UPPERS))
            add(maybeFilter(LOWERS))
            if (!o.noNumbers) add(maybeFilter(NUMBERS))
            if (!o.noSpecials) add(maybeFilter(o.specials()))
        }.filter { it.isNotEmpty() }
    }

    /** Uniform integer in [0, n) by rejection sampling, one keystream byte per draw. */
    private fun ChaCha20Stream.sampleIndex(n: Int): Int {
        val limit = (256 / n) * n
        while (true) {
            val b = nextByte()
            if (b < limit) return b % n
        }
    }

    // endregion v3

    private fun PassgenOptions.specials(): String = customSpecials.ifEmpty { DEFAULT_SPECIALS }

    private fun PassgenOptions.ambiguous(): String = customAmbiguous.ifEmpty { DEFAULT_AMBIGUOUS }

    private fun String.filterAmbiguous(ambiguous: String): String = filterNot { it in ambiguous }

    private fun String.isPrintableAscii(): Boolean = all { it.code in 0x20..0x7E }

    private fun sha256(input: String): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
}
