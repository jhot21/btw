package com.x8bit.bitwarden.data.tools.passgen.algorithm

/**
 * Thrown when passgen options cannot produce a password.
 */
class PassgenException(message: String) : IllegalArgumentException(message)
