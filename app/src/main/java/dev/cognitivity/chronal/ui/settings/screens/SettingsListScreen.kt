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

package dev.cognitivity.chronal.ui.settings.screens

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.cognitivity.chronal.ui.settings.items.SettingItemRenderer
import dev.cognitivity.chronal.ui.settings.items.SettingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsListScreen(
    title: String,
    items: List<SettingItem>,
    navController: NavController,
    expanded: Boolean
) {
    fun prevVisible(fromIndex: Int): SettingItem? {
        var i = fromIndex - 1
        while (i >= 0) {
            val item = items[i]
            if (item.meta.visible()) return item
            i--
        }
        return null
    }
    fun nextVisible(fromIndex: Int): SettingItem? {
        var i = fromIndex + 1
        while (i < items.size) {
            val item = items[i]
            if (item.meta.visible()) return item
            i++
        }
        return null
    }

    Scaffold(
        contentWindowInsets = if(expanded) WindowInsets(0) else ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.navigationBars),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    FilledIconButton(
                        onClick = { navController.popBackStack() },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            itemsIndexed(items) { index, item ->
                val prevItem = prevVisible(index)
                val nextItem = nextVisible(index)
                val topRounded = prevItem == null || prevItem.hasContainer != item.hasContainer
                val bottomRounded = nextItem == null || nextItem.hasContainer != item.hasContainer

                SettingItemRenderer(
                    item,
                    topRounded = topRounded,
                    bottomRounded = bottomRounded,
                    onNavigate = { navController.navigate("page/$it") }
                )
            }
        }
    }
}