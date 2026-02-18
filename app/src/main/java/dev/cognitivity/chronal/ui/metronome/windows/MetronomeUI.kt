/*
 * Chronal: Metronome app for Android
 * Copyright (C) 2025-2026  cognitivity
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

package dev.cognitivity.chronal.ui.metronome.windows

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Window
import android.view.WindowManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.google.gson.JsonObject
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.MetronomeTrack
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.activity.MainActivity
import dev.cognitivity.chronal.activity.NavigationIcon
import dev.cognitivity.chronal.activity.NavigationItem
import dev.cognitivity.chronal.activity.vibratorManager
import dev.cognitivity.chronal.rhythm.metronome.Beat
import dev.cognitivity.chronal.settings.Setting
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.settings.types.json.MetronomePreset
import dev.cognitivity.chronal.settings.types.json.MetronomeState
import dev.cognitivity.chronal.ui.metronome.sheets.EditRhythm
import dev.cognitivity.chronal.ui.metronome.sheets.TapTempo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

var dropdownExpanded by mutableStateOf(false)
var showTempoTapper by mutableStateOf(false)
var showRhythmPrimary by mutableStateOf(false)
var showRhythmSecondary by mutableStateOf(false)

var vibratePrimary by mutableStateOf(Settings.METRONOME_VIBRATIONS.get())
var vibrateSecondary by mutableStateOf(Settings.METRONOME_VIBRATIONS_SECONDARY.get())

var paused by mutableStateOf(true)

lateinit var activity: MainActivity

val intervals = mutableListOf<Long>()
var lastTapTime: Long? = null

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetronomePageMain(window: Window, expanded: Boolean, mainActivity: MainActivity, padding: PaddingValues) {
    val metronome = ChronalApp.getInstance().metronome

    activity = mainActivity

    //load preset from widget if available
    val presetJson = mainActivity.intent.getStringExtra("preset")
    if(presetJson != null && presetJson.isNotEmpty()) {
        val preset = MetronomePreset.fromJson(Gson().fromJson(presetJson, JsonObject::class.java))

        val primaryTrack = metronome.getTrack(0)
        primaryTrack.bpm = preset.state.bpm
        primaryTrack.beatValue = preset.state.beatValuePrimary
        val secondaryTrack = metronome.getTrack(1)
        secondaryTrack.bpm = preset.state.bpm
        secondaryTrack.beatValue = preset.state.beatValueSecondary
        secondaryTrack.enabled = preset.state.secondaryEnabled

        Settings.METRONOME_STATE.set(preset.state)
        Settings.METRONOME_RHYTHM.set(preset.primaryRhythm.serialize())
        Settings.METRONOME_RHYTHM_SECONDARY.set(preset.secondaryRhythm.serialize())
        Settings.METRONOME_SIMPLE_RHYTHM.set(preset.primarySimpleRhythm)
        Settings.METRONOME_SIMPLE_RHYTHM_SECONDARY.set(preset.secondarySimpleRhythm)
        metronome.getTrack(0).setRhythm(preset.primaryRhythm)
        metronome.getTrack(1).setRhythm(preset.secondaryRhythm)

        mainActivity.intent.removeExtra("preset")

        LaunchedEffect(Unit) {
            Setting.saveAll()
        }
    }

    val navController = rememberNavController()

    if(expanded) {
        MetronomePageExpanded(navController, window)
    } else {
        MetronomePageCompact(navController, window, padding)
    }
    if(showTempoTapper) {
        BottomSheet(
            onDismissRequest = {
                showTempoTapper = false
                intervals.clear()
                lastTapTime = 0
            },
        ) {
            TapTempo()
        }
    }
    if(showRhythmPrimary) {
        BottomSheet(
            onDismissRequest = {
                showRhythmPrimary = false
            },
        ) {
            EditRhythm(true, expanded) {
                showRhythmPrimary = false
            }
        }
    }
    if(showRhythmSecondary) {
        BottomSheet(
            onDismissRequest = {
                showRhythmSecondary = false
            },
        ) {
            EditRhythm(false, expanded) {
                showRhythmSecondary = false
            }
        }
    }
}

@Composable
fun TopBar(navController: NavController, color: Color, padding: Boolean, clipShape: Shape) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        NavigationItem(
            R.string.page_metronome, "clock", NavigationIcon.ResourceIcon(R.drawable.baseline_music_note_24)
        ),
        NavigationItem(
            R.string.page_conductor, "conductor", NavigationIcon.VectorIcon(Icons.Outlined.Person),
            NavigationIcon.VectorIcon(Icons.Filled.Person)
        )
    )

    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    PrimaryTabRow(
        selectedTabIndex = selectedIndex,
        modifier = Modifier.windowInsetsPadding(if(padding) WindowInsets.statusBars else WindowInsets(0.dp))
            .selectableGroup()
            .clip(clipShape),
        containerColor = color,
        contentColor = MaterialTheme.colorScheme.primary,
    ) {
        items.forEachIndexed { index, item ->
            Tab(
                selected = index == selectedIndex,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    when (val icon = item.icon) {
                        is NavigationIcon.ResourceIcon -> {
                            Icon(
                                painter = painterResource(
                                    if (currentRoute == item.route)
                                        (item.selectedIcon as NavigationIcon.ResourceIcon).resourceId
                                    else icon.resourceId
                                ),
                                contentDescription = context.getString(item.label)
                            )
                        }

                        is NavigationIcon.VectorIcon -> {
                            Icon(
                                imageVector = (
                                        if (currentRoute == item.route)
                                            (item.selectedIcon as NavigationIcon.VectorIcon).imageVector
                                        else icon.imageVector
                                        ),
                                contentDescription = context.getString(item.label)
                            )
                        }
                    }
                },
                text = { Text(context.getString(item.label)) },
                interactionSource = object : MutableInteractionSource {
                    override val interactions: Flow<Interaction> = emptyFlow()

                    override suspend fun emit(interaction: Interaction) {}

                    override fun tryEmit(interaction: Interaction) = true
                }
            )
        }
    }
}
@Composable
fun ClockBeats(progress: Animatable<Float, AnimationVector1D>, trackSize: Float, beats: List<Beat>, majorOffColor: Color, minorOffColor: Color, majorPrimaryColor: Color, minorPrimaryColor: Color,
               surface: Color = MaterialTheme.colorScheme.surface) {
    val showBeats = Settings.SHOW_BEATS.get()
    val showSubdivisions = Settings.SHOW_SUBDIVISIONS.get()
    if (!showBeats && !showSubdivisions) return

    Canvas(Modifier.fillMaxSize()) {

        val borderSize = 8.dp.toPx()
        val radius = (size.minDimension / 2) - trackSize / 2

        val pillWidth = 16.dp.toPx()
        val pillHeight = 8.dp.toPx()


        val totalDuration = beats.sumOf { abs(it.duration) }
        var currentDuration = 0.0
        for(beat in beats) {
            val isMajor = beat.isHigh
            if((isMajor && !showBeats) || (!isMajor && !showSubdivisions) || beat.duration <= 0) {
                currentDuration += abs(beat.duration)
                continue
            }

            val percentage = currentDuration / totalDuration
            val angle = 2 * Math.PI * (percentage - 0.25)
            val x = cos(angle).toFloat()
            val y = sin(angle).toFloat()

            val dotX = (center.x + radius * x)
            val dotY = (center.y + radius * y)

            val reached = progress.value > percentage
            val beatColor = if (isMajor) {
                if (reached) majorPrimaryColor else majorOffColor
            } else {
                if (reached) minorPrimaryColor else minorOffColor
            }

            drawContext.canvas.save()
            drawContext.canvas.rotate((360f * percentage.toFloat()) - 90f, dotX, dotY)

            val size = if(isMajor) Size(pillWidth + borderSize, pillHeight + borderSize) else Size(pillHeight + borderSize, pillHeight + borderSize)
            drawRoundRect(
                color = surface,
                topLeft = Offset(dotX - size.width / 2, dotY - size.height / 2),
                size = size,
                cornerRadius = CornerRadius(size.minDimension / 2),
            )

            val outlineSize = if(isMajor) Size(pillWidth, pillHeight) else Size(pillHeight, pillHeight)
            drawRoundRect(
                color = beatColor,
                topLeft = Offset(dotX - outlineSize.width / 2, dotY - outlineSize.height / 2),
                size = outlineSize,
                cornerRadius = CornerRadius(outlineSize.minDimension / 2),
            )

            drawContext.canvas.restore()
            currentDuration += abs(beat.duration)
        }
    }
}

var lastVibration = 0L

fun setBPM(bpm: Float) {
    val metronome = ChronalApp.getInstance().metronome

    paused = true
    metronome.stop()
    metronome.getTracks().forEach { it.bpm = bpm }

    val primaryTrack = metronome.getTrack(0)
    val secondaryTrack = metronome.getTrack(1)

    CoroutineScope(Dispatchers.Main).launch {
        Settings.METRONOME_STATE.save(MetronomeState(
            bpm = bpm, beatValuePrimary = primaryTrack.beatValue,
            beatValueSecondary = secondaryTrack.beatValue, secondaryEnabled = secondaryTrack.enabled,
        ))
    }

    if(bpm <= MetronomeTrack.MIN_BPM || bpm >= MetronomeTrack.MAX_BPM) {
        if(System.currentTimeMillis() - lastVibration < 100) return
        lastVibration = System.currentTimeMillis()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && vibratorManager != null)
            vibratorManager!!.vibrate(
            CombinedVibration.createParallel(
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
            )
        ) else {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(10)
        }
    } else {
        val tickPattern = longArrayOf(5)
        val tickAmplitude = intArrayOf((bpm / 2).toInt().coerceIn(1, 255))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && vibratorManager != null) {
            vibratorManager!!.vibrate(
                CombinedVibration.createParallel(
                    VibrationEffect.createWaveform(tickPattern,tickAmplitude, -1)
                )
            )
        } else {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(5)
        }
    }
}

fun updateSleepMode(window: Window) {
    if(!paused) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    } else {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = {
            onDismissRequest()
        },
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { it != SheetValue.PartiallyExpanded }
        ),
        contentWindowInsets = { WindowInsets.navigationBars },
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        content()
    }
}

fun Modifier.verticalBPMGesture(): Modifier {
    val metronome = ChronalApp.getInstance().metronome
    var change = 0

    return this.pointerInput(Unit) {
        detectVerticalDragGestures(
            onDragStart = {
                change = 0
            },
            onVerticalDrag = { _, dragAmount ->
                change += dragAmount.toInt()
                if (abs(change) >= 64) {
                    val adjustment = (change / 40)
                    setBPM((metronome.getTrack(0).bpm) - adjustment)
                    change %= 40
                }
            }
        )
    }
}