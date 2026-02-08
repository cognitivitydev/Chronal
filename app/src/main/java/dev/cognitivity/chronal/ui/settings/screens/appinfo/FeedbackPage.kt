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

package dev.cognitivity.chronal.ui.settings.screens.appinfo

import androidx.core.net.toUri
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.ui.settings.items.SettingItem
import dev.cognitivity.chronal.ui.settings.items.SettingMeta
import dev.cognitivity.chronal.ui.settings.data.SettingsPage

private val GITHUB_ISSUES_URI = "https://github.com/cognitivity/chronal/issues".toUri()
private val CROWDIN_URI = "https://crowdin.com/project/chronal".toUri()
private val REVIEW_URI = "https://play.google.com/store/apps/details?id=dev.cognitivity.chronal&reviewId=0".toUri()
private val EMAIL_URI = "mailto:cognitivitydev@gmail.com".toUri()

object FeedbackPage : SettingsPage(
    id = "feedback",
    title = R.string.page_settings_feedback,
    items = listOf(
        SettingItem.UriLink(
            meta = SettingMeta(R.string.settings_feedback_open_issue_title, R.string.settings_feedback_open_issue_text,
                icon = R.drawable.outline_bug_report_24
            ),
            uri = GITHUB_ISSUES_URI
        ),
        SettingItem.UriLink(
            meta = SettingMeta(R.string.settings_feedback_translate_title, R.string.settings_feedback_translate_text,
                icon = R.drawable.outline_public_24
            ),
            uri = CROWDIN_URI
        ),
        SettingItem.UriLink(
            meta = SettingMeta(R.string.settings_feedback_review_title, R.string.settings_feedback_review_text,
                icon = R.drawable.outline_rate_review_24
            ),
            uri = REVIEW_URI
        ),
        SettingItem.UriLink(
            meta = SettingMeta(R.string.settings_feedback_email_title, R.string.settings_feedback_email_text,
                icon = R.drawable.outline_mail_24
            ),
            uri = EMAIL_URI
        )
    )
)