/*
 * Chronal: Metronome app for Android
 * Copyright (C) 2025  cognitivity
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

package dev.cognitivity.chronal

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dev.cognitivity.chronal.rhythm.metronome.Rhythm
import kotlinx.coroutines.flow.first

enum class SettingKey(val category: Int, val settingName: Int) {
    COLOR_SCHEME(R.string.setting_category_general, R.string.setting_name_color_scheme),
    PRIMARY_INSTRUMENT(R.string.setting_category_general, R.string.setting_name_primary_instrument),

    VISUAL_LATENCY(R.string.setting_category_metronome, R.string.setting_name_visual_latency),
    SHOW_BEATS(R.string.setting_category_metronome, R.string.setting_name_show_beats),
    SHOW_SUBDIVISIONS(R.string.setting_category_metronome, R.string.setting_name_show_subdivisions),
    HIGH_CONTRAST(R.string.setting_category_metronome, R.string.setting_name_high_contrast),
    NO_ANIMATION(R.string.setting_category_metronome, R.string.setting_name_no_animation),
    TEMPO_MARKINGS(R.string.setting_category_metronome, R.string.setting_name_tempo_markings),

    TUNER_FREQUENCY(R.string.setting_category_tuner, R.string.setting_name_tuner_frequency),
    SHOW_OCTAVE(R.string.setting_category_tuner, R.string.setting_name_show_octave),
    TRANSPOSE_NOTES(R.string.setting_category_tuner, R.string.setting_name_transpose_notes),
    AUDIO_THRESHOLD(R.string.setting_category_tuner, R.string.setting_name_audio_threshold),
    ACCIDENTALS(R.string.setting_category_tuner, R.string.setting_name_accidentals),
    NOTE_NAMES(R.string.setting_category_tuner, R.string.setting_name_note_names),

    SHOW_DEVELOPER_OPTIONS(R.string.setting_category_internal, R.string.setting_name_show_developer_options),
    METRONOME_RHYTHM(R.string.setting_category_internal, R.string.setting_name_metronome_rhythm),
    METRONOME_SIMPLE_RHYTHM(R.string.setting_category_internal, R.string.setting_name_metronome_simple_rhythm),
    METRONOME_VIBRATIONS(R.string.setting_category_internal, R.string.setting_name_metronome_vibrations),
    METRONOME_RHYTHM_SECONDARY(R.string.setting_category_internal, R.string.setting_name_metronome_rhythm_secondary),
    METRONOME_SIMPLE_RHYTHM_SECONDARY(R.string.setting_category_internal, R.string.setting_name_metronome_simple_rhythm_secondary),
    METRONOME_VIBRATIONS_SECONDARY(R.string.setting_category_internal, R.string.setting_name_metronome_vibrations_secondary),
    METRONOME_SOUNDS(R.string.setting_category_internal, R.string.setting_name_metronome_sounds),
    METRONOME_STATE(R.string.setting_category_internal, R.string.setting_name_metronome_state),
    METRONOME_PRESETS(R.string.setting_category_internal, R.string.setting_name_metronome_presets),
    FULLSCREEN_WARNING(R.string.setting_category_internal, R.string.setting_name_fullscreen_warning),
    TUNER_LAYOUT(R.string.setting_category_internal, R.string.setting_name_tuner_layout),
    RAW(R.string.setting_category_internal, R.string.setting_name_raw)
    ;

    override fun toString(): String {
        return "${category}_$name"
    }
}

data class SettingMenu(val type: String, val id: String) {
    companion object {
        fun Launch(id: String): SettingMenu {
            return SettingMenu("Launch", id)
        }
        fun Expandable(id: String): SettingMenu {
            return SettingMenu("Expandable", id)
        }
    }
}

val Context.dataStore by preferencesDataStore("settings")

class SettingsManager(val context: Context) {
    /******* GENERAL *******/
    val colorScheme = Setting(
        SettingKey.COLOR_SCHEME,
        hint = R.string.setting_description_color_scheme,
        menu = SettingMenu.Expandable("Colors"),
        default = ColorScheme(
            color = ColorScheme.Color.SYSTEM,
            theme = ColorScheme.Theme.SYSTEM,
            contrast = ColorScheme.Contrast.SYSTEM
        )
    )
    val primaryInstrument = Setting(
        SettingKey.PRIMARY_INSTRUMENT,
        hint = R.string.setting_description_primary_instrument,
        menu = SettingMenu.Launch("Instrument"),
        default = Instrument("B♭ Trumpet", "B♭ Tpt.", -1)
    )

    /******* METRONOME *******/
    val visualLatency = Setting(
        SettingKey.VISUAL_LATENCY,
        hint = R.string.setting_description_visual_latency,
        menu = SettingMenu.Expandable("Latency"),
        default = 100
    )
    val showBeats = Setting(
        SettingKey.SHOW_BEATS,
        hint = R.string.setting_description_show_beats,
        default = true
    )
    val showSubdivisions = Setting(
        SettingKey.SHOW_SUBDIVISIONS,
        hint = R.string.setting_description_show_subdivisions,
        default = true
    )
    val highContrast = Setting(
        SettingKey.HIGH_CONTRAST,
        hint = R.string.setting_description_high_contrast,
        default = false
    )
    val noAnimation = Setting(
        SettingKey.NO_ANIMATION,
        hint = R.string.setting_description_no_animation,
        default = false
    )
    val tempoMarkings = Setting(
        SettingKey.TEMPO_MARKINGS,
        hint = R.string.setting_description_tempo_markings,
        menu = SettingMenu.Launch("Markings"),
        default = mutableListOf(
            TempoMarking("Larghissimo", 1..24),
            TempoMarking("Grave", 25..39),
            TempoMarking("Lento", 40..49),
            TempoMarking("Largo", 50..59),
            TempoMarking("Larghetto", 60..66),
            TempoMarking("Adagio", 67..76),
            TempoMarking("Andante", 77..108),
            TempoMarking("Moderato", 109..120),
            TempoMarking("Allegretto", 121..132),
            TempoMarking("Allegro", 133..143),
            TempoMarking("Vivace", 144..159),
            TempoMarking("Presto", 160..199),
            TempoMarking("Prestissimo", 200..500)
        )
    )

    /******* TUNER *******/
    val tunerFrequency = Setting(
        SettingKey.TUNER_FREQUENCY,
        hint = R.string.setting_description_tuner_frequency,
        menu = SettingMenu.Expandable("Frequency"),
        default = 440
    )
    val showOctave = Setting(
        SettingKey.SHOW_OCTAVE,
        hint = R.string.setting_description_show_octave,
        default = false
    )
    val transposeNotes = Setting(
        SettingKey.TRANSPOSE_NOTES,
        hint = R.string.setting_description_transpose_notes,
        default = false
    )
    val audioThreshold = Setting(
        SettingKey.AUDIO_THRESHOLD,
        hint = R.string.setting_description_audio_threshold,
        menu = SettingMenu.Expandable("Percentage"),
        default = 0.5f
    )
    val accidentals = Setting(
        SettingKey.ACCIDENTALS,
        hint = R.string.setting_description_accidentals,
        menu = SettingMenu.Expandable("Accidentals"),
        default = 2
    )
    val noteNames = Setting(
        SettingKey.NOTE_NAMES,
        hint = R.string.setting_description_note_names,
        menu = SettingMenu.Expandable("Note"),
        default = 0
    )

    /******* INTERNAL *******/
    val showDeveloperOptions = Setting(
        SettingKey.SHOW_DEVELOPER_OPTIONS,
        hint = R.string.setting_description_show_developer_options,
        default = false
    )
    val metronomeRhythm = Setting(
        SettingKey.METRONOME_RHYTHM,
        hint = R.string.setting_description_metronome_rhythm,
        default = "{4/4}Q;q;q;q;"
    )
    val metronomeSimpleRhythm = Setting(
        SettingKey.METRONOME_SIMPLE_RHYTHM,
        hint = R.string.setting_description_metronome_simple_rhythm,
        default = SimpleRhythm(4 to 4, 4, 2)
    )
    val metronomeVibrations = Setting(
        SettingKey.METRONOME_VIBRATIONS,
        hint = R.string.setting_description_metronome_vibrations,
        default = true
    )
    val metronomeRhythmSecondary = Setting(
        SettingKey.METRONOME_RHYTHM_SECONDARY,
        hint = R.string.setting_description_metronome_rhythm_secondary,
        default = "{4/4}Q;q;q;q;"
    )
    val metronomeSimpleRhythmSecondary = Setting(
        SettingKey.METRONOME_SIMPLE_RHYTHM_SECONDARY,
        hint = R.string.setting_description_metronome_simple_rhythm_secondary,
        default = SimpleRhythm(4 to 4, 4, 2)
    )
    val metronomeVibrationsSecondary = Setting(
        SettingKey.METRONOME_VIBRATIONS_SECONDARY,
        hint = R.string.setting_description_metronome_vibrations_secondary,
        default = true
    )
    val metronomeSounds = Setting(
        SettingKey.METRONOME_SOUNDS,
        hint = R.string.setting_description_metronome_sounds,
        default = 0 to 0
    )
    val metronomeState = Setting(
        SettingKey.METRONOME_STATE,
        hint = R.string.setting_description_metronome_state,
        default = MetronomeState(bpm = 120f, beatValuePrimary = 4f, beatValueSecondary = 4f, secondaryEnabled = false)
    )
     val metronomePresets = Setting(
         SettingKey.METRONOME_PRESETS,
         hint = R.string.setting_description_metronome_presets,
         menu = SettingMenu.Expandable("Presets"),
         default = mutableListOf(
             MetronomePreset(
                 name = context.getString(R.string.presets_example_default),
                 primaryRhythm = Rhythm.deserialize("{4/4}Q;q;q;q;"),
                 primarySimpleRhythm = SimpleRhythm(2 to 4, 4, 3),
                 secondaryRhythm = Rhythm.deserialize("{4/4}Q;q;q;q;"),
                 secondarySimpleRhythm = SimpleRhythm(4 to 4, 4, 1),
                 state = MetronomeState(bpm = 120f, beatValuePrimary = 4f, beatValueSecondary = 4f, secondaryEnabled = true)
             ),
             MetronomePreset(
                 name = context.getString(R.string.presets_example_3_2),
                 primaryRhythm = Rhythm.deserialize("{2/4}Q;Q;"),
                 primarySimpleRhythm = SimpleRhythm(2 to 4, 4, 0),
                 secondaryRhythm = Rhythm.deserialize("{2/4}3:2[q:q:q];"),
                 secondarySimpleRhythm = SimpleRhythm(2 to 4, 6, 1),
                 state = MetronomeState(bpm = 120f, beatValuePrimary = 4f, beatValueSecondary = 4f, secondaryEnabled = true)
             ),
         )
     )
    val fullscreenWarning = Setting(
        SettingKey.FULLSCREEN_WARNING,
        hint = R.string.setting_description_fullscreen_warning,
        default = true
    )
    val tunerLayout = Setting(
        SettingKey.TUNER_LAYOUT,
        hint = R.string.setting_description_tuner_layout,
        menu = SettingMenu.Expandable("Percentage"),
        default = 0.33f
    )
    val raw = Setting( // view only, not written
        SettingKey.RAW,
        hint = R.string.setting_description_raw,
        menu = SettingMenu.Expandable("Raw"),
        default = null
    )


    val keyMap: Map<SettingKey, Setting<*>> = mapOf(
        /******* GENERAL *******/
        SettingKey.COLOR_SCHEME to colorScheme,
        SettingKey.PRIMARY_INSTRUMENT to primaryInstrument,

        /******* METRONOME *******/
        SettingKey.VISUAL_LATENCY to visualLatency,
        SettingKey.SHOW_BEATS to showBeats,
        SettingKey.SHOW_SUBDIVISIONS to showSubdivisions,
        SettingKey.HIGH_CONTRAST to highContrast,
        SettingKey.NO_ANIMATION to noAnimation,
        SettingKey.TEMPO_MARKINGS to tempoMarkings,

        /******* TUNER *******/
        SettingKey.TUNER_FREQUENCY to tunerFrequency,
        SettingKey.SHOW_OCTAVE to showOctave,
        SettingKey.TRANSPOSE_NOTES to transposeNotes,
        SettingKey.AUDIO_THRESHOLD to audioThreshold,
        SettingKey.ACCIDENTALS to accidentals,
        SettingKey.NOTE_NAMES to noteNames,

        /******* INTERNAL *******/
        SettingKey.SHOW_DEVELOPER_OPTIONS to showDeveloperOptions,
        SettingKey.METRONOME_RHYTHM to metronomeRhythm,
        SettingKey.METRONOME_SIMPLE_RHYTHM to metronomeSimpleRhythm,
        SettingKey.METRONOME_RHYTHM_SECONDARY to metronomeRhythmSecondary,
        SettingKey.METRONOME_SIMPLE_RHYTHM_SECONDARY to metronomeSimpleRhythmSecondary,
        SettingKey.METRONOME_SOUNDS to metronomeSounds,
        SettingKey.FULLSCREEN_WARNING to fullscreenWarning,
        SettingKey.TUNER_LAYOUT to tunerLayout,
        SettingKey.RAW to raw
    )

    suspend fun save() {
        context.dataStore.edit { settings ->
            /******* GENERAL *******/
            val primaryInstrumentKey = stringPreferencesKey(primaryInstrument.key.toString())
            settings[primaryInstrumentKey] = primaryInstrument.value.toJson().toString()

            val colorSchemeKey = stringPreferencesKey(colorScheme.key.toString())
            settings[colorSchemeKey] = colorScheme.value.toJson().toString()

            /******* METRONOME *******/
            val visualLatencyKey = intPreferencesKey(visualLatency.key.toString())
            settings[visualLatencyKey] = visualLatency.value

            val showBeatsKey = booleanPreferencesKey(showBeats.key.toString())
            settings[showBeatsKey] = showBeats.value

            val showSubdivisionsKey = booleanPreferencesKey(showSubdivisions.key.toString())
            settings[showSubdivisionsKey] = showSubdivisions.value

            val highContrastKey = booleanPreferencesKey(highContrast.key.toString())
            settings[highContrastKey] = highContrast.value

            val noAnimationKey = booleanPreferencesKey(noAnimation.key.toString())
            settings[noAnimationKey] = noAnimation.value

            val tempoMarkingsKey = stringPreferencesKey(tempoMarkings.key.toString())
            settings[tempoMarkingsKey] = Gson().toJson(tempoMarkings.value.map { it.toJson() })

            /******* TUNER *******/
            val tunerFrequencyKey = intPreferencesKey(tunerFrequency.key.toString())
            settings[tunerFrequencyKey] = tunerFrequency.value

            val showOctaveKey = booleanPreferencesKey(showOctave.key.toString())
            settings[showOctaveKey] = showOctave.value

            val transposeNotesKey = booleanPreferencesKey(transposeNotes.key.toString())
            settings[transposeNotesKey] = transposeNotes.value

            val audioThresholdKey = floatPreferencesKey(audioThreshold.key.toString())
            settings[audioThresholdKey] = audioThreshold.value

            val accidentalsKey = intPreferencesKey(accidentals.key.toString())
            settings[accidentalsKey] = accidentals.value

            val noteNamesKey = intPreferencesKey(noteNames.key.toString())
            settings[noteNamesKey] = noteNames.value

            /******* INTERNAL *******/
            val showDeveloperOptionsKey = booleanPreferencesKey(showDeveloperOptions.key.toString())
            settings[showDeveloperOptionsKey] = showDeveloperOptions.value

            val metronomeRhythmKey = stringPreferencesKey(metronomeRhythm.key.toString())
            settings[metronomeRhythmKey] = metronomeRhythm.value

            val metronomeSimpleRhythmKey = stringPreferencesKey(metronomeSimpleRhythm.key.toString())
            settings[metronomeSimpleRhythmKey] = metronomeSimpleRhythm.value.toJson().toString()

            val metronomeRhythmSecondaryKey = stringPreferencesKey(metronomeRhythmSecondary.key.toString())
            settings[metronomeRhythmSecondaryKey] = metronomeRhythmSecondary.value

            val metronomeSimpleRhythmSecondaryKey = stringPreferencesKey(metronomeSimpleRhythmSecondary.key.toString())
            settings[metronomeSimpleRhythmSecondaryKey] = metronomeSimpleRhythmSecondary.value.toJson().toString()

            val metronomeSoundsKey = stringPreferencesKey(metronomeSounds.key.toString())
            settings[metronomeSoundsKey] = "${metronomeSounds.value.first},${metronomeSounds.value.second}"

            val metronomeStateKey = stringPreferencesKey(metronomeState.key.toString())
            settings[metronomeStateKey] = metronomeState.value.toJson().toString()

            val metronomePresetsKey = stringPreferencesKey(metronomePresets.key.toString())
            settings[metronomePresetsKey] = Gson().toJson(metronomePresets.value.map { it.toJson() })

            val fullscreenWarningKey = booleanPreferencesKey(fullscreenWarning.key.toString())
            settings[fullscreenWarningKey] = fullscreenWarning.value

            val tunerLayoutKey = floatPreferencesKey(tunerLayout.key.toString())
            settings[tunerLayoutKey] = tunerLayout.value
        }
    }

    suspend fun load() {
        val prefs = context.dataStore.data.first()

        /******* GENERAL *******/
        colorScheme.value = prefs[stringPreferencesKey(colorScheme.key.toString())]
            ?.let { ColorScheme.fromJson(it) } ?: colorScheme.default

        primaryInstrument.value = prefs[stringPreferencesKey(primaryInstrument.key.toString())]
            ?.let { Instrument.fromJson(it) } ?: primaryInstrument.default

        /******* METRONOME *******/
        visualLatency.value = prefs[intPreferencesKey(visualLatency.key.toString())]
            ?: visualLatency.default

        showBeats.value = prefs[booleanPreferencesKey(showBeats.key.toString())]
            ?: showBeats.default

        showSubdivisions.value = prefs[booleanPreferencesKey(showSubdivisions.key.toString())]
            ?: showSubdivisions.default

        highContrast.value = prefs[booleanPreferencesKey(highContrast.key.toString())]
            ?: highContrast.default

        noAnimation.value = prefs[booleanPreferencesKey(noAnimation.key.toString())]
            ?: noAnimation.default

        tempoMarkings.value = prefs[stringPreferencesKey(tempoMarkings.key.toString())]
            ?.let { json ->
                val jsonArray = Gson().fromJson(json, JsonArray::class.java)
                jsonArray.map { item -> TempoMarking.fromJson(item.asJsonObject) }.toMutableList()
            } ?: tempoMarkings.default

        /******* TUNER *******/
        tunerFrequency.value = prefs[intPreferencesKey(tunerFrequency.key.toString())]
            ?: tunerFrequency.default

        showOctave.value = prefs[booleanPreferencesKey(showOctave.key.toString())]
            ?: showOctave.default

        transposeNotes.value = prefs[booleanPreferencesKey(transposeNotes.key.toString())]
            ?: transposeNotes.default

        audioThreshold.value = prefs[floatPreferencesKey(audioThreshold.key.toString())]
            ?: audioThreshold.default

        accidentals.value = prefs[intPreferencesKey(accidentals.key.toString())]
            ?: accidentals.default

        noteNames.value = prefs[intPreferencesKey(noteNames.key.toString())]
            ?: noteNames.default


        /******* INTERNAL *******/
        showDeveloperOptions.value = prefs[booleanPreferencesKey(showDeveloperOptions.key.toString())]
            ?: showDeveloperOptions.default

        metronomeRhythm.value = prefs[stringPreferencesKey(metronomeRhythm.key.toString())]
            ?: metronomeRhythm.default

        metronomeSimpleRhythm.value = prefs[stringPreferencesKey(metronomeSimpleRhythm.key.toString())]
            ?.let { SimpleRhythm.fromJson(it) } ?: metronomeSimpleRhythm.default

        metronomeRhythmSecondary.value = prefs[stringPreferencesKey(metronomeRhythmSecondary.key.toString())]
            ?: metronomeRhythmSecondary.default

        metronomeSimpleRhythmSecondary.value = prefs[stringPreferencesKey(metronomeSimpleRhythmSecondary.key.toString())]
            ?.let { SimpleRhythm.fromJson(it) } ?: metronomeSimpleRhythmSecondary.default

        metronomeSounds.value = prefs[stringPreferencesKey(metronomeSounds.key.toString())]?.let {
            val parts = it.split(",")
            if (parts.size == 2) (parts[0].toIntOrNull() ?: 0) to (parts[1].toIntOrNull() ?: 0 ) else null
        } ?: metronomeSounds.default

        metronomeState.value = prefs[stringPreferencesKey(metronomeState.key.toString())]
            ?.let { MetronomeState.fromJson(it) } ?: metronomeState.default

        metronomePresets.value = prefs[stringPreferencesKey(metronomePresets.key.toString())]?.let {
            val jsonArray = Gson().fromJson(it, JsonArray::class.java)
            jsonArray.map { preset -> MetronomePreset.fromJson(preset.asJsonObject) }.toCollection(ArrayList())
        } ?: metronomePresets.default

        fullscreenWarning.value = prefs[booleanPreferencesKey(fullscreenWarning.key.toString())]
            ?: fullscreenWarning.default

        tunerLayout.value = prefs[floatPreferencesKey(tunerLayout.key.toString())]
            ?: tunerLayout.default
    }

    suspend fun toJson(): JsonObject {
        val prefs = context.dataStore.data.first()
        val jsonObject = Gson().toJsonTree(prefs).asJsonObject
        return jsonObject
    }

    fun get(key: SettingKey): Setting<*> {
        return keyMap[key] ?: throw IllegalArgumentException("Setting not found: $key")
    }
}

data class Setting<T>(val key: SettingKey, val hint: Int, val default: T,
                  val menu: SettingMenu? = null, var value: T = default)

data class Instrument(
    var name: String,
    var shortened: String,
    var transposition: Int
) {
    companion object {
        fun fromJson(jsonObject: JsonObject): Instrument {
            return Instrument(
                name = jsonObject.get("name").asString,
                shortened = jsonObject.get("shortened").asString,
                transposition = jsonObject.get("transposition").asInt
            )
        }
        fun fromJson(json: String): Instrument {
            val jsonObject = Gson().fromJson(json, JsonObject::class.java)
            return fromJson(jsonObject)
        }
    }
    fun toJson(): JsonObject {
        return JsonObject().apply {
            addProperty("name", name)
            addProperty("shortened", shortened)
            addProperty("transposition", transposition)
        }
    }
}

data class SimpleRhythm(
    val timeSignature: Pair<Int, Int>,
    val subdivision: Int,
    val emphasis: Int
) {
    companion object {
        fun fromJson(jsonObject: JsonObject): SimpleRhythm {
            return SimpleRhythm(
                timeSignature = Pair(
                    jsonObject.getAsJsonObject("timeSignature").get("numerator").asInt,
                    jsonObject.getAsJsonObject("timeSignature").get("denominator").asInt
                ),
                subdivision = jsonObject.get("subdivision").asInt,
                emphasis = jsonObject.get("emphasis").asInt
            )
        }
        fun fromJson(json: String): SimpleRhythm {
            val jsonObject = Gson().fromJson(json, JsonObject::class.java)
            return fromJson(jsonObject)
        }
    }
    fun toJson(): JsonObject {
        return JsonObject().apply {
            add("timeSignature", JsonObject().apply {
                addProperty("numerator", timeSignature.first)
                addProperty("denominator", timeSignature.second)
            })
            addProperty("subdivision", subdivision)
            addProperty("emphasis", emphasis)
        }
    }
}

data class MetronomeState(
    val bpm: Float,
    val beatValuePrimary: Float,
    val beatValueSecondary: Float,
    val secondaryEnabled: Boolean
) {
    companion object {
        fun fromJson(jsonObject: JsonObject): MetronomeState {
            return MetronomeState(
                bpm = jsonObject.get("bpm").asFloat,
                beatValuePrimary = jsonObject.get("valuePrimary").asFloat,
                beatValueSecondary = jsonObject.get("valueSecondary").asFloat,
                secondaryEnabled = jsonObject.get("secondaryEnabled").asBoolean
            )
        }
        fun fromJson(json: String): MetronomeState {
            val jsonObject = Gson().fromJson(json, JsonObject::class.java)
            return fromJson(jsonObject)
        }
    }
    fun toJson(): JsonObject {
        return JsonObject().apply {
            addProperty("bpm", bpm)
            addProperty("valuePrimary", beatValuePrimary)
            addProperty("valueSecondary", beatValueSecondary)
            addProperty("secondaryEnabled", secondaryEnabled)
        }
    }
}

data class MetronomePreset(
    val timestamp: Long = System.currentTimeMillis(),
    val name: String,
    val primaryRhythm: Rhythm,
    val primarySimpleRhythm: SimpleRhythm,
    val secondaryRhythm: Rhythm,
    val secondarySimpleRhythm: SimpleRhythm,
    val state: MetronomeState
) {
    companion object {
        fun fromJson(jsonObject: JsonObject): MetronomePreset {
            return MetronomePreset(
                timestamp = jsonObject.get("timestamp")?.asLong ?: System.currentTimeMillis(),
                name = jsonObject.get("name").asString,
                primaryRhythm = Rhythm.deserialize(jsonObject["primaryRhythm"].asString),
                primarySimpleRhythm = SimpleRhythm.fromJson(jsonObject["primarySimpleRhythm"].asJsonObject),
                secondaryRhythm = Rhythm.deserialize(jsonObject["secondaryRhythm"].asString),
                secondarySimpleRhythm = SimpleRhythm.fromJson(jsonObject["secondarySimpleRhythm"].asJsonObject),
                state = MetronomeState.fromJson(jsonObject["state"].asJsonObject)
            )
        }
    }
    fun toJson(): JsonObject {
        return JsonObject().apply {
            addProperty("timestamp", timestamp)
            addProperty("name", name)
            addProperty("primaryRhythm", primaryRhythm.serialize())
            add("primarySimpleRhythm", primarySimpleRhythm.toJson())
            addProperty("secondaryRhythm", secondaryRhythm.serialize())
            add("secondarySimpleRhythm", secondarySimpleRhythm.toJson())
            add("state", state.toJson())
        }
    }
}

data class ColorScheme(
    val color: Color,
    val theme: Theme,
    val contrast: Contrast
) {
    enum class Color {
        /** Defaults to AQUA when API < 31 */
        SYSTEM,
        RED,
        ORANGE,
        YELLOW,
        GREEN,
        AQUA,
        BLUE,
        PURPLE
    }
    enum class Theme {
        SYSTEM,
        LIGHT,
        DARK
    }
    enum class Contrast {
        /** Only when color is SYSTEM */
        SYSTEM,
        /** Unavailable when color is SYSTEM */
        LOW,
        /** Unavailable when color is SYSTEM */
        MEDIUM,
        /** Unavailable when color is SYSTEM */
        HIGH
    }
    companion object {
        fun fromJson(jsonObject: JsonObject): ColorScheme {
            return ColorScheme(
                color = Color.entries[jsonObject.get("color").asInt],
                theme = Theme.entries[jsonObject.get("theme").asInt],
                contrast = Contrast.entries[jsonObject.get("contrast").asInt]
            )
        }
        fun fromJson(json: String): ColorScheme {
            val jsonObject = Gson().fromJson(json, JsonObject::class.java)
            return fromJson(jsonObject)
        }
    }
    fun toJson(): JsonObject {
        return JsonObject().apply {
            addProperty("color", color.ordinal)
            addProperty("theme", theme.ordinal)
            addProperty("contrast", contrast.ordinal)
        }
    }
}

data class TempoMarking(
    val name: String,
    val range: IntRange,
) {
    companion object {
        fun fromJson(jsonObject: JsonObject): TempoMarking {
            return TempoMarking(
                name = jsonObject.get("name").asString,
                range = IntRange(
                    jsonObject.getAsJsonObject("range").get("min").asInt,
                    jsonObject.getAsJsonObject("range").get("max").asInt
                )
            )
        }
    }
    fun toJson(): JsonObject {
        return JsonObject().apply {
            addProperty("name", name)
            add("range", JsonObject().apply {
                addProperty("min", range.first)
                addProperty("max", range.last)
            })
        }
    }
}