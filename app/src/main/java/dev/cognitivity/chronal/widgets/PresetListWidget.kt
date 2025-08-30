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

package dev.cognitivity.chronal.widgets

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.activity.MainActivity
import dev.cognitivity.chronal.activity.PresetActivity
import dev.cognitivity.chronal.ui.theme.colors.AquaGlanceTheme

class PresetListWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        provideContent {
            GlanceTheme(
                colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) GlanceTheme.colors
                else AquaGlanceTheme()
            ) {
                WidgetContent(context)
            }
        }
    }

    @Composable
    private fun WidgetContent(context: Context) {
        val settings = ChronalApp.getInstance().settings
        var presets = settings.metronomePresets.value

        Scaffold(
            titleBar = {
                TitleBar(
                    startIcon = ImageProvider(R.drawable.baseline_music_note_24),
                    iconColor = GlanceTheme.colors.onPrimaryContainer,
                    title = context.getString(R.string.widget_list_title),
                    textColor = GlanceTheme.colors.onSurface,
                    actions = {
                        CircleIconButton(
                            imageProvider = ImageProvider(R.drawable.outline_refresh_24),
                            contentDescription = context.getString(R.string.widget_list_reload),
                            backgroundColor = null,
                            contentColor = GlanceTheme.colors.secondary,
                            onClick = {
                                presets = settings.metronomePresets.value
                            }
                        )
                    }
                )
            },
            modifier = GlanceModifier.clickable(
                actionStartActivity<PresetActivity>()
            )
        ) {
            if (presets.isEmpty()) {
                Text(
                    text = context.getString(R.string.widget_list_none),
                    modifier = GlanceModifier.fillMaxSize(),
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = GlanceTheme.colors.onSurface
                    )
                )
            } else {
                LazyColumn(
                    modifier = GlanceModifier.fillMaxSize(),
                ) {
                    items(presets.size) { index ->
                        val preset = presets[index]
                        val onClick = actionStartActivity<MainActivity>(
                            parameters = actionParametersOf(
                                ActionParameters.Key<String>("preset") to (preset.toJson().toString()),
                            )
                        )
                        Box(
                            modifier = GlanceModifier.fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                        ) {
                            Row(
                                modifier = GlanceModifier.fillMaxWidth()
                                    .padding(8.dp)
                                    .cornerRadius(8.dp)
                                    .background(GlanceTheme.colors.secondaryContainer)
                                    .clickable(onClick),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    modifier = GlanceModifier.defaultWeight(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = preset.name,
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            color = GlanceTheme.colors.onSecondaryContainer
                                        ),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = context.getString(R.string.presets_bpm, preset.state.bpm),
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            color = GlanceTheme.colors.onSecondaryContainer
                                        ),
                                        maxLines = 1
                                    )
                                }
                                Image(
                                    provider = ImageProvider(R.drawable.baseline_play_arrow_24),
                                    contentDescription = context.getString(R.string.generic_play),
                                    modifier = GlanceModifier.padding(end = 8.dp),
                                    colorFilter = ColorFilter.tint(GlanceTheme.colors.secondary)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}