package dev.cognitivity.chronal.activity

import android.annotation.SuppressLint
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.view.WindowInsets
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.ui.metronome.windows.metronome
import dev.cognitivity.chronal.ui.metronome.windows.metronomeSecondary
import dev.cognitivity.chronal.ui.metronome.windows.paused
import dev.cognitivity.chronal.ui.metronome.windows.setBPM
import dev.cognitivity.chronal.ui.metronome.windows.updateSleepMode
import dev.cognitivity.chronal.ui.theme.MetronomeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs

class FullscreenActivity : ComponentActivity() {
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController?.hide(WindowInsets.Type.systemBars())
        } else {
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        }

        setContent {
            MetronomeTheme {
                MainContent()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController?.hide(WindowInsets.Type.systemBars())
        } else {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun MainContent() {
        var doNotShow by remember { mutableStateOf(!ChronalApp.getInstance().settings.fullscreenWarning.value) }
        var settingDialog by remember { mutableStateOf(false) }

        val coroutineScope = rememberCoroutineScope()
        val progress = remember { Animatable(0f) }
        var invert by remember { mutableStateOf(false) }
        var i by remember { mutableIntStateOf(metronome.getRhythm().measures[0].timeSig.first) }

        var highContrast by remember { mutableStateOf(ChronalApp.getInstance().settings.highContrast.value) }
        var noAnimations by remember { mutableStateOf(ChronalApp.getInstance().settings.noAnimation.value) }

        val color = MaterialTheme.colorScheme.surface
        val invertColor = if(highContrast) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant

        metronome.setUpdateListener(3) { beat ->
            val timestamp = metronome.timestamp
            val measure = metronome.getRhythm().measures[beat.measure]
            if (beat.index == 0) {
                repeat(measure.timeSig.first) { index ->
                    coroutineScope.launch {
                        val beatDelay = ((1f / measure.timeSig.second) * 60000 / metronome.bpm * metronome.beatValue).toInt() * index
                        delay(ChronalApp.getInstance().settings.visualLatency.value.toLong() + beatDelay)
                        if(!metronome.playing || timestamp != metronome.timestamp) return@launch

                        i = index + 1
                        if(progress.value != 0f || noAnimations) invert = !invert
                        progress.snapTo(0f)
                        if(!noAnimations) {
                            progress.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = ((1f / measure.timeSig.second) * 60000 / metronome.bpm * metronome.beatValue).toInt(),
                                    easing = LinearEasing
                                )
                            )
                        }
                    }
                }
            }
        }
        metronome.setPauseListener(5) { paused ->
            if(paused) {
                i = metronome.getRhythm().measures[0].timeSig.first
                coroutineScope.launch {
                    progress.animateTo(
                        targetValue = if(invert) 1f else 0f,
                        animationSpec = MotionScheme.expressive().slowEffectsSpec(),
                    )
                    progress.snapTo(0f)
                    invert = false
                }
            } else invert = false
        }

        var change = 0

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        change += dragAmount.toInt()
                        if (abs(change) >= 8) {
                            val adjustment = (change / 8)
                            setBPM(metronome.bpm - adjustment)
                            change %= 8
                        }
                    }
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        paused = !paused

                        if (paused) {
                            metronome.stop()
                            metronomeSecondary.stop()
                        }
                        else {
                            metronome.start()
                            metronomeSecondary.start()
                        }

                        updateSleepMode(window)
                    }
                )
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(abs(progress.value))
                        .background(if(!invert) invertColor else color),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(if(!invert) color else invertColor),
                )
            }
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                val dateFormat = DateFormat.getTimeFormat(LocalContext.current)
                var time by remember { mutableStateOf(dateFormat.format(Date())) }

                val batteryManager = LocalContext.current.getSystemService(BATTERY_SERVICE) as BatteryManager
                var battery by remember { mutableIntStateOf(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)) }

                LaunchedEffect(Unit) {
                    while (true) {
                        time = dateFormat.format(Date())
                        battery = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                        delay(1000)
                    }
                }

                Row(
                    modifier = Modifier.padding(16.dp).displayCutoutPadding()
                ) {
                    Text(
                        text = time,
                        modifier = Modifier.align(Alignment.CenterVertically)
                            .background(MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.25f), CircleShape)
                            .padding(8.dp, 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "$battery%",
                        modifier = Modifier.align(Alignment.CenterVertically)
                            .background(MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.25f), CircleShape)
                            .padding(8.dp, 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                 IconButton(
                    onClick = {
                        finish()
                    },
                    modifier = Modifier.align(Alignment.BottomStart)
                        .padding(24.dp)
                        .statusBarsPadding()
                        .background(MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.25f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = getString(R.string.generic_close),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = {
                        settingDialog = true
                    },
                    modifier = Modifier.align(Alignment.BottomEnd)
                        .padding(24.dp)
                        .statusBarsPadding()
                        .background(MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.25f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = getString(R.string.generic_settings),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                BeatShape(i)
            }

            var photoDialog by remember { mutableStateOf(true) }

            if(photoDialog && ChronalApp.getInstance().settings.fullscreenWarning.value) {
                AlertDialog(
                    onDismissRequest = { photoDialog = false },
                    icon = @Composable {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_bolt_24),
                            contentDescription = getString(R.string.fullscreen_warning_title)
                        )
                    },
                    title = {
                        Text(
                            text = getString(R.string.fullscreen_warning_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = getString(R.string.fullscreen_warning_text),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(
                                modifier = Modifier.height(24.dp)
                            )
                            HorizontalDivider()
                            val interactionSource = remember { MutableInteractionSource() }
                            Row(
                                modifier = Modifier.padding(4.dp)
                                    .clickable(interactionSource, indication = null) {
                                        doNotShow = !doNotShow
                                    }
                            ) {
                                Text(getString(R.string.fullscreen_warning_hide),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Checkbox(
                                    checked = doNotShow,
                                    onCheckedChange = {
                                        doNotShow = it
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary,
                                        uncheckedColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    interactionSource = interactionSource
                                )
                            }
                            HorizontalDivider()
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                photoDialog = false
                                CoroutineScope(Dispatchers.Default).launch {
                                    ChronalApp.getInstance().settings.fullscreenWarning.value = !doNotShow
                                    ChronalApp.getInstance().settings.save()
                                }
                            }
                        ) {
                            Text(getString(R.string.generic_okay))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                photoDialog = false
                                finish()
                            }
                        ) {
                            Text(getString(R.string.generic_exit))
                        }
                    }
                )
            }
            if(settingDialog) {
                AlertDialog(
                    onDismissRequest = { settingDialog = false },
                    title = {
                        Text(
                            text = getString(R.string.generic_settings),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Column {
                            val contrastInteraction = remember { MutableInteractionSource() }
                            Row(
                                modifier = Modifier.clickable(
                                    interactionSource = contrastInteraction,
                                    indication = null
                                ) {
                                    highContrast = !highContrast
                                    ChronalApp.getInstance().settings.highContrast.value = highContrast
                                    coroutineScope.launch {
                                        ChronalApp.getInstance().settings.save()
                                    }
                                },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(getString(R.string.fullscreen_setting_name_contrast),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(getString(R.string.fullscreen_setting_description_contrast),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = highContrast,
                                    onCheckedChange = { checked ->
                                        ChronalApp.getInstance().settings.highContrast.value = checked
                                        highContrast = checked
                                        coroutineScope.launch {
                                            ChronalApp.getInstance().settings.save()
                                        }
                                    },
                                    interactionSource = contrastInteraction,
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val animationInteraction = remember { MutableInteractionSource() }
                            Row(
                                modifier = Modifier.clickable(
                                    interactionSource = animationInteraction,
                                    indication = null
                                ) {
                                    noAnimations = !noAnimations
                                    ChronalApp.getInstance().settings.noAnimation.value = noAnimations
                                    coroutineScope.launch {
                                        ChronalApp.getInstance().settings.save()
                                    }
                                }
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(getString(R.string.fullscreen_setting_name_animation),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(getString(R.string.fullscreen_setting_description_animation),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = noAnimations,
                                    onCheckedChange = { checked ->
                                        ChronalApp.getInstance().settings.noAnimation.value = checked
                                        noAnimations = checked
                                        coroutineScope.launch {
                                            ChronalApp.getInstance().settings.save()
                                        }
                                    },
                                    interactionSource = animationInteraction,
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                settingDialog = false
                            }
                        ) {
                            Text(getString(R.string.generic_confirm))
                        }
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun BeatShape(beat: Int) {
        val color = MaterialTheme.colorScheme.primary
        val strokeWidth = 2.dp

        val shape = when (beat) {
            1 -> MaterialShapes.Circle.toShape(0)
            2 -> MaterialShapes.Pill.toShape(0)
            else -> RoundedPolygon(
                numVertices = beat,
                rounding = CornerRounding(radius = 0.2f)
            ).normalized().toShape(-90)
        }

        val invertedShape = when (beat) {
            1 -> MaterialShapes.Circle.toShape(180)
            2 -> MaterialShapes.Pill.toShape(180)
            else -> RoundedPolygon(
                numVertices = beat,
                rounding = CornerRounding(radius = 0.2f)
            ).normalized().toShape(90)
        }

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val shapeSize = if (this@BoxWithConstraints.maxHeight > maxWidth) maxHeight / 2 else maxWidth / 2
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .aspectRatio(1f)
                    .size(shapeSize)
                    .align(Alignment.Center)
                    .border(strokeWidth, color, shape)
                    .background(MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.75f), shape),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = metronome.bpm.toString(),
                        fontSize = 96.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .offset(y = (8).dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = getString(R.string.metronome_bpm),
                        fontSize = 48.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .offset(y = (-8).dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .size(shapeSize)
                    .offset(x = shapeSize / 2, y = -shapeSize / 2)
                    .align(Alignment.TopEnd)
                    .border(2.dp, color, invertedShape)
            )

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .size(shapeSize)
                    .offset(x = -shapeSize / 2, y = shapeSize / 2)
                    .align(Alignment.BottomStart)
                    .border(2.dp, color, invertedShape)
            )
        }
    }
}

