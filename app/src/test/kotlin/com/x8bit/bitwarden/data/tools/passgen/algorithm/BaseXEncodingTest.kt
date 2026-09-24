package com.x8bit.bitwarden.data.tools.passgen.algorithm

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BaseXEncodingTest {

    @Test
    fun `encodes like eknkc basex`() {
        val binary = BaseXEncoding("01")
        assertEquals("101", binary.encode(byteArrayOf(5)))
        assertEquals("001", binary.encode(byteArrayOf(0, 0, 1)))
        assertEquals("0", binary.encode(byteArrayOf(0)))
        assertEquals("", binary.encode(byteArrayOf()))
        assertEquals("ff01", BaseXEncoding("0123456789abcdef").encode(byteArrayOf(-1, 1)))
    }

    @Test
    fun `duplicate alphabet throws Ambiguous alphabet`() {
        val e = assertThrows<PassgenException> { BaseXEncoding("abca") }
        assertEquals("Ambiguous alphabet.", e.message)
    }

    @Test
    fun `alphabet shorter than 2 throws`() {
        assertThrows<PassgenException> { BaseXEncoding("a") }
        assertThrows<PassgenException> { BaseXEncoding("") }
    }
}
