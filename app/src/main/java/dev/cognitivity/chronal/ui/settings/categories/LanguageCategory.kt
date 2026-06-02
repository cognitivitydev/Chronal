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

package dev.cognitivity.chronal.ui.settings.categories

import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import com.google.gson.Gson
import com.google.gson.JsonArray
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.settings.types.json.Language
import dev.cognitivity.chronal.ui.settings.data.SettingsCategory
import dev.cognitivity.chronal.ui.settings.items.SettingItem
import dev.cognitivity.chronal.ui.settings.items.SettingMeta

private val translationResource = R.raw.translations

object LanguageCategory : SettingsCategory(
    id = "language",
    title = R.string.page_settings_language,
    icon = R.drawable.outline_language_24,
    color = Color(0xFF00F7FF),
    items = getItems()
)

private fun getItems(): List<SettingItem> {
    val languages = Gson().fromJson(
        ChronalApp.getInstance().resources.openRawResource(translationResource).readBytes().decodeToString(),
        JsonArray::class.java
    ).map { Language(it.asJsonObject) }
    val items = mutableListOf<SettingItem>()

    languages.filter { it.progress > 0f }.forEach { language ->
        val progressString = when(language.progress) {
            in 0.00f..0.33f -> R.string.settings_translations_progress_few
            in 0.33f..0.67f -> R.string.settings_translations_progress_partial
            in 0.67f..0.95f -> R.string.settings_translations_progress_most
            else -> R.string.settings_translations_progress_complete
        }
        val icon = R.drawable.outline_translate_24

        items.add(
            SettingItem.PageLink(
                meta = SettingMeta(
                    title = { language.name },
                    description = { ChronalApp.getInstance().getString(progressString, (language.progress*100).toInt()) },
                    icon = icon
                ),
                pageId = "language/${language.key}"
            )
        )
    }
    val missing = languages.filter { it.progress == 0f }
    if(missing.isNotEmpty()) {
        items.add(
            SettingItem.SubCategoryHeader(
                text = R.string.settings_translations_missing_header
            )
        )
        missing.forEach { language ->
            val progressString = R.string.settings_translations_progress_none
            val icon = R.drawable.outline_globe_2_cancel_24

            items.add(
                SettingItem.PageLink(
                    meta = SettingMeta(
                        title = { language.name },
                        description = { ChronalApp.getInstance().getString(progressString, (language.progress*100).toInt()) },
                        icon = icon
                    ),
                    pageId = "language/${language.key}"
                )
            )
        }
    }
    items.add(SettingItem.Divider())
    items.add(
        SettingItem.TextElement(
            meta = SettingMeta(
                title = R.string.settings_translations_suggest,
                icon = R.drawable.outline_public_24,
            ),
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, "https://crowdin.com/project/chronal".toUri())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ChronalApp.getInstance().startActivity(intent)
            }
        )
    )
    return items
}