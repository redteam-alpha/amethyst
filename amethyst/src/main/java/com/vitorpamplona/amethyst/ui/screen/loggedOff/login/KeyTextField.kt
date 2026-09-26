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
package com.vitorpamplona.amethyst.ui.screen.loggedOff.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.commons.icons.symbols.Icon
import com.vitorpamplona.amethyst.commons.icons.symbols.MaterialSymbols
import com.vitorpamplona.amethyst.commons.keystorage.KeyInputCheck
import com.vitorpamplona.amethyst.commons.resources.Res
import com.vitorpamplona.amethyst.commons.resources.hide_password
import com.vitorpamplona.amethyst.commons.resources.login_key_bad_nsec
import com.vitorpamplona.amethyst.commons.resources.login_key_grouped_label
import com.vitorpamplona.amethyst.commons.resources.login_key_hex_progress
import com.vitorpamplona.amethyst.commons.resources.login_key_hex_too_long
import com.vitorpamplona.amethyst.commons.resources.login_key_invalid
import com.vitorpamplona.amethyst.commons.resources.login_key_invalid_characters
import com.vitorpamplona.amethyst.commons.resources.login_key_private_for
import com.vitorpamplona.amethyst.commons.resources.login_key_public_only
import com.vitorpamplona.amethyst.commons.resources.login_with_qr_code
import com.vitorpamplona.amethyst.commons.resources.nsec_npub_hex_private_key
import com.vitorpamplona.amethyst.commons.resources.show_password
import com.vitorpamplona.amethyst.commons.ui.stringRes
import com.vitorpamplona.amethyst.commons.ui.theme.placeholderText
import com.vitorpamplona.amethyst.ui.painterRes
import com.vitorpamplona.amethyst.ui.screen.loggedIn.qrcode.SimpleQrCodeScanner

@Composable
fun KeyTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue, throughQR: Boolean) -> Unit,
    onLogin: () -> Unit,
) {
    var dialogOpen by remember { mutableStateOf(false) }

    var showCharsKey by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            modifier =
                Modifier
                    .semantics { contentType = ContentType.Password },
            value = value,
            onValueChange = { onValueChange(it, false) },
            keyboardOptions =
                KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Go,
                ),
            placeholder = {
                Text(
                    text = stringRes(Res.string.nsec_npub_hex_private_key),
                    color = MaterialTheme.colorScheme.placeholderText,
                )
            },
            trailingIcon = {
                Row {
                    IconButton(onClick = { showCharsKey = !showCharsKey }) {
                        Icon(
                            symbol = if (showCharsKey) MaterialSymbols.VisibilityOff else MaterialSymbols.Visibility,
                            contentDescription =
                                if (showCharsKey) {
                                    stringRes(Res.string.hide_password)
                                } else {
                                    stringRes(Res.string.show_password)
                                },
                        )
                    }
                }
            },
            leadingIcon = {
                if (dialogOpen) {
                    SimpleQrCodeScanner {
                        dialogOpen = false
                        if (!it.isNullOrEmpty()) {
                            onValueChange(TextFieldValue(it), true)
                        }
                    }
                }
                IconButton(onClick = { dialogOpen = true }) {
                    Icon(
                        painter = painterRes(R.drawable.ic_qrcode, 5),
                        contentDescription = stringRes(Res.string.login_with_qr_code),
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            },
            visualTransformation =
                if (showCharsKey) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardActions =
                KeyboardActions(
                    onGo = {
                        onLogin()
                    },
                ),
        )
        KeyInputFeedback(value.text, showCharsKey)
    }
}

/**
 * Droidstr: checks the key as it's typed. The status line never reveals the key (an npub is public);
 * the grouped copy only appears while the key is shown.
 */
@Composable
private fun KeyInputFeedback(
    text: String,
    showKey: Boolean,
) {
    val check = remember(text) { KeyInputCheck.check(text) }
    val (message, isError) =
        when (check) {
            KeyInputCheck.None -> null to false
            is KeyInputCheck.HexIncomplete -> stringRes(Res.string.login_key_hex_progress, check.count) to false
            is KeyInputCheck.HexTooLong -> stringRes(Res.string.login_key_hex_too_long, check.count) to true
            is KeyInputCheck.InvalidCharacters -> stringRes(Res.string.login_key_invalid_characters, check.characters) to true
            KeyInputCheck.BadNsec -> stringRes(Res.string.login_key_bad_nsec) to true
            KeyInputCheck.InvalidKey -> stringRes(Res.string.login_key_invalid) to true
            is KeyInputCheck.PrivateKey -> stringRes(Res.string.login_key_private_for, shortNpub(check.npub)) to false
            is KeyInputCheck.PublicKeyOnly -> stringRes(Res.string.login_key_public_only, shortNpub(check.npub)) to false
        }
    if (message != null) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
    val showsGroups =
        showKey &&
            check !is KeyInputCheck.None &&
            check !is KeyInputCheck.PublicKeyOnly &&
            text.isNotBlank()
    if (showsGroups) {
        Text(
            text = stringRes(Res.string.login_key_grouped_label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = KeyInputCheck.grouped(text),
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace, letterSpacing = 1.sp),
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

/** "npub1abcdefgh…uvwxyz": enough to recognise an account at a glance. */
private fun shortNpub(npub: String): String = if (npub.length > 24) npub.take(14) + "…" + npub.takeLast(8) else npub
