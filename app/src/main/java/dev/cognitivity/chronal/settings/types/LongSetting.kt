package dev.cognitivity.chronal.settings.types

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import com.google.gson.JsonObject
import dev.cognitivity.chronal.settings.Setting

class LongSetting(
    key: String,
    defaultValue: Long,
) : Setting<Long>(key, defaultValue) {
    private val prefKey = longPreferencesKey(key)

    override suspend fun load(prefs: Preferences) {
        value = prefs[prefKey] ?: defaultValue
    }

    override fun write(editor: MutablePreferences) {
        editor[prefKey] = value
    }

    override fun import(json: JsonObject) {
        value = json.get(key)?.asLong ?: defaultValue
    }

    override fun export(json: JsonObject) {
        json.addProperty(key, value)
    }
}