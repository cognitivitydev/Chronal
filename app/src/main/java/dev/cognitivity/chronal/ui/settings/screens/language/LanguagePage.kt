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

package dev.cognitivity.chronal.ui.settings.screens.language

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import com.google.gson.Gson
import com.google.gson.JsonArray
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.settings.types.json.Language
import dev.cognitivity.chronal.ui.settings.data.SettingsPage
import dev.cognitivity.chronal.ui.settings.items.SettingItem
import dev.cognitivity.chronal.ui.settings.items.SettingMeta
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val translationResource = R.raw.translations

object LanguagePage : SettingsPage(
    id = "language",
    title = R.string.page_settings_language,
    items = listOf()
) {
    fun title(languageKey: String): String {
        return Gson().fromJson(
            ChronalApp.getInstance().resources.openRawResource(translationResource).readBytes().decodeToString(),
            JsonArray::class.java
        ).map { Language(it.asJsonObject) }
            .firstOrNull { it.key == languageKey }?.name ?: ""
    }
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    fun items(languageKey: String, activity: Activity): List<SettingItem> {
        val language = Gson().fromJson(
            ChronalApp.getInstance().resources.openRawResource(translationResource).readBytes().decodeToString(),
            JsonArray::class.java
        ).map { Language(it.asJsonObject) }
            .firstOrNull { it.key == languageKey } ?: return emptyList()
        val items = mutableListOf<SettingItem>()

        items.add(
            SettingItem.Element { ProgressIndicator(language) }
        )
        items.add(
            SettingItem.Element { ApplyButton(language, activity) }
        )
        if(language.progress > 0f && language.progress < 1f) {
            items.add(
                SettingItem.LongDescription(
                    text = R.string.settings_language_default_english
                )
            )
        }
        items.add(SettingItem.Divider())
        if(language.contributors.isNotEmpty()) {
            items.add(
                SettingItem.Container {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_translations_contributors, language.name),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                .padding(bottom = 8.dp),
                            textAlign = TextAlign.Center
                        )
                        language.contributors.forEach {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.titleMediumEmphasized,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            )
        }
        if(language.progress < 1f) {
            items.add(
                SettingItem.TextElement(
                    meta = SettingMeta(
                        title = R.string.settings_translations_contribute,
                        icon = R.drawable.outline_crowdsource_24,
                    ),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, "https://crowdin.com/project/chronal/${language.key}".toUri())
                        activity.startActivity(intent)
                    }
                )
            )
        }

        return items
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun ApplyButton(language: Language, activity: Activity) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                modifier = Modifier.heightIn(ButtonDefaults.MediumContainerHeight),
                contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight),
                enabled = language.progress > 0f && language.key != Settings.APP_LANGUAGE.get(),
                onClick = {
                    val originalLanguage = Settings.APP_LANGUAGE.get()
                    Settings.APP_LANGUAGE.set(language.key)
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val localeTag = language.androidCode ?: language.key
                            val appLocales = LocaleListCompat.forLanguageTags(localeTag)
                            AppCompatDelegate.setApplicationLocales(appLocales)

                            activity.recreate()
                        } catch (_: Exception) {
                            Toast.makeText(
                                activity,
                                activity.getString(R.string.settings_language_apply_error, language.key, originalLanguage),
                                Toast.LENGTH_SHORT
                            ).show()

                            Settings.APP_LANGUAGE.set(originalLanguage)
                        } finally {
                            Settings.APP_LANGUAGE.save()
                        }
                    }
                },
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_translate_24),
                    contentDescription = null
                )
                Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(ButtonDefaults.MediumContainerHeight)))
                Text(
                    text = stringResource(if(language.key != Settings.APP_LANGUAGE.get()) R.string.generic_apply else R.string.generic_applied),
                    style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight)
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun ProgressIndicator(language: Language) {
        val progressString = when(language.progress) {
            0f -> R.string.settings_translations_progress_none
            in 0.00f..0.33f -> R.string.settings_translations_progress_few
            in 0.33f..0.67f -> R.string.settings_translations_progress_partial
            in 0.67f..0.95f -> R.string.settings_translations_progress_most
            else -> R.string.settings_translations_progress_complete
        }
        val icon = if(language.key == Settings.APP_LANGUAGE.get()) R.drawable.outline_check_24
            else if(language.progress == 0f) R.drawable.outline_globe_2_cancel_24 else R.drawable.outline_language_24

        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }
        val progress by animateFloatAsState(
            targetValue = if(visible) language.progress else 0f,
            animationSpec = tween(durationMillis = 750, easing = EaseOutExpo)
        )
        val rotation by animateFloatAsState(
            targetValue = if(visible) 0f else -15f,
            animationSpec = tween(durationMillis = 750, easing = EaseOutExpo)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(96.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier.fillMaxSize()
                        .rotate(rotation)
                        .clip(MaterialShapes.Cookie12Sided.toShape())
                        .rotate(-rotation)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Box(
                        Modifier.fillMaxWidth()
                            .fillMaxHeight(progress)
                            .align(Alignment.BottomCenter)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(56.dp)
                )
            }
            Text(
                text = stringResource(progressString, (language.progress*100).toInt()),
                style = MaterialTheme.typography.titleMediumEmphasized,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}