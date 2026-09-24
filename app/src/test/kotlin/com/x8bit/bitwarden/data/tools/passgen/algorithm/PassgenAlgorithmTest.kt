package com.x8bit.bitwarden.data.tools.passgen.algorithm

import com.x8bit.bitwarden.data.tools.passgen.model.PassgenOptions
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows

class PassgenAlgorithmTest {

    @Serializable
    private data class GoldenVector(
        val name: String,
        val version: Int,
        val passphrase: String,
        val salt: String,
        val length: Int,
        val noUppers: Boolean,
        val noNumbers: Boolean,
        val noSpecials: Boolean,
        val customSpecials: String,
        val noAmbiguous: Boolean,
        val customAmbiguous: String,
        val expected: String,
        val error: Boolean,
    )

    private val vectors: List<GoldenVector> by lazy {
        val text = requireNotNull(javaClass.classLoader?.getResource("passgen/golden_vectors.json"))
            .readText()
        Json.decodeFromString(text)
    }

    @Test
    fun `golden vectors are present and include a unicode passphrase`() {
        assertTrue(vectors.size > 1900)
        assertTrue(vectors.any { it.passphrase == "pässwörd✓" && !it.error })
    }

    @TestFactory
    fun `matches Go passgen for every golden vector`(): List<DynamicTest> =
        vectors.map { v ->
            DynamicTest.dynamicTest(v.name) {
                val options = PassgenOptions(
                    version = v.version,
                    passphrase = v.passphrase,
                    salt = v.salt,
                    length = v.length,
                    noUppers = v.noUppers,
                    noNumbers = v.noNumbers,
                    noSpecials = v.noSpecials,
                    customSpecials = v.customSpecials,
                    noAmbiguous = v.noAmbiguous,
                    customAmbiguous = v.customAmbiguous,
                )
                if (v.error) {
                    assertThrows<PassgenException> { PassgenAlgorithm.generate(options) }
                } else {
                    assertEquals(v.expected, PassgenAlgorithm.generate(options))
                }
            }
        }

    @Test
    fun `rejects empty passphrase, bad length, unknown version and non-ASCII custom sets`() {
        val base = PassgenOptions(passphrase = "p")
        assertThrows<PassgenException> { PassgenAlgorithm.generate(base.copy(passphrase = "")) }
        assertThrows<PassgenException> { PassgenAlgorithm.generate(base.copy(length = 0)) }
        assertThrows<PassgenException> { PassgenAlgorithm.generate(base.copy(version = 1)) }
        assertThrows<PassgenException> {
            PassgenAlgorithm.generate(base.copy(customSpecials = "é!"))
        }
        assertThrows<PassgenException> {
            PassgenAlgorithm.generate(base.copy(noAmbiguous = true, customAmbiguous = "\n"))
        }
    }
}
