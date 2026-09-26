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

/**
 * Handwritten ("paper") backup of an nsec: splits the key into short numbered groups that are easy
 * to copy by hand and read back, and checks a copy by asking for a couple of groups at random.
 */
object PaperBackup {
    const val PREFIX = "nsec1"
    private const val GROUP_SIZE = 4

    /** The part after `nsec1`, in groups of four characters (the last group may be shorter). */
    fun groups(nsec: String): List<String> {
        val body = nsec.trim().lowercase()
        require(body.startsWith(PREFIX)) { "Not an nsec" }
        return body.removePrefix(PREFIX).chunked(GROUP_SIZE)
    }

    /** Two different group numbers (1-based, ascending) to ask the user to type back from their paper. */
    fun pickChallenge(
        groupCount: Int,
        random: Random = Random.Default,
    ): Pair<Int, Int> {
        require(groupCount >= 2)
        val first = random.nextInt(groupCount)
        var second = random.nextInt(groupCount - 1)
        if (second >= first) second++
        return (minOf(first, second) + 1) to (maxOf(first, second) + 1)
    }

    /** Case and spaces don't matter; everything else must match exactly. */
    fun matches(
        groups: List<String>,
        number: Int,
        typed: String,
    ): Boolean = groups.getOrNull(number - 1) == typed.trim().lowercase().replace(" ", "")
}
