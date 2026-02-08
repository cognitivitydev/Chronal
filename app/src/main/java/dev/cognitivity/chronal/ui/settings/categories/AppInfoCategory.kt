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

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.activity.CreditsActivity
import dev.cognitivity.chronal.activity.HelpActivity
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.ui.settings.items.SettingItem
import dev.cognitivity.chronal.ui.settings.items.SettingMeta
import dev.cognitivity.chronal.ui.settings.data.SettingsCategory
import dev.cognitivity.chronal.ui.settings.screens.appinfo.DeveloperOptionsPage
import dev.cognitivity.chronal.ui.settings.screens.appinfo.FeedbackPage
import dev.cognitivity.chronal.ui.settings.screens.appinfo.SchemePage

private val PRIVACY_POLICY_URI = "https://chronal.cognitivity.dev/privacy".toUri()

private var developerOptionsEnabled by mutableStateOf(Settings.SHOW_DEVELOPER_OPTIONS.get())
private var developerOptionsCount by mutableIntStateOf(0) // enable developer options after 3 taps

object AppInfoCategory : SettingsCategory(
    id = "app_info",
    title = R.string.page_settings_app_info,
    icon = R.drawable.baseline_info_24,
    iconColor = { MaterialTheme.colorScheme.onPrimaryContainer },
    iconContainer = { MaterialTheme.colorScheme.primaryContainer },
    items = listOf(
        SettingItem.TextElement(
            onClick = {
                if(!developerOptionsEnabled) {
                    developerOptionsCount++
                    if(developerOptionsCount >= 3) {
                        Settings.SHOW_DEVELOPER_OPTIONS.set(true)
                        developerOptionsEnabled = true
                        developerOptionsCount = 0
                    }
                }
            },
            meta = SettingMeta(R.string.setting_name_version, description = { Settings.VERSION.get() }),
        ),
        SettingItem.PageLink(
            meta = SettingMeta(R.string.page_settings_color_scheme, R.string.setting_description_color_scheme),
            pageId = SchemePage.id
        ),
        SettingItem.Switch(
            meta = SettingMeta(R.string.page_settings_developer_options,
                visible = { developerOptionsEnabled }
            ),
            pageId = DeveloperOptionsPage.id,
            onCheckedChange = { developerOptionsEnabled = it },
            setting = Settings.SHOW_DEVELOPER_OPTIONS
        ),
        SettingItem.Divider(),
        SettingItem.ActivityLink(
            meta = SettingMeta(R.string.settings_menu_open_source_credits),
            activity = CreditsActivity::class.java
        ),
        SettingItem.UriLink(
            meta = SettingMeta(R.string.settings_menu_view_source),
            uri = "https://github.com/cognitivitydev/chronal".toUri()
        ),
        SettingItem.Divider(),
        SettingItem.PageLink(
            meta = SettingMeta(R.string.page_settings_feedback),
            pageId = FeedbackPage.id
        ),
        SettingItem.ActivityLink(
            meta = SettingMeta(R.string.help_title),
            activity = HelpActivity::class.java
        ),
        SettingItem.UriLink(
            meta = SettingMeta(R.string.settings_footer_privacy_policy),
            uri = PRIVACY_POLICY_URI
        ),
        SettingItem.Divider(),
        SettingItem.UriLink(
            meta = SettingMeta(R.string.settings_support_project,
                icon = R.drawable.outline_volunteer_activism_24
            ),
            uri = "https://ko-fi.com/cognitivity".toUri()
        )
    )
)