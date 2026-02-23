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

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.settings.types.BooleanSetting
import dev.cognitivity.chronal.settings.types.FloatSetting
import dev.cognitivity.chronal.settings.types.IntSetting

sealed class SettingItem(
    open val meta: SettingMeta,
    val hasContainer: Boolean
) {
    /**
     * A header that separates settings into subcategories.
     */
    data class SubCategoryHeader(
        val text: Int,
        override val meta: SettingMeta = SettingMeta(text)
    ) : SettingItem(meta, false)

    /**
     * A divider that separates settings without a specific header.
     */
    data class Divider(
        override val meta: SettingMeta = SettingMeta(R.string.generic_unknown),
    ) : SettingItem(meta, false)

    /**
     * Basic switch setting. If `pageId` or `activity` is non-null, tapping the container will open the specified page.
     */
    data class Switch(
        val setting: BooleanSetting,
        val pageId: String? = null,
        val activity: Class<*>? = null,
        val onCheckedChange: ((Boolean) -> Unit) = {},
        override val meta: SettingMeta
    ) : SettingItem(meta, true)

    /**
     * A header for individual setting pages that contain a switch. Appears above a longer description and/or other related settings.
     */
    data class SwitchHeader(
        val setting: BooleanSetting,
        val onCheckedChange: ((Boolean) -> Unit) = {},
        override val meta: SettingMeta
    ) : SettingItem(meta, false)

    /**
     * A group of radio buttons.
     */
    data class RadioGroupItem(
        val setting: IntSetting,
        val options: List<RadioOption>,
        val onOptionSelected: ((Int) -> Unit) = {},
    ) : SettingItem(SettingMeta(R.string.generic_unknown), false)

    data class RadioOption(
        val id: Int,
        val title: Int,
        val description: Int? = null
    )

    /**
     * A slider for integer settings, appearing below the title and description.
     */
    data class IntSlider(
        val setting: IntSetting,
        val pageId: String? = null,
        val onValueChange: ((Int) -> Unit) = {},
        val onValueChangeFinished: ((Int) -> Unit) = {},
        val range: IntRange,
        val steps: Int = 0,
        val valueLabel: (Int) -> String,
        override val meta: SettingMeta
    ) : SettingItem(meta, true)

    /**
     * A slider for float settings, appearing below the title and description.
     */
    data class FloatSlider(
        val setting: FloatSetting,
        val pageId: String? = null,
        val onValueChange: ((Float) -> Unit) = {},
        val onValueChangeFinished: ((Float) -> Unit) = {},
        val range: ClosedFloatingPointRange<Float>,
        val steps: Int = 0,
        val valueLabel: (Float) -> String,
        override val meta: SettingMeta
    ) : SettingItem(meta, true)

    /**
     * A time selector for integer settings, represented as minutes after midnight in local time.
     */
    data class TimeSelector(
        val setting: IntSetting,
        val onTimeSelected: ((Int) -> Unit) = {},
        override val meta: SettingMeta
    ) : SettingItem(meta, true)

    /**
     * A link to another settings page.
     */
    data class PageLink(
        val pageId: String,
        override val meta: SettingMeta
    ) : SettingItem(meta, true)

    /**
     * A button that appears in the main category list and opens a category when tapped.
     */
    data class CategoryOption(
        val iconColor: @Composable (() -> Color),
        val iconContainer: @Composable (() -> Color),
        val pageId: String,
        override val meta: SettingMeta
    ) : SettingItem(meta, true)

    /**
     * A link to an activity.
     */
    data class ActivityLink(
        val activity: Class<*>,
        override val meta: SettingMeta
    ) : SettingItem(meta, true)

    /**
     * A link to a URI
     */
    data class UriLink(
        val uri: Uri,
        override val meta: SettingMeta
    ) : SettingItem(meta, true)

    /**
     * An informational text element that can have a custom click action.
     */
    data class TextElement(
        val onClick: (() -> Unit) = {},
        override val meta: SettingMeta,
    ) : SettingItem(meta, true)

    /**
     * A long description that appears in individual settings pages.
     */
    data class LongDescription(
        val text: Int,
        override val meta: SettingMeta = SettingMeta(R.string.generic_unknown)
    ) : SettingItem(meta, false)

    /**
     * A custom element that does not fit into the above categories and has custom content.
     */
    data class Element(
        val content: @Composable () -> Unit
    ) : SettingItem(SettingMeta(R.string.generic_unknown), false)

    /**
     * A custom element that does not fit into the above categories and has custom content within a container.
     */
    data class Container(
        val content: @Composable () -> Unit,
    ) : SettingItem(SettingMeta(R.string.generic_unknown), true)
}

data class SettingMeta(
    val title: Int,
    val description: (() -> String)? = null,
    val icon: Int? = null,
    val visible: () -> Boolean = { true }
) {
    constructor(
        title: Int,
        description: Int,
        icon: Int? = null,
        iconColor: (@Composable () -> Color)? = null,
        iconContainer: (@Composable () -> Color)? = null,
        visible: () -> Boolean = { true }
    ) : this(
        title,
        description = { ChronalApp.getInstance().getString(description) },
        icon,
        visible
    )
}