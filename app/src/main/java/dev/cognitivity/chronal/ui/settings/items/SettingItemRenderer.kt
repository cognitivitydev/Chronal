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

package dev.cognitivity.chronal.ui.settings.items

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ui.settings.items.components.*

@Composable
fun SettingItemRenderer(
    item: SettingItem,
    topRounded: Boolean = false,
    bottomRounded: Boolean = false,
    onNavigate: ((String) -> Unit)? = null
) {
    if(!item.meta.visible()) return

    val shape = RoundedCornerShape(
        topStart = if (topRounded) 12.dp else 6.dp,
        topEnd = if (topRounded) 12.dp else 6.dp,
        bottomStart = if (bottomRounded) 12.dp else 6.dp,
        bottomEnd = if (bottomRounded) 12.dp else 6.dp
    )

    when (item) {
        is SettingItem.SubCategoryHeader -> SubCategoryHeaderItem(item)
        is SettingItem.Divider -> SettingsDividerItem()
        is SettingItem.SwitchHeader -> SwitchHeaderItem(item)
        is SettingItem.RadioGroupItem -> SettingRadioGroupRow(item)
        is SettingItem.LongDescription -> LongDescriptionItem(item)
        is SettingItem.Element -> item.content()

        else -> Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = shape,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 1.dp)
                .defaultMinSize(minHeight = 72.dp)
        ) {
            when (item) {
                is SettingItem.SubCategoryHeader -> SubCategoryHeaderItem(item)
                is SettingItem.Switch -> SwitchItem(item, onNavigate)
                is SettingItem.IntSlider,
                is SettingItem.FloatSlider
                    -> SliderItem(item, onNavigate)
                is SettingItem.TimeSelector -> TimeSelectorItem(item)
                is SettingItem.PageLink -> PageLinkItem(item, onNavigate)
                is SettingItem.CategoryOption -> CategoryOptionItem(item, onNavigate)
                is SettingItem.ActivityLink -> ActivityLinkItem(item)
                is SettingItem.UriLink -> UriLinkItem(item)
                is SettingItem.TextElement -> TextItem(item)
                is SettingItem.Container -> item.content()
                else -> throw IllegalArgumentException("Unsupported setting item type: ${item::class}")
            }
        }
    }
}