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
package com.vitorpamplona.amethyst.ui.screen.loggedIn.keyBackup

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import com.vitorpamplona.amethyst.LocalPreferences
import com.vitorpamplona.amethyst.commons.icons.symbols.Icon
import com.vitorpamplona.amethyst.commons.icons.symbols.MaterialSymbols
import com.vitorpamplona.amethyst.commons.keystorage.PaperBackup
import com.vitorpamplona.amethyst.commons.resources.Res
import com.vitorpamplona.amethyst.commons.resources.copy_my_secret_key
import com.vitorpamplona.amethyst.commons.resources.paper_backup_check
import com.vitorpamplona.amethyst.commons.resources.paper_backup_check_my_copy
import com.vitorpamplona.amethyst.commons.resources.paper_backup_done
import com.vitorpamplona.amethyst.commons.resources.paper_backup_done_body
import com.vitorpamplona.amethyst.commons.resources.paper_backup_done_title
import com.vitorpamplona.amethyst.commons.resources.paper_backup_group_label
import com.vitorpamplona.amethyst.commons.resources.paper_backup_intro
import com.vitorpamplona.amethyst.commons.resources.paper_backup_mismatch
import com.vitorpamplona.amethyst.commons.resources.paper_backup_reading_tips
import com.vitorpamplona.amethyst.commons.resources.paper_backup_show_again
import com.vitorpamplona.amethyst.commons.resources.paper_backup_title
import com.vitorpamplona.amethyst.commons.resources.paper_backup_verify_body
import com.vitorpamplona.amethyst.commons.resources.paper_backup_verify_title
import com.vitorpamplona.amethyst.commons.resources.paper_backup_write_it_down
import com.vitorpamplona.amethyst.commons.resources.paper_backup_write_it_down_description
import com.vitorpamplona.amethyst.commons.resources.paper_backup_written_it_down
import com.vitorpamplona.amethyst.commons.ui.stringRes
import com.vitorpamplona.amethyst.commons.ui.theme.ButtonBorder
import com.vitorpamplona.amethyst.commons.ui.theme.ButtonPadding
import com.vitorpamplona.amethyst.ui.note.authenticate
import com.vitorpamplona.amethyst.ui.note.rememberAuthPromptLabels
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import com.vitorpamplona.quartz.nip19Bech32.toNpub
import com.vitorpamplona.quartz.nip19Bech32.toNsec
import kotlinx.coroutines.launch

/**
 * Opens the paper backup after the same device authentication the copy button uses.
 * Hidden for accounts whose key this app doesn't hold (signer apps, bunkers, read-only).
 */
@Composable
fun PaperBackupButton(accountViewModel: AccountViewModel) {
    val keyPair = accountViewModel.account.settings.keyPair
    val privKey = keyPair.privKey ?: return
    val context = LocalContext.current
    val authLabels = rememberAuthPromptLabels()
    val authTitle = stringRes(Res.string.copy_my_secret_key)
    var showing by remember { mutableStateOf(false) }

    val keyguardLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) showing = true
        }

    OutlinedButton(
        modifier = Modifier.padding(horizontal = 3.dp),
        onClick = {
            authenticate(
                title = authTitle,
                context = context,
                labels = authLabels,
                keyguardLauncher = keyguardLauncher,
                onApproved = { showing = true },
                onError = { title, message -> accountViewModel.toastManager.toast(title, message) },
            )
        },
        shape = ButtonBorder,
        contentPadding = ButtonPadding,
    ) {
        Icon(
            symbol = MaterialSymbols.EditNote,
            contentDescription = stringRes(Res.string.paper_backup_write_it_down_description),
            modifier = Modifier.padding(end = 5.dp),
        )
        Text(stringRes(Res.string.paper_backup_write_it_down))
    }

    if (showing) {
        PaperBackupDialog(
            nsec = privKey.toNsec(),
            npub = keyPair.pubKey.toNpub(),
            onDismiss = { showing = false },
        )
    }
}

private enum class PaperBackupStep { SHOW, VERIFY, DONE }

/**
 * Full-screen dialog: the key as large numbered groups, then two random groups typed back from
 * the paper. Only a correct copy marks the key as backed up (which also retires the home nudge).
 * The dialog is its own window, so it sets FLAG_SECURE itself.
 */
@Composable
private fun PaperBackupDialog(
    nsec: String,
    npub: String,
    onDismiss: () -> Unit,
) {
    val groups = remember(nsec) { PaperBackup.groups(nsec) }
    val challenge = remember(nsec) { PaperBackup.pickChallenge(groups.size) }
    var step by rememberSaveable { mutableStateOf(PaperBackupStep.SHOW) }
    var wroteItDown by rememberSaveable { mutableStateOf(false) }
    var firstAnswer by rememberSaveable { mutableStateOf("") }
    var secondAnswer by rememberSaveable { mutableStateOf("") }
    var mismatch by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                securePolicy = SecureFlagPolicy.SecureOn,
            ),
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when (step) {
                    PaperBackupStep.SHOW -> {
                        Text(stringRes(Res.string.paper_backup_title), style = MaterialTheme.typography.titleLarge)
                        Text(stringRes(Res.string.paper_backup_intro))
                        KeyGroups(groups)
                        Text(
                            stringRes(Res.string.paper_backup_reading_tips),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = wroteItDown, onCheckedChange = { wroteItDown = it })
                            Text(stringRes(Res.string.paper_backup_written_it_down, groups.size))
                        }
                        Button(
                            onClick = { step = PaperBackupStep.VERIFY },
                            enabled = wroteItDown,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(stringRes(Res.string.paper_backup_check_my_copy)) }
                    }

                    PaperBackupStep.VERIFY -> {
                        Text(stringRes(Res.string.paper_backup_verify_title), style = MaterialTheme.typography.titleLarge)
                        Text(stringRes(Res.string.paper_backup_verify_body))
                        GroupField(challenge.first, firstAnswer) {
                            firstAnswer = it
                            mismatch = false
                        }
                        GroupField(challenge.second, secondAnswer) {
                            secondAnswer = it
                            mismatch = false
                        }
                        if (mismatch) {
                            Text(stringRes(Res.string.paper_backup_mismatch), color = MaterialTheme.colorScheme.error)
                        }
                        Button(
                            onClick = {
                                if (PaperBackup.matches(groups, challenge.first, firstAnswer) &&
                                    PaperBackup.matches(groups, challenge.second, secondAnswer)
                                ) {
                                    scope.launch { LocalPreferences.setHasBackedUpKeys(true, npub) }
                                    step = PaperBackupStep.DONE
                                } else {
                                    mismatch = true
                                }
                            },
                            enabled = firstAnswer.isNotBlank() && secondAnswer.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(stringRes(Res.string.paper_backup_check)) }
                        OutlinedButton(
                            onClick = { step = PaperBackupStep.SHOW },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(stringRes(Res.string.paper_backup_show_again)) }
                    }

                    PaperBackupStep.DONE -> {
                        Text(stringRes(Res.string.paper_backup_done_title), style = MaterialTheme.typography.titleLarge)
                        Text(stringRes(Res.string.paper_backup_done_body))
                        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                            Text(stringRes(Res.string.paper_backup_done))
                        }
                    }
                }
            }
        }
    }
}

/** The key as numbered groups, two per row, in a large monospaced font. */
@Composable
private fun KeyGroups(groups: List<String>) {
    val keyStyle =
        MaterialTheme.typography.headlineMedium.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 30.sp,
            letterSpacing = 2.sp,
        )
    val labelStyle = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace)
    Column(
        Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(PaperBackup.PREFIX, style = keyStyle, fontWeight = FontWeight.Bold)
        groups.chunked(2).forEachIndexed { row, pair ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                pair.forEachIndexed { col, group ->
                    Text(
                        (row * 2 + col + 1).toString().padStart(2, '0'),
                        style = labelStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(group, style = keyStyle, modifier = Modifier.weight(1f))
                }
                // Keeps a lone last group aligned with the column above it.
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun GroupField(
    number: Int,
    value: String,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(stringRes(Res.string.paper_backup_group_label, number.toString().padStart(2, '0'))) },
        singleLine = true,
        textStyle = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace),
        keyboardOptions =
            KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Password,
            ),
        modifier = Modifier.fillMaxWidth(),
    )
}
