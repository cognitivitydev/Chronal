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

package dev.cognitivity.chronal.settings.types

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.google.gson.JsonObject
import dev.cognitivity.chronal.settings.Setting

class BooleanSetting(
    key: String,
    defaultValue: Boolean,
) : Setting<Boolean>(key, defaultValue) {
    private val prefKey = booleanPreferencesKey(key)

    override suspend fun load(prefs: Preferences) {
        value = prefs[prefKey] ?: defaultValue
    }

    override fun write(editor: MutablePreferences) {
        editor[prefKey] = value
    }

    override fun import(json: JsonObject) {
        value = json.get(key)?.asBoolean ?: value
    }

    override fun export(json: JsonObject) {
        json.addProperty(key, value)
    }
}