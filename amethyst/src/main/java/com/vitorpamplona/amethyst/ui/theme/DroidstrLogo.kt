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
package com.vitorpamplona.amethyst.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitorpamplona.amethyst.commons.resources.Res
import com.vitorpamplona.amethyst.commons.resources.app_logo
import com.vitorpamplona.amethyst.commons.resources.droidstr_wordmark
import com.vitorpamplona.amethyst.commons.ui.stringRes

private val LogoGold = Color(0xFFE6B450)
private val LogoRed = Color(0xFFB0122A)

/** The Droidstr mark with its wordmark, for the login and sign-up screens. */
@Composable
fun DroidstrLogo(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            imageVector = DroidstrMark,
            contentDescription = stringRes(Res.string.app_logo),
            modifier = Modifier.size(150.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringRes(Res.string.droidstr_wordmark),
            color = LogoGold,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            letterSpacing = 8.sp,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

// Same geometry as the launcher icon (res/drawable/ic_launcher_foreground.xml), framed by an octagon.
val DroidstrMark: ImageVector by lazy {
    ImageVector
        .Builder(
            name = "DroidstrMark",
            defaultWidth = 108.dp,
            defaultHeight = 108.dp,
            viewportWidth = 108f,
            viewportHeight = 108f,
        ).apply {
            path(stroke = SolidColor(LogoGold.copy(alpha = 0.6f)), strokeLineWidth = 1.5f) {
                moveTo(54f, 8f)
                lineTo(86.5f, 21.5f)
                lineTo(100f, 54f)
                lineTo(86.5f, 86.5f)
                lineTo(54f, 100f)
                lineTo(21.5f, 86.5f)
                lineTo(8f, 54f)
                lineTo(21.5f, 21.5f)
                close()
            }
            // The dark red "glitch" copy, offset down and to the left.
            path(fill = SolidColor(LogoRed), pathFillType = PathFillType.EvenOdd) { letterD(-2.5f, 1f) }
            path(fill = SolidColor(LogoGold), pathFillType = PathFillType.EvenOdd) { letterD(0f, 0f) }
            path(fill = SolidColor(LogoRed)) { bar(29f, 49f, 42f, 52f) }
            path(fill = SolidColor(LogoGold)) { bar(66f, 59f, 79f, 61f) }
        }.build()
}

private fun PathBuilder.letterD(
    dx: Float,
    dy: Float,
) {
    moveTo(37f + dx, 30f + dy)
    lineTo(57f + dx, 30f + dy)
    lineTo(71f + dx, 44f + dy)
    lineTo(71f + dx, 64f + dy)
    lineTo(57f + dx, 78f + dy)
    lineTo(37f + dx, 78f + dy)
    close()
    moveTo(46f + dx, 39f + dy)
    lineTo(46f + dx, 69f + dy)
    lineTo(54f + dx, 69f + dy)
    lineTo(62f + dx, 61f + dy)
    lineTo(62f + dx, 47f + dy)
    lineTo(54f + dx, 39f + dy)
    close()
}

private fun PathBuilder.bar(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
) {
    moveTo(left, top)
    lineTo(right, top)
    lineTo(right, bottom)
    lineTo(left, bottom)
    close()
}
