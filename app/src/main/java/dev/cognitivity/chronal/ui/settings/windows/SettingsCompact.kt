/*
 * Chronal: Metronome app for Android
 * Copyright (C) 2025  cognitivity
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

package dev.cognitivity.chronal.ui.settings.windows

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.settings.Setting
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPageCompact(
    categories: LinkedHashMap<String, ArrayList<Setting<*>>>,
    scope: CoroutineScope,
    context: Context,
    padding: PaddingValues
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
            .padding(bottom = padding.calculateBottomPadding())
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(context.getString(R.string.page_settings)) },
                scrollBehavior = scrollBehavior,
                actions = {
                    Box(
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        MoreSettingsDropdown()
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize()
        ) {
            categories.entries.forEachIndexed { index, category ->
                item {
                    if (index != 0) Divider()
                    CategoryHeader(title = category.key)
                }
                items(category.value, key = { it.key.toString() }) { setting ->
                    DrawSetting(setting, scope, context)
                }
            }
            item {
                SettingsFooter()
            }
        }
    }
}