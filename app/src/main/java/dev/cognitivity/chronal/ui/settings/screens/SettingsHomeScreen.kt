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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import dev.cognitivity.chronal.ui.settings.items.SettingItemRenderer
import dev.cognitivity.chronal.ui.settings.items.SettingItem
import dev.cognitivity.chronal.ui.settings.items.SettingMeta
import dev.cognitivity.chronal.ui.settings.layout.SettingsLayout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHomeScreen(navController: NavController, expanded: Boolean) {
    Scaffold(
        contentWindowInsets = if(expanded) WindowInsets(0) else ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.navigationBars),
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
        ) {
            val categories = SettingsLayout.categories.toList()
            itemsIndexed(categories) { index, category ->
                SettingItemRenderer(
                    SettingItem.PageLink(
                        meta = SettingMeta(
                            title = category.title,
                            icon = category.icon,
                            iconColor = category.iconColor,
                            iconContainer = category.iconContainer
                        ),
                        pageId = category.id
                    ),
                    topRounded = index == 0,
                    bottomRounded = index == categories.size - 1,
                    onNavigate = { navController.navigate("category/$it") }
                )
            }
        }
    }
}