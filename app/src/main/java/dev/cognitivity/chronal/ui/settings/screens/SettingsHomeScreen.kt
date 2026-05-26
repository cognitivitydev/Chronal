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

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.ui.settings.items.SettingItem
import dev.cognitivity.chronal.ui.settings.items.SettingItemRenderer
import dev.cognitivity.chronal.ui.settings.items.SettingMeta
import dev.cognitivity.chronal.ui.settings.items.components.ReviewButton
import dev.cognitivity.chronal.ui.settings.layout.SettingsLayout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHomeScreen(navController: NavController, expanded: Boolean) {
    var showReview by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = if(expanded) WindowInsets(0) else ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.navigationBars),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.page_settings)) },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 48.dp)
            ) {
                val categories = SettingsLayout.categories.toList()
                itemsIndexed(categories) { index, category ->
                    SettingItemRenderer(
                        item = SettingItem.CategoryOption(
                            meta = SettingMeta(
                                title = category.title,
                                icon = category.icon,
                            ),
                            color = category.color,
                            pageId = category.id
                        ),
                        topRounded = index == 0,
                        bottomRounded = index == categories.size - 1,
                        onNavigate = { navController.navigate("category/$it") }
                    )
                }
            }
            val animationSpec = MaterialTheme.motionScheme.slowSpatialSpec<IntOffset>()
            AnimatedVisibility(visible = showReview,
                enter = slideInVertically(animationSpec) { it },
                exit = slideOutVertically(animationSpec) { it }
            ) {
                ReviewButton { showReview = false }
            }
            showReview = shouldShowReview()
        }
    }
}

private fun shouldShowReview(): Boolean {
    // check if cooldown has passed
    val nextReview = Settings.REVIEW_TIMESTAMP.get()
    if(System.currentTimeMillis() < nextReview || nextReview == -1L) return false

    // show only when installed from Play Store
    val packageManager = ChronalApp.getInstance().packageManager
    val packageName = ChronalApp.getInstance().packageName
    val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        packageManager.getInstallSourceInfo(packageName).installingPackageName
    } else {
        packageManager.getInstallerPackageName(packageName)
    }
    return installer == "com.android.vending"// || installer == null
}