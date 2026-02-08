/*
 * Chronal: Metronome app for Android
 * Copyright (C) 2026  cognitivity
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.cognitivity.chronal.ui.settings.screens.appinfo

import android.content.ClipData
import android.content.Intent
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.activity.MainActivity
import dev.cognitivity.chronal.settings.Setting
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.ui.settings.items.SettingItem
import dev.cognitivity.chronal.ui.settings.items.SettingMeta
import dev.cognitivity.chronal.ui.settings.data.SettingsPage
import dev.cognitivity.chronal.ui.settings.screens.appinfo.DeveloperOptionsPage.ImportExport
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object DeveloperOptionsPage : SettingsPage(
    id = "developer_options",
    title = R.string.page_settings_developer_options,
    items = listOf(
        SettingItem.SwitchHeader(
            meta = SettingMeta(R.string.setting_name_show_developer_options),
            setting = Settings.SHOW_DEVELOPER_OPTIONS
        ),
        SettingItem.LongDescription(R.string.settings_developer_options_warning),
        SettingItem.Container {
            ImportExport()
        }
    )
) {
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun ImportExport() {
        val scope = rememberCoroutineScope()

        val clipboard = LocalClipboard.current

        val gson = GsonBuilder().setPrettyPrinting().create()
        var string by remember { mutableStateOf(gson.toJson(Setting.exportToJson())) }
        var error by remember { mutableStateOf(null as String?) }

        var importing by remember { mutableStateOf(false) }

        fun compress(): String {
            val compact = Gson().toJson(Setting.exportToJson())

            val compressed = ByteArrayOutputStream().use {
                GZIPOutputStream(it).use { gzip ->
                    gzip.write(compact.toByteArray(Charsets.UTF_8))
                }
                it.toByteArray()
            }
            val base64 = Base64.encode(compressed, Base64.DEFAULT)
            val string = String(base64, Charsets.UTF_8)

            return string
        }

        fun decompress(string: String): String {
            val gzip = Base64.decode(string, Base64.DEFAULT)
            val decompressed = ByteArrayOutputStream().use { output ->
                GZIPInputStream(gzip.inputStream()).use { gzip ->
                    gzip.copyTo(output)
                }
                output.toByteArray()
            }
            return String(decompressed, Charsets.UTF_8)
        }

        fun parseInputToJson(): JsonObject {
            try {
                val decompressed = decompress(string)
                val obj = gson.fromJson(decompressed, JsonObject::class.java)
                return obj
            } catch (_: Exception) {
                val obj = gson.fromJson(string, JsonObject::class.java)
                return obj
            }
        }

        @Composable
        fun ImportRow() {
            Button(
                onClick = {
                    try {
                        val jsonObject = parseInputToJson()
                        Setting.importFromJson(jsonObject)
                        scope.launch {
                            Setting.saveAll()
                            // reload app
                            ChronalApp.getInstance().startActivity(
                                Intent(ChronalApp.getInstance(), MainActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
                            )
                        }
                    } catch(exception: Exception) {
                        error = exception.message
                        Toast.makeText(ChronalApp.getInstance(), error, Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = error == null
            ) {
                Text(context.getString(R.string.generic_save))
            }
            FilledTonalButton(
                onClick = {
                    string = gson.toJson(Setting.exportToJson())
                    error = null
                    importing = false
                }
            ) {
                Text(context.getString(R.string.generic_cancel))
            }
        }
        @Composable
        fun RowScope.ImportExportRow() {
            FilledIconButton(
                onClick = {
                    scope.launch {
                        val compressed = compress()
                        clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Chronal settings", compressed)))
                    }
                },
                modifier = Modifier.minimumInteractiveComponentSize()
                    .size(IconButtonDefaults.smallContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide))
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_download_24),
                    contentDescription = context.getString(R.string.generic_export),
                    modifier = Modifier.size(IconButtonDefaults.smallIconSize),
                )
            }
            FilledTonalIconButton(
                onClick = {
                    importing = true
                },
                modifier = Modifier.minimumInteractiveComponentSize()
                    .size(IconButtonDefaults.smallContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide))
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_upload_24),
                    contentDescription = context.getString(R.string.generic_import),
                    modifier = Modifier.size(IconButtonDefaults.smallIconSize),
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    string = gson.toJson(Setting.exportToJson())
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_refresh_24),
                    contentDescription = context.getString(R.string.generic_refresh),
                    modifier = Modifier.size(IconButtonDefaults.smallIconSize),
                )
            }
        }


        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(context.getString(R.string.setting_name_raw), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Box(
                modifier = Modifier.padding(top = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if(importing) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    if (importing) {
                        val focusRequester = remember { FocusRequester() }
                        LaunchedEffect(importing) {
                            if (importing) focusRequester.requestFocus()
                        }
                        BasicTextField(
                            value = string,
                            onValueChange = {
                                string = it
                                error = null
                            },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                                .focusRequester(focusRequester)
                        )
                    } else {
                        SelectionContainer {
                            Text(
                                text = string,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Column {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(start = 8.dp, top = 16.dp)
                ) {
                    if(importing) {
                        ImportRow()
                    } else {
                        ImportExportRow()
                    }
                }
                if(error != null) {
                    Row {
                        Icon(
                            painter = painterResource(R.drawable.outline_warning_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
                        )
                        Text(error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}
