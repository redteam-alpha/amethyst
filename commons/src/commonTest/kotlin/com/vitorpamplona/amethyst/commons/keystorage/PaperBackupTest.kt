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

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PaperBackupTest {
    // NIP-19 test vector.
    private val nsec = "nsec1vl029mgpspedva04g90vltkh6fvh240zqtv9k0t9af8935ke9laqsnlfe5"

    @Test
    fun groupsRebuildTheKey() {
        val groups = PaperBackup.groups(nsec)
        assertEquals(15, groups.size)
        assertTrue(groups.dropLast(1).all { it.length == 4 })
        assertEquals(nsec, PaperBackup.PREFIX + groups.joinToString(""))
    }

    @Test
    fun challengePicksTwoDifferentGroupsInRange() {
        val random = Random(42)
        repeat(500) {
            val (a, b) = PaperBackup.pickChallenge(15, random)
            assertTrue(a in 1..15 && b in 1..15 && a < b)
        }
    }

    @Test
    fun answersIgnoreCaseAndSpacesOnly() {
        val groups = listOf("vl02", "9mgp", "sped")
        assertTrue(PaperBackup.matches(groups, 2, " 9MGP "))
        assertTrue(PaperBackup.matches(groups, 3, "sp ed"))
        assertFalse(PaperBackup.matches(groups, 3, "sped1"))
        assertFalse(PaperBackup.matches(groups, 4, "vl02"))
    }
}
