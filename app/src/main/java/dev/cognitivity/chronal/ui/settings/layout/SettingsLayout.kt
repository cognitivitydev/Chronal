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

package dev.cognitivity.chronal.ui.settings.layout

import dev.cognitivity.chronal.ui.settings.categories.AppInfoCategory
import dev.cognitivity.chronal.ui.settings.categories.MetronomeCategory
import dev.cognitivity.chronal.ui.settings.categories.NotificationsCategory
import dev.cognitivity.chronal.ui.settings.categories.TunerCategory
import dev.cognitivity.chronal.ui.settings.data.SettingsCategory
import dev.cognitivity.chronal.ui.settings.data.SettingsPage
import dev.cognitivity.chronal.ui.settings.screens.appinfo.DeveloperOptionsPage
import dev.cognitivity.chronal.ui.settings.screens.appinfo.FeedbackPage
import dev.cognitivity.chronal.ui.settings.screens.appinfo.SchemePage
import dev.cognitivity.chronal.ui.settings.screens.notifications.PracticeReminderPage
import dev.cognitivity.chronal.ui.settings.screens.tuner.A4FrequencyPage
import dev.cognitivity.chronal.ui.settings.screens.tuner.AccidentalsPage
import dev.cognitivity.chronal.ui.settings.screens.tuner.NoteLocalePage

object SettingsLayout {
    val categories = listOf(
        MetronomeCategory,
        TunerCategory,
        NotificationsCategory,
        AppInfoCategory
    )

    val pages = listOf(
        A4FrequencyPage,
        NoteLocalePage,
        AccidentalsPage,
        FeedbackPage,
        DeveloperOptionsPage,
        SchemePage,
        PracticeReminderPage
    )

    fun categoryOf(id: String): SettingsCategory = categories.find { it.id == id } ?: error("Unknown category: $id")

    fun pageOf(id: String): SettingsPage = pages.find { it.id == id } ?: error("Unknown page: $id")
}