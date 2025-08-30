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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.Setting
import kotlinx.coroutines.CoroutineScope
import kotlin.math.sin

@Composable
fun SettingsPageExpanded(
    categories: LinkedHashMap<String, ArrayList<Setting<*>>>,
    scope: CoroutineScope,
    context: Context,
) {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(horizontal = 12.dp)
                .fillMaxSize()
                .padding(
                    start = 0.dp,
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                    bottom = innerPadding.calculateBottomPadding()
                )
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(context.getString(R.string.page_settings),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    MoreSettingsDropdown()
                }
            }
            val outlineVariant = MaterialTheme.colorScheme.outlineVariant
            Canvas(
                modifier = Modifier.fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .zIndex(1f)
            ) {
                val strokeWidth = 2.dp.toPx()
                val path = Path().apply {
                    moveTo(0f, size.height / 2)
                    for (i in 0..size.width.toInt() step 4) {
                        lineTo(i.toFloat(), (size.height / 2) + (sin(i / 10f) * 2))
                    }
                }
                drawPath(
                    path = path,
                    color = outlineVariant,
                    style = Stroke(width = strokeWidth)
                )
            }
            LazyColumn {
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
}