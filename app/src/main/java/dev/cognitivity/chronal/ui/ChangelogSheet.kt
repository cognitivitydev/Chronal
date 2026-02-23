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

package dev.cognitivity.chronal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.settings.Settings
import java.io.IOException
import kotlin.text.lines
import kotlin.text.replace

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChangelogSheet(onDismissRequest: () -> Unit,
                   fromVersionCode: Int = -1, fromVersion: String? = null,
                   toVersionCode: Int = Settings.VERSION_CODE.get(), toVersion: String = Settings.VERSION.get()
) {
    val entries = mutableListOf<String>()
    try {
        val files = context.assets.list("changelogs") ?: arrayOf()
        val versionCodes = files.mapNotNull { it.removeSuffix(".txt").toIntOrNull() }.sorted()
        for (code in versionCodes) {
            if (code in (fromVersionCode + 1)..toVersionCode) {
                val text = context.assets.open("changelogs/$code.txt").bufferedReader().use { it.readText() }
                entries.add(text)
            }
        }
    } catch (_: IOException) {}

    ModalBottomSheet(
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = context.getString(R.string.changelog_title),
                style = MaterialTheme.typography.headlineMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurface
            )
            if(fromVersion != null) {
                Text(
                    text = context.getString(R.string.changelog_updated, fromVersion, toVersion),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            overscrollEffect = null
        ) {
            items(entries.size) { index ->
                val changelog = entries[index]
                val version = changelog.lines()[0].removePrefix("# ").trim()
                val content = changelog.replace("# $version\n\n", "")

                Text(
                    text = version,
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}