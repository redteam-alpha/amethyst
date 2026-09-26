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
import com.vitorpamplona.quartz.nip19Bech32.toNpub
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class KeyInputCheckTest {
    // BIP-340 test vector: secret key -> x-only public key.
    private val secret = "b7e151628aed2a6abf7158809cf4f3c762e7160f38b4da56a784d9045190cfef"
    private val npub = "dff1d77f2a671c5f36183726db2341be58feae1da2deced843240f7b502ba659".hexToByteArray().toNpub()

    @Test
    fun completeHexKeyShowsItsNpub() {
        assertEquals(KeyInputCheck.PrivateKey(npub), KeyInputCheck.check(secret))
        assertEquals(KeyInputCheck.PrivateKey(npub), KeyInputCheck.check("  " + secret.uppercase() + " "))
        // Typed or pasted in groups of four.
        assertEquals(KeyInputCheck.PrivateKey(npub), KeyInputCheck.check(KeyInputCheck.grouped(secret)))
    }

    @Test
    fun partialAndOverlongHexKeysAreCounted() {
        assertEquals(KeyInputCheck.HexIncomplete(58), KeyInputCheck.check(secret.dropLast(6)))
        assertEquals(KeyInputCheck.HexTooLong(65), KeyInputCheck.check(secret + "0"))
    }

    @Test
    fun typosInHexKeysAreNamed() {
        assertEquals(KeyInputCheck.InvalidCharacters("o"), KeyInputCheck.check(secret.replace('0', 'o')))
        // Short words aren't flagged: they're probably a name or a NIP-05.
        assertEquals(KeyInputCheck.None, KeyInputCheck.check("bob"))
    }

    @Test
    fun zeroIsNotAValidKey() {
        assertEquals(KeyInputCheck.InvalidKey, KeyInputCheck.check("0".repeat(64)))
    }

    @Test
    fun nsecAndNpubInput() {
        val nsec = "nsec1vl029mgpspedva04g90vltkh6fvh240zqtv9k0t9af8935ke9laqsnlfe5"
        assertIs<KeyInputCheck.PrivateKey>(KeyInputCheck.check(nsec))
        assertEquals(KeyInputCheck.BadNsec, KeyInputCheck.check(nsec.dropLast(1)))
        assertEquals(KeyInputCheck.PublicKeyOnly(npub), KeyInputCheck.check(npub))
        assertEquals(KeyInputCheck.None, KeyInputCheck.check("ncryptsec1qgg9947rlpvqu76pj5ecreduf9jxhselq2nae2kghhvd5g7dgjtcxfqtd67p9m0w57lspw8gsq6yphnm8623nsl8xn9j4jdzz84zm3frztj3z7s35vpzmqf6ksu8r89qk5z2zxfmu5gv8th8wclt0h4p"))
        assertEquals(KeyInputCheck.None, KeyInputCheck.check(""))
    }

    @Test
    fun spacedHexKeysAreNormalisedForLogin() {
        assertEquals(secret, KeyInputCheck.normalizedHexKey(" " + KeyInputCheck.grouped(secret).uppercase() + "\n"))
        assertEquals(null, KeyInputCheck.normalizedHexKey(secret.dropLast(1)))
        // Seed words and nsecs are left alone.
        assertEquals(null, KeyInputCheck.normalizedHexKey("abandon ability able about above absent"))
        assertEquals(null, KeyInputCheck.normalizedHexKey("nsec1vl029mgpspedva04g90vltkh6fvh240zqtv9k0t9af8935ke9laqsnlfe5"))
    }

    @Test
    fun groupsOfFour() {
        assertEquals("b7e1 5162 8aed", KeyInputCheck.grouped("b7e15162 8aed"))
    }
}
