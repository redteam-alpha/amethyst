/*
 * Copyright (c) 2025 Vitor Pamplona
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.vitorpamplona.amethyst.commons.keystorage

import com.vitorpamplona.quartz.nip01Core.core.hexToByteArray
import com.vitorpamplona.quartz.nip01Core.crypto.KeyPair
import com.vitorpamplona.quartz.nip19Bech32.decodePrivateKeyAsHexOrNull
import com.vitorpamplona.quartz.nip19Bech32.decodePublicKeyAsHexOrNull
import com.vitorpamplona.quartz.nip19Bech32.toNpub

/**
 * Live feedback on what someone is typing into the login key field, so a 64-character hex key (or an
 * nsec) can be checked before logging in: how far along it is, what's wrong with it, and, once it's
 * complete, the npub it belongs to. The npub is public, so it's safe to show even while the key is hidden.
 */
sealed interface KeyInputCheck {
    /** Nothing to say: empty, or a login string handled elsewhere (ncryptsec, bunker, NIP-05…). */
    data object None : KeyInputCheck

    /** A hex private key in progress. */
    data class HexIncomplete(
        val count: Int,
    ) : KeyInputCheck

    data class HexTooLong(
        val count: Int,
    ) : KeyInputCheck

    /** Looks like a hex key, but has characters hex doesn't allow. */
    data class InvalidCharacters(
        val characters: String,
    ) : KeyInputCheck

    /** An nsec whose checksum doesn't match: incomplete or mistyped. */
    data object BadNsec : KeyInputCheck

    /** 64 hex characters, but not a usable secp256k1 key. */
    data object InvalidKey : KeyInputCheck

    data class PrivateKey(
        val npub: String,
    ) : KeyInputCheck

    /**
     * An nsec that only decodes after reading look-alike characters the way nsecs use them ("1" and
     * "i" as "l", "o" as "0"), which is how it will be logged in; the copy should still be fixed.
     */
    data class PrivateKeyWithLookAlikes(
        val npub: String,
    ) : KeyInputCheck

    /** An nsec with characters bech32 never uses after "nsec1" (1, b, i, o) that doesn't decode even when read as look-alikes. */
    data class NsecLookAlikes(
        val characters: String,
    ) : KeyInputCheck

    /** An npub or nprofile: logs in read-only. */
    data class PublicKeyOnly(
        val npub: String,
    ) : KeyInputCheck

    companion object {
        const val HEX_KEY_LENGTH = 64
        private const val NSEC_PREFIX = "nsec1"

        // Characters bech32 leaves out because they're easy to confuse, and what they most likely are.
        private const val NOT_BECH32 = "1bio"
        private val LOOK_ALIKES = mapOf('1' to 'l', 'i' to 'l', 'o' to '0')

        fun check(input: String): KeyInputCheck {
            val text = input.trim().removePrefix("nostr:")
            if (text.isEmpty()) return None
            val lower = text.lowercase()
            val compact = lower.filterNot { it.isWhitespace() }
            return when {
                compact.startsWith(NSEC_PREFIX) -> checkNsec(compact)
                lower.startsWith("npub1") || lower.startsWith("nprofile1") ->
                    // As with nsecs (see decodeNsec), only a 32-byte result is a successful decode.
                    decodePublicKeyAsHexOrNull(lower)
                        ?.takeIf { it.length == HEX_KEY_LENGTH }
                        ?.let { PublicKeyOnly(it.hexToByteArray().toNpub()) } ?: None
                lower.startsWith("ncryptsec1") || lower.startsWith("bunker:") || "@" in lower -> None
                else -> checkHex(lower)
            }
        }

        /**
         * A 64-character hex private key typed with spaces (in groups), padding or capitals, as the
         * plain lowercase hex the login code expects; null for anything else, which is passed on as is.
         */
        fun normalizedHexKey(input: String): String? {
            val compact = input.filterNot { it.isWhitespace() }.lowercase()
            return compact.takeIf { it.length == HEX_KEY_LENGTH && it.all { c -> c in '0'..'9' || c in 'a'..'f' } }
        }

        /**
         * What to hand to the login code: a hex key without spaces or capitals, or an nsec without spaces
         * and with look-alike characters read as nsecs use them, when that makes it decode. Anything else
         * is passed on as typed.
         */
        fun loginKey(input: String): String {
            normalizedHexKey(input)?.let { return it }
            val compact = input.trim().removePrefix("nostr:").filterNot { it.isWhitespace() }.lowercase()
            if (!compact.startsWith(NSEC_PREFIX)) return input
            return listOf(compact, withoutLookAlikes(compact)).firstOrNull { decodeNsec(it) != null } ?: input
        }

        /** The key as groups of four characters, for reading it back against a written copy. */
        fun grouped(input: String): String =
            input
                .trim()
                .filterNot { it.isWhitespace() }
                .chunked(4)
                .joinToString(" ")

        private fun checkNsec(nsec: String): KeyInputCheck {
            decodeNsec(nsec)?.let { return privateKey(it) }
            val body = nsec.removePrefix(NSEC_PREFIX)
            val lookAlikes = body.filter { it in NOT_BECH32 }.toSet()
            if (lookAlikes.isEmpty()) return BadNsec
            decodeNsec(withoutLookAlikes(nsec))?.let { hex ->
                val key = privateKey(hex)
                if (key is PrivateKey) return PrivateKeyWithLookAlikes(key.npub)
            }
            return NsecLookAlikes(lookAlikes.joinToString(" "))
        }

        private fun withoutLookAlikes(nsec: String): String =
            NSEC_PREFIX + nsec.removePrefix(NSEC_PREFIX).map { LOOK_ALIKES[it] ?: it }.joinToString("")

        // When a bech32 string doesn't parse, the decoder falls back to reading it as hex, which doesn't
        // reject non-hex characters, so anything but a 32-byte key is a failed decode.
        private fun decodeNsec(nsec: String): String? = decodePrivateKeyAsHexOrNull(nsec)?.takeIf { it.length == HEX_KEY_LENGTH }

        private fun checkHex(text: String): KeyInputCheck {
            // Spaces are allowed so a key copied in groups can be checked too.
            val compact = text.filterNot { it.isWhitespace() }
            val invalid = compact.filterNot { it in '0'..'9' || it in 'a'..'f' }.toSet()
            if (invalid.isNotEmpty()) {
                // Short or mostly non-hex input is probably something else (a name, a NIP-05…).
                return if (compact.length >= 8 && invalid.size <= 3) InvalidCharacters(invalid.joinToString(" ")) else None
            }
            return when {
                compact.length < HEX_KEY_LENGTH -> HexIncomplete(compact.length)
                compact.length > HEX_KEY_LENGTH -> HexTooLong(compact.length)
                else -> privateKey(compact)
            }
        }

        private fun privateKey(hex: String): KeyInputCheck =
            try {
                if (hex.length != HEX_KEY_LENGTH) {
                    InvalidKey
                } else {
                    PrivateKey(KeyPair(privKey = hex.hexToByteArray()).pubKey.toNpub())
                }
            } catch (e: Exception) {
                InvalidKey
            }
    }
}
