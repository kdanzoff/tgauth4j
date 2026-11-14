package io.github.kdanzoff.tgauth4j.core

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/** HMAC-SHA256 et comparaison de signatures en temps constant. */
internal object Hmac {

    private const val ALGORITHM = "HmacSHA256"

    fun sha256(key: ByteArray, message: ByteArray): ByteArray =
        Mac.getInstance(ALGORITHM).apply { init(SecretKeySpec(key, ALGORITHM)) }.doFinal(message)

    fun hex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it) }

    /** Compare deux signatures hexadécimales en temps constant, sans tenir compte de la casse. */
    fun sameHex(expected: String, received: String): Boolean =
        MessageDigest.isEqual(expected.toByteArray(Charsets.UTF_8), received.lowercase().toByteArray(Charsets.UTF_8))
}
