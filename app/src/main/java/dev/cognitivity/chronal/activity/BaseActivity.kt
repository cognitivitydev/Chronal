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

package dev.cognitivity.chronal.activity

import android.content.Context
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import dev.cognitivity.chronal.settings.Settings
import java.util.*

abstract class BaseActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context?) {
        if(newBase == null) {
            super.attachBaseContext(null)
            return
        }

        try {
            val savedLanguage = Settings.APP_LANGUAGE.get()
            if(savedLanguage == "system") {
                super.attachBaseContext(newBase)
                return
            }

            val locale = Locale.forLanguageTag(savedLanguage)
            Locale.setDefault(locale)
            val config = Configuration(newBase.resources.configuration)
            config.setLocale(locale)

            super.attachBaseContext(newBase.createConfigurationContext(config))
        } catch(_: Exception) {
            super.attachBaseContext(newBase)
        }
    }
}