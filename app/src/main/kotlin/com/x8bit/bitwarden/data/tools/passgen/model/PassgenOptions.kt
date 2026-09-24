package com.x8bit.bitwarden.data.tools.passgen.model

/**
 * Inputs to passgen. Mirrors the Go library's `opts` struct field for field.
 * `customAmbiguous` only applies when [noAmbiguous] is true.
 */
data class PassgenOptions(
    val version: Int = 3,
    val passphrase: String,
    val salt: String = "",
    val length: Int = 40,
    val noUppers: Boolean = false,
    val noNumbers: Boolean = false,
    val noSpecials: Boolean = false,
    val customSpecials: String = "",
    val noAmbiguous: Boolean = false,
    val customAmbiguous: String = "",
) {
    override fun toString(): String = "PassgenOptions(version=$version, length=$length)"
}
