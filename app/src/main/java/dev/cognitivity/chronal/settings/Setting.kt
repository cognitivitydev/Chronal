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
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dev.cognitivity.chronal.metronome.sound.SoundPack
import dev.cognitivity.chronal.settings.types.json.MetronomeConfig
import dev.cognitivity.chronal.settings.types.json.MetronomeConfigTrack
import dev.cognitivity.chronal.settings.types.json.SimpleRhythm
import dev.cognitivity.chronal.settings.types.json.TempoMarking
import dev.cognitivity.chronal.settings.types.json.TrackColor
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
            val version = Settings.getCurrentVersionName()
            val versionCode = Settings.getCurrentVersionCode()

            suspend fun load() {
                val prefs = dataStore.data.first()
                SETTINGS.forEach { it.load(prefs) }
                Settings.VERSION.set(version)
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
            if(fromVersion < 12) {
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

                // update tempo markings
                val markings = Settings.TEMPO_MARKINGS.get()
                val updatedMarkings = mutableListOf<TempoMarking>()
                for (marking in markings) {
                    if (marking.range.last == 500) {
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

            // Version 13:
            // > Split rhythms into tracks
            // > Combined metronome settings into a single JSON object
            // > Moved presets to the new MetronomeConfig structure
            // > Moved sounds to track-based sound packs
            if(fromVersion < 13) {
                val configKey = stringPreferencesKey(Settings.METRONOME_CONFIG.key)

                // convert legacy sound
                val newSoundPack = prefs[stringPreferencesKey("metronome_sounds")]?.let { raw ->
                    try {
                        val json = Gson().fromJson(raw, JsonArray::class.java)
                        val highId = if (json.size() > 0) json[0].asInt else 0
                        when (highId) {
                            0 -> SoundPack.BUILTIN_CLICK.id
                            1 -> SoundPack.BUILTIN_SINE.id
                            2 -> SoundPack.BUILTIN_SQUARE.id
                            3 -> SoundPack.BUILTIN_CLAP.id
                            4 -> SoundPack.BUILTIN_BELL.id
                            5 -> SoundPack.BUILTIN_TAMBOURINE.id
                            6 -> SoundPack.BUILTIN_BLOCK.id
                            else -> SoundPack.DEFAULT_ID
                        }
                    } catch (_: Exception) {
                        SoundPack.DEFAULT_ID
                    }
                } ?: SoundPack.DEFAULT_ID

                // update rhythm
                if(prefs[configKey] == null) {
                    val bpm = prefs[stringPreferencesKey("metronome_state")]?.let {
                        try {
                            Gson().fromJson(it, JsonObject::class.java).get("bpm")?.asFloat ?: 120f
                        } catch (_: Exception) {
                            120f
                        }
                    } ?: 120f

                    val migratedConfig = MetronomeConfig(
                        version = 2,
                        bpm = bpm,
                        tracks = listOf(
                            MetronomeConfigTrack(
                                name = "Primary track",
                                enabled = true,
                                vibrate = prefs[booleanPreferencesKey("metronome_vibrations")] ?: true,
                                rhythm = prefs[stringPreferencesKey("metronome_rhythm")] ?: "{4/4}Q;q;q;q;",
                                beatValue = prefs[stringPreferencesKey("metronome_beat_value")]?.toFloatOrNull() ?: 4f,
                                simpleRhythm = prefs[stringPreferencesKey("metronome_simple_rhythm")]?.let {
                                    try {
                                        SimpleRhythm.fromJson(it)
                                    } catch (_: Exception) {
                                        SimpleRhythm(4 to 4, 4, 2)
                                    }
                                } ?: SimpleRhythm(4 to 4, 4, 2),
                                color = TrackColor.Primary,
                                soundPackId = newSoundPack,
                            ),
                            MetronomeConfigTrack(
                                name = "Secondary track",
                                enabled = prefs[booleanPreferencesKey("metronome_secondary_enabled")] ?: false,
                                vibrate = prefs[booleanPreferencesKey("metronome_vibrations_secondary")] ?: true,
                                rhythm = prefs[stringPreferencesKey("metronome_rhythm_secondary")] ?: "{4/4}Q;q;q;q;",
                                beatValue = prefs[stringPreferencesKey("metronome_beat_value_secondary")]?.toFloatOrNull() ?: 4f,
                                simpleRhythm = prefs[stringPreferencesKey("metronome_simple_rhythm_secondary")]?.let {
                                    try {
                                        SimpleRhythm.fromJson(it)
                                    } catch (_: Exception) {
                                        SimpleRhythm(4 to 4, 4, 2)
                                    }
                                } ?: SimpleRhythm(4 to 4, 4, 2),
                                color = TrackColor.Secondary,
                                soundPackId = newSoundPack,
                            )
                        )
                    )
                    prefs[configKey] = migratedConfig.toJson().toString()
                    Log.d("Setting", "(v13) Migrated rhythms to MetronomeConfig format")
                }

                // update presets
                val presetsString = prefs[stringPreferencesKey(Settings.METRONOME_PRESETS.key)]
                val presetsJson = Gson().fromJson(presetsString, JsonArray::class.java)
                val newPresets = JsonArray()

                for(preset in presetsJson.map { it.asJsonObject} ) {
                    if(preset.has("config")) continue
                    val timestamp = preset["timestamp"].asLong
                    val name = preset["name"].asString
                    val primaryRhythm = preset["primaryRhythm"].asString
                    val primarySimpleRhythm = preset["primarySimpleRhythm"].asJsonObject
                    val secondaryRhythm = preset["secondaryRhythm"].asString
                    val secondarySimpleRhythm = preset["secondarySimpleRhythm"].asJsonObject
                    val state = preset["state"].asJsonObject
                    val bpm = state["bpm"].asFloat
                    val primaryBeatValue = state["valuePrimary"].asFloat
                    val secondaryBeatValue = state["valueSecondary"].asFloat
                    val secondaryEnabled = state["secondaryEnabled"].asBoolean

                    val migratedConfig = MetronomeConfig(
                        version = 13,
                        bpm = bpm,
                        tracks = listOf(
                            MetronomeConfigTrack(
                                name = "Primary track",
                                enabled = true,
                                vibrate = true,
                                rhythm = primaryRhythm,
                                beatValue = primaryBeatValue,
                                simpleRhythm = SimpleRhythm.fromJson(primarySimpleRhythm),
                                color = TrackColor.Primary,
                                soundPackId = newSoundPack,
                            ),
                            MetronomeConfigTrack(
                                name = "Secondary track",
                                enabled = secondaryEnabled,
                                vibrate = true,
                                rhythm = secondaryRhythm,
                                beatValue = secondaryBeatValue,
                                simpleRhythm = SimpleRhythm.fromJson(secondarySimpleRhythm),
                                color = TrackColor.Secondary,
                                soundPackId = newSoundPack,
                            )
                        )
                    )

                    val newPreset = JsonObject()
                    newPreset.addProperty("timestamp", timestamp)
                    newPreset.addProperty("name", name)
                    newPreset.add("config", migratedConfig.toJson())

                    newPresets.add(newPreset)
                }
                Log.d("Setting", "(v13) Migrated presets to MetronomeConfig format")
            }
        }
    }
}