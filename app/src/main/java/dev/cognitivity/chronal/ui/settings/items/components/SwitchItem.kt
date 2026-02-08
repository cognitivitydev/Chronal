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

package dev.cognitivity.chronal.ui.settings.items.components

import android.content.Intent
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.ui.settings.items.SettingItem
import kotlinx.coroutines.launch

@Composable
fun SwitchItem(item: SettingItem.Switch, onNavigate: ((String) -> Unit)?) {
    val scope = rememberCoroutineScope()
    var checked by remember { mutableStateOf(item.setting.get()) }
    val interactionSource = remember { MutableInteractionSource() }

    val onCheckedChange: () -> Unit = {
        checked = !checked
        item.onCheckedChange(checked)
        scope.launch {
            item.setting.save(checked)
        }
    }

    val clickable = item.pageId != null || item.activity != null

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxHeight()
            .clickable(
                interactionSource = if(clickable) interactionSource else remember { MutableInteractionSource() },
                indication = LocalIndication.current
            ) {
                if(!clickable) onCheckedChange()
                else {
                    if(item.pageId != null) onNavigate?.invoke(item.pageId)
                    else ChronalApp.getInstance().startActivity(
                        Intent(context, item.activity)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }
    ) {
        Column(
            modifier = Modifier.width(IntrinsicSize.Max)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(context.getString(item.meta.title), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            if(item.meta.description != null) {
                Text(item.meta.description.invoke(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        if(clickable) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(32.dp)
            )
            Box(
                modifier = Modifier.width(1.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange() },
            interactionSource = interactionSource,
            modifier = Modifier.padding(start = 12.dp, end = 16.dp)
        )
    }
}
