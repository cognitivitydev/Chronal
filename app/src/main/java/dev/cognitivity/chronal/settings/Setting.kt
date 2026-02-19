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

package dev.cognitivity.chronal.settings

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.settings.types.json.TempoMarking
import kotlinx.coroutines.flow.first

abstract class Setting<T>(
    val key: String,
    val defaultValue: T
) {
    protected var value: T = defaultValue

    init {
        if (SETTINGS.any { it.key == key }) {
            throw IllegalArgumentException("Duplicate Setting key: $key")
        }
        SETTINGS.add(this)
    }

    fun get(): T = value

    fun set(newValue: T) {
        value = newValue
    }

    protected abstract fun write(editor: MutablePreferences)

    suspend fun save() {
        dataStore.edit { editor ->
            write(editor)
        }
    }
    suspend fun save(newValue: T) {
        value = newValue
        save()
    }

    abstract suspend fun load(prefs: Preferences)

    abstract fun import(json: JsonObject)
    abstract fun export(json: JsonObject)


    companion object {
        val Context.dataStore by preferencesDataStore("settings")
        lateinit var dataStore: DataStore<Preferences>

        fun init(context: Context) {
            dataStore = context.dataStore
        }

        private val SETTINGS = mutableListOf<Setting<*>>()

        fun all(): List<Setting<*>> = SETTINGS

        suspend fun saveAll() {
            dataStore.edit { editor ->
                SETTINGS.forEach { it.write(editor) }
            }
        }

        suspend fun loadAll() {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionName = packageInfo.versionName
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                packageInfo.versionCode
            }


            suspend fun load() {
                Settings // initialize objects

                val prefs = dataStore.data.first()
                SETTINGS.forEach { it.load(prefs) }
                Settings.VERSION.set(versionName ?: "0.0.0")
                Settings.VERSION_CODE.set(versionCode)
            }

            if(dataStore.data.first().asMap().isEmpty()) { // first launch
                load()
                return
            }

            // check for updates
            dataStore.edit { prefs ->
                val prevVersion = prefs[intPreferencesKey("version_code")] ?: -1
                if (prevVersion <= versionCode) {
                    Log.i("Setting", "App has been updated ($prevVersion > $versionCode), attempting migration")
                    migrate(prefs, prevVersion)
                    prefs[intPreferencesKey("version_code")] = versionCode
                }
            }
            load()
        }

        fun exportToJson(): JsonObject {
            val json = JsonObject()
            for (setting in SETTINGS) {
                setting.export(json)
            }
            return json
        }

        fun importFromJson(json: JsonObject) {
            for (setting in SETTINGS) {
                setting.import(json)
                Log.d("Setting", "Imported setting ${setting.key} with value ${setting.get()}")
            }
        }


        fun migrate(prefs: MutablePreferences, fromVersion: Int) {
            // Version 12:
            // > Reworked settings structure - "12345678_SETTING_NAME" to "setting_name"
            // > Updated BPM limit (500 -> 800) - update max tempo markings
            if(fromVersion < 15) {
                val keysToChange = prefs.asMap().keys.filter { it.name.matches(Regex("\\d+_.*")) }
                for(key in keysToChange) {
                    val newKey = key.name.substringAfter("_").lowercase()
                    when(val value = prefs[key]) {
                        is Boolean -> { prefs[booleanPreferencesKey(newKey)] = value }
                        is Float -> { prefs[floatPreferencesKey(newKey)] = value }
                        is Int -> { prefs[intPreferencesKey(newKey)] = value }
                        is String -> { prefs[stringPreferencesKey(newKey)] = value }
                    }
                    Log.d("Setting", "(v12) Migrated setting ${key.name} to $newKey")
                    prefs.remove(key)
                }
            }
            // update tempo markings
            val markings = Settings.TEMPO_MARKINGS.get()
            val updatedMarkings = mutableListOf<TempoMarking>()
            for(marking in markings) {
                if(marking.range.last == 500) {
                    updatedMarkings.add(marking.copy(range = marking.range.first..800))
                } else {
                    updatedMarkings.add(marking)
                }
            }
            prefs[stringPreferencesKey(Settings.TEMPO_MARKINGS.key)] = JsonArray().apply {
                markings.forEach { add(it.toJson()) }
            }.toString()
            Log.d("Setting", "(v12) Updated tempo markings to 800")
        }
    }
}