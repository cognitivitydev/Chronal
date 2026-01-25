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
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.cognitivity.chronal.settings.Setting

class JsonSetting<T>(
    key: String,
    defaultValue: T,
    private val serializer: (T) -> JsonElement,
    private val deserializer: (JsonElement) -> T,
) : Setting<T>(key, defaultValue) {
    private val prefKey = stringPreferencesKey(key)

    override suspend fun load(prefs: Preferences) {
        value = prefs[prefKey]?.let {
            try {
                deserializer(JsonParser.parseString(it))
            } catch (_: Exception) {
                defaultValue
            }
        } ?: defaultValue
    }

    override fun write(editor: MutablePreferences) {
        editor[prefKey] = serializer(value).toString()
    }

    override fun import(json: JsonObject) {
        json.get(key)?.let {
            value = deserializer(it)
        }
    }

    override fun export(json: JsonObject) {
        json.add(key, serializer(value))
    }
}