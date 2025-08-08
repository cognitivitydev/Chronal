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
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.google.gson.JsonObject
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.Metronome
import dev.cognitivity.chronal.MetronomePreset
import dev.cognitivity.chronal.MetronomeState
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.activity.MainActivity
import dev.cognitivity.chronal.activity.NavigationIcon
import dev.cognitivity.chronal.activity.NavigationItem
import dev.cognitivity.chronal.activity.vibratorManager
import dev.cognitivity.chronal.rhythm.metronome.Beat
import dev.cognitivity.chronal.ui.metronome.sheets.EditRhythm
import dev.cognitivity.chronal.ui.metronome.sheets.EditSubdivision
import dev.cognitivity.chronal.ui.metronome.sheets.EditTimeSignature
import dev.cognitivity.chronal.ui.metronome.sheets.TapTempo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

lateinit var metronome: Metronome
lateinit var metronomeSecondary: Metronome

var dropdownExpanded by mutableStateOf(false)
var showTempoTapper by mutableStateOf(false)
var showRhythmPrimary by mutableStateOf(false)
var showTimeSignaturePrimary by mutableStateOf(false)
var showSubdivisionPrimary by mutableStateOf(false)
var showRhythmSecondary by mutableStateOf(false)
var showTimeSignatureSecondary by mutableStateOf(false)
var showSubdivisionSecondary by mutableStateOf(false)

var secondaryEnabled by mutableStateOf(ChronalApp.getInstance().settings.metronomeState.value.secondaryEnabled)

var vibratePrimary by mutableStateOf(ChronalApp.getInstance().settings.metronomeVibrations.value)
var vibrateSecondary by mutableStateOf(ChronalApp.getInstance().settings.metronomeVibrationsSecondary.value)

var paused by mutableStateOf(true)

lateinit var activity: MainActivity

val intervals = mutableListOf<Long>()
var lastTapTime: Long? = null

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetronomePageMain(window: Window, expanded: Boolean, mainActivity: MainActivity, padding: PaddingValues) {
    metronome = ChronalApp.getInstance().metronome
    metronomeSecondary = ChronalApp.getInstance().metronomeSecondary
    val settings = ChronalApp.getInstance().settings

    activity = mainActivity

    //load preset from widget if available
    val presetJson = mainActivity.intent.getStringExtra("preset")
    if(presetJson != null && presetJson.isNotEmpty()) {
        val preset = MetronomePreset.fromJson(Gson().fromJson(presetJson, JsonObject::class.java))
        metronome.bpm = preset.state.bpm
        metronome.beatValue = preset.state.beatValuePrimary
        metronomeSecondary.bpm = preset.state.bpm
        metronomeSecondary.active = true
        metronomeSecondary.beatValue = preset.state.beatValueSecondary
        metronomeSecondary.active = preset.state.secondaryEnabled

        settings.metronomeState.value = preset.state
        settings.metronomeRhythm.value = preset.primaryRhythm.serialize()
        settings.metronomeRhythmSecondary.value = preset.secondaryRhythm.serialize()
        settings.metronomeSimpleRhythm.value = preset.primarySimpleRhythm
        settings.metronomeSimpleRhythmSecondary.value = preset.secondarySimpleRhythm
        metronome.setRhythm(preset.primaryRhythm)
        metronomeSecondary.setRhythm(preset.secondaryRhythm)

        mainActivity.intent.removeExtra("preset")
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
    if(showTimeSignaturePrimary || showTimeSignatureSecondary) {
        Dialog(
            onDismissRequest = { },
            title = { Text(context.getString(R.string.metronome_edit_time_signature)) },
            text = {
                EditTimeSignature(window, showTimeSignaturePrimary, expanded)
            },
            confirmButton = {
                TextButton(onClick = {
                    showTimeSignaturePrimary = false
                    showTimeSignatureSecondary = false
                }) {
                    Text(context.getString(R.string.generic_save))
                }
            }
        )
    }

    if(showSubdivisionPrimary || showSubdivisionSecondary) {
        Dialog(
            onDismissRequest = { },
            title = { Text(context.getString(R.string.metronome_edit_beats)) },
            text = {
                EditSubdivision(window, showSubdivisionPrimary, expanded)
            },
            confirmButton = {
                TextButton(onClick = {
                    showSubdivisionPrimary = false
                    showSubdivisionSecondary = false
                }) {
                    Text(context.getString(R.string.generic_save))
                }
            }
        )
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
    val showBeats = ChronalApp.getInstance().settings.showBeats.value
    val showSubdivisions = ChronalApp.getInstance().settings.showSubdivisions.value
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

fun setBPM(new: Int) {
    paused = true
    metronome.stop()
    metronomeSecondary.stop()
    metronome.bpm = new.coerceIn(1, 500)
    metronomeSecondary.bpm = metronome.bpm

    ChronalApp.getInstance().settings.metronomeState.value = MetronomeState(
        bpm = metronome.bpm, beatValuePrimary = metronome.beatValue,
        beatValueSecondary = metronomeSecondary.beatValue, secondaryEnabled = secondaryEnabled,
    )

    CoroutineScope(Dispatchers.Main).launch {
        ChronalApp.getInstance().settings.save()
    }

    if(metronome.bpm == 1 || metronome.bpm == 500) {
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
        val tickAmplitude = intArrayOf((metronome.bpm / 2).coerceIn(1, 255))

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

@Composable
fun Dialog(onDismissRequest: () -> Unit, title: @Composable () -> Unit, text: @Composable () -> Unit, confirmButton: @Composable () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.fillMaxWidth(0.9f),
        title = title,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        text = text,
        confirmButton = confirmButton
    )
}