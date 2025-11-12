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

package dev.cognitivity.chronal.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.MusicFont
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.SimpleRhythm
import dev.cognitivity.chronal.rhythm.metronome.Measure
import dev.cognitivity.chronal.rhythm.metronome.Rhythm
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmElement
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmNote
import dev.cognitivity.chronal.rhythm.metronome.elements.RhythmTuplet
import dev.cognitivity.chronal.rhythm.metronome.elements.StemDirection
import dev.cognitivity.chronal.toPx
import dev.cognitivity.chronal.ui.metronome.windows.ClockBeats
import dev.cognitivity.chronal.ui.theme.MetronomeTheme
import kotlinx.coroutines.launch
import kotlin.math.abs

class SimpleEditorActivity : ComponentActivity() {
    private var isPrimary by mutableStateOf(true)
    private var error by mutableStateOf(false)
    private var previewRhythm by mutableStateOf<Rhythm?>(null)
    private lateinit var rhythm: MutableState<SimpleRhythm>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if(!intent.hasExtra("isPrimary")) {
            finish()
            return
        }
        isPrimary = intent.getBooleanExtra("isPrimary", true)


        setContent {
            MetronomeTheme {
                MainContent()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
        ExperimentalMaterial3WindowSizeClassApi::class
    )
    @Composable
    fun MainContent() {
        val scope = rememberCoroutineScope()
        val navController = rememberNavController()
        var backDropdown by remember { mutableStateOf(false) }
        val setting = if (isPrimary) ChronalApp.getInstance().settings.metronomeSimpleRhythm else ChronalApp.getInstance().settings.metronomeSimpleRhythmSecondary
        val initialValue = setting.value
        val startPage = if(initialValue.timeSignature.first != 0 && initialValue.timeSignature.second == 0) "beat" else "time_signature"
        rhythm = remember { mutableStateOf(setting.value) }
        val sizeClass = calculateWindowSizeClass(this)
        var expanded = false
        when(sizeClass.widthSizeClass) {
            WindowWidthSizeClass.Expanded -> {
                expanded = true
            }
        }

        previewRhythm = getRhythm(rhythm.value)

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(getString(if(isPrimary) R.string.simple_editor_primary else R.string.simple_editor_secondary)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                    navigationIcon = {
                        MaterialTheme(
                            colorScheme = MaterialTheme.colorScheme.copy(surfaceContainer = MaterialTheme.colorScheme.surfaceContainerHigh),
                            shapes = MaterialTheme.shapes.copy(
                                extraSmall = RoundedCornerShape(
                                    16.dp
                                )
                            )
                        ) {
                            DropdownMenu(
                                expanded = backDropdown,
                                onDismissRequest = { backDropdown = false },
                            ) {
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.outline_save_24),
                                            contentDescription = getString(R.string.generic_save_exit)
                                        )
                                    },
                                    text = { Text(getString(R.string.generic_save_exit)) },
                                    onClick = {
                                        backDropdown = false
                                        if(!error) {
                                            val parsedRhythm = getRhythm(rhythm.value)
                                            if(parsedRhythm == null) {
                                                error = true
                                                return@DropdownMenuItem
                                            }
                                            val metronome = ChronalApp.getInstance().metronome
                                            val selectedTrack = metronome.getTrack(if(isPrimary) 0 else 1)
                                            if(isPrimary) {
                                                ChronalApp.getInstance().settings.metronomeRhythm.value = parsedRhythm.serialize()
                                                ChronalApp.getInstance().settings.metronomeSimpleRhythm.value = rhythm.value
                                            } else {
                                                ChronalApp.getInstance().settings.metronomeRhythmSecondary.value = parsedRhythm.serialize()
                                                ChronalApp.getInstance().settings.metronomeSimpleRhythmSecondary.value = rhythm.value
                                            }
                                            scope.launch {
                                                ChronalApp.getInstance().settings.save()
                                                selectedTrack.setRhythm(parsedRhythm)
                                                finish()
                                            }
                                        }
                                    },
                                    enabled = !error
                                )
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = getString(R.string.generic_exit_discard)
                                        )
                                    },
                                    text = { Text(getString(R.string.generic_exit_discard)) },
                                    onClick = {
                                        backDropdown = false
                                        rhythm.value = initialValue
                                        val rhythm = getRhythm(rhythm.value)
                                        if(rhythm == null) {
                                            error = true
                                            return@DropdownMenuItem
                                        }
                                        val metronome = ChronalApp.getInstance().metronome
                                        val selectedTrack = metronome.getTrack(if(isPrimary) 0 else 1)

                                        if(isPrimary) {
                                            ChronalApp.getInstance().settings.metronomeRhythm.value = rhythm.serialize()
                                            ChronalApp.getInstance().settings.metronomeSimpleRhythm.value = initialValue
                                        } else {
                                            ChronalApp.getInstance().settings.metronomeRhythmSecondary.value = rhythm.serialize()
                                            ChronalApp.getInstance().settings.metronomeSimpleRhythmSecondary.value = initialValue
                                        }
                                        scope.launch {
                                            ChronalApp.getInstance().settings.save()
                                            selectedTrack.setRhythm(rhythm)
                                            finish()
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Close,
                                            contentDescription = getString(R.string.generic_cancel)
                                        )
                                    },
                                    text = { Text(getString(R.string.generic_cancel)) },
                                    onClick = {
                                        backDropdown = false
                                    }
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                if(rhythm == initialValue) {
                                    finish()
                                } else {
                                    backDropdown = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = getString(R.string.generic_back)
                            )
                        }
                    },
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(innerPadding)
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                TabRow(navController)
                if(expanded) {
                    Row {
                        Row(
                            modifier = Modifier.fillMaxHeight()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(208.dp),
                            ) {
                                ClockPreview {
                                    Box(
                                        modifier = Modifier.fillMaxHeight(0.5f)
                                            .align(Alignment.Center)
                                    ) {
                                        if (currentRoute == "beat") {
                                            MusicFont.Number.TimeSignatureLine(rhythm.value.timeSignature.first,
                                                color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                                            )
                                        } else {
                                            MusicFont.Number.TimeSignature(rhythm.value.timeSignature.first, rhythm.value.timeSignature.second,
                                                color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                                            )
                                        }
                                    }
                                }
                            }
                            EmphasisSwitcher()
                        }
                        NavigationHost(navController,
                            modifier = Modifier.fillMaxHeight()
                                .weight(1f),
                            expanded = true, startPage
                        )
                    }
                } else {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.height(IntrinsicSize.Min)
                                    .weight(1f)
                                    .aspectRatio(1f),
                            ) {
                                ClockPreview {
                                    Box(
                                        modifier = Modifier.fillMaxHeight(0.5f)
                                            .align(Alignment.Center)
                                    ) {
                                        if (currentRoute == "beat") {
                                            MusicFont.Number.TimeSignatureLine(rhythm.value.timeSignature.first,
                                                color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                                            )
                                        } else {
                                            MusicFont.Number.TimeSignature(rhythm.value.timeSignature.first, rhythm.value.timeSignature.second,
                                                color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                                            )
                                        }
                                    }
                                }
                            }
                            EmphasisSwitcher()
                        }
                        NavigationHost(navController,
                            modifier = Modifier.fillMaxWidth()
                                .weight(1f),
                            expanded = false, startPage
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun TabRow(navController: NavController) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        PrimaryTabRow(
            selectedTabIndex = when(currentRoute) {
                "beat" -> 0
                "time_signature" -> 1
                else -> 0
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Tab(
                selected = currentRoute == "beat",
                onClick = {
                    navController.navigate("beat")
                    rhythm.value = rhythm.value.copy(timeSignature = rhythm.value.timeSignature.first to 0, subdivision = 0)
                },
                text = { Text(getString(R.string.simple_editor_beat_count)) }
            )
            Tab(
                selected = currentRoute == "time_signature",
                onClick = {
                    navController.navigate("time_signature")
                    val second = if(rhythm.value.timeSignature.second == 0) 4 else rhythm.value.timeSignature.second
                    val subdivision = if(rhythm.value.subdivision == 0) 4 else rhythm.value.subdivision
                    rhythm.value = rhythm.value.copy(timeSignature = rhythm.value.timeSignature.first to second, subdivision = subdivision)
                },
                text = { Text(getString(R.string.simple_editor_time_signature)) }
            )
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun BoxScope.ClockPreview(content: @Composable BoxScope.() -> Unit = {}) {
        if(previewRhythm == null) {
            error = true
        }
        if(error) {
            Column(
                modifier = Modifier.size(180.dp)
                    .align(Alignment.Center)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow, CircleShape),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_warning_24),
                    contentDescription = getString(R.string.generic_error),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(getString(R.string.simple_editor_invalid_beat),
                    style = MaterialTheme.typography.bodySmallEmphasized,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        } else {
            val metronome = ChronalApp.getInstance().metronome
            val selectedTrack = metronome.getTrack(if(isPrimary) 0 else 1)
            val trackColor = if(isPrimary) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer

            Box(
                modifier = Modifier.size(180.dp)
                    .align(Alignment.Center)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val trackSize = 4.dp.toPx()
                    val radius = (size.minDimension / 2) - trackSize / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    drawCircle(
                        color = trackColor,
                        radius = radius,
                        center = center,
                        style = Stroke(width = trackSize)
                    )
                }
                if(isPrimary) {
                    ClockBeats(remember { Animatable(-1f) }, 4.dp.toPx(),
                        selectedTrack.calculateIntervals(previewRhythm!!).filter { it.measure == 0 },
                        majorOffColor = MaterialTheme.colorScheme.primaryContainer,
                        minorOffColor = MaterialTheme.colorScheme.onPrimary,
                        majorPrimaryColor = MaterialTheme.colorScheme.primary,
                        minorPrimaryColor = MaterialTheme.colorScheme.primary,
                        surface = MaterialTheme.colorScheme.surface
                    )
                } else {
                    ClockBeats(remember { Animatable(-1f) }, 4.dp.toPx(),
                        selectedTrack.calculateIntervals(previewRhythm!!).filter { it.measure == 0 },
                        majorOffColor = MaterialTheme.colorScheme.tertiaryContainer,
                        minorOffColor = MaterialTheme.colorScheme.onTertiary,
                        majorPrimaryColor = MaterialTheme.colorScheme.tertiary,
                        minorPrimaryColor = MaterialTheme.colorScheme.tertiary,
                        surface = MaterialTheme.colorScheme.surface
                    )
                }
                content()
            }
        }
    }

    @Composable
    fun EmphasisSwitcher() {
        Column(
            modifier = Modifier.width(IntrinsicSize.Min)
                .padding(end = 32.dp),
            horizontalAlignment = Alignment.Start
        ) {
            @Composable
            fun EmphasisButton(
                label: Int,
                selected: Boolean,
                onClick: () -> Unit
            ) {
                val interactionSource = remember { MutableInteractionSource() }
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onClick
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = selected,
                        onClick = onClick,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = if (isPrimary) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.tertiary,
                        ),
                        interactionSource = interactionSource
                    )
                    Text(context.getString(label),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (selected) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            EmphasisButton(R.string.simple_editor_emphasis_all, rhythm.value.emphasis == 0) {
                rhythm.value = rhythm.value.copy(emphasis = 0)
            }
            EmphasisButton(R.string.simple_editor_emphasis_none, rhythm.value.emphasis == 1) {
                rhythm.value = rhythm.value.copy(emphasis = 1)
                checkRhythm(rhythm.value)
            }
            EmphasisButton(R.string.simple_editor_emphasis_first, rhythm.value.emphasis == 2) {
                rhythm.value = rhythm.value.copy(emphasis = 2)
                checkRhythm(rhythm.value)
            }
            EmphasisButton(R.string.simple_editor_emphasis_alternate, rhythm.value.emphasis == 3) {
                rhythm.value = rhythm.value.copy(emphasis = 3)
                checkRhythm(rhythm.value)
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun NavigationHost(navController: NavHostController, modifier: Modifier = Modifier, expanded: Boolean, startPage: String) {
        val enterTransition: (Boolean) -> EnterTransition = { forward ->
            if (expanded) {
                slideInVertically(MotionScheme.expressive().slowSpatialSpec(), initialOffsetY = { if(forward) it else -it })
            } else {
                slideInHorizontally(MotionScheme.expressive().slowSpatialSpec(), initialOffsetX = { if(forward) it else -it })
            }
        }

        val exitTransition: (Boolean) -> ExitTransition = { forward ->
            if(expanded) {
                slideOutVertically(MotionScheme.expressive().slowSpatialSpec(), targetOffsetY = { if(forward) -it else it })
            } else {
                slideOutHorizontally(MotionScheme.expressive().slowSpatialSpec(), targetOffsetX = { if(forward) -it else it })
            }
        }
        NavHost(
            navController = navController,
            startDestination = startPage,
            modifier = modifier.clipToBounds()
        ) {
            composable("beat",
                enterTransition = {
                    enterTransition(false)
                },
                exitTransition = {
                    exitTransition(true)
                }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                            .padding(top = 32.dp, bottom = 64.dp)
                    ) {
                        FilledTonalIconButton(
                            onClick = {
                                if (rhythm.value.timeSignature.first > 1) {
                                    rhythm.value = rhythm.value.copy(timeSignature = rhythm.value.timeSignature.copy(first = rhythm.value.timeSignature.first - 1))
                                    checkRhythm(rhythm.value)
                                }
                            },
                            modifier = Modifier.minimumInteractiveComponentSize()
                                .size(IconButtonDefaults.mediumContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide)),
                            enabled = rhythm.value.timeSignature.first > 1,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_remove_24),
                                contentDescription = context.getString(R.string.generic_subtract),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier = Modifier.height(56.dp)
                                .align(Alignment.CenterVertically)
                                .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(16.dp))
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text("${rhythm.value.timeSignature.first}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))
                        FilledIconButton(
                            onClick = {
                                rhythm.value = rhythm.value.copy(timeSignature = rhythm.value.timeSignature.copy(first = rhythm.value.timeSignature.first + 1))
                                checkRhythm(rhythm.value)
                            },
                            modifier = Modifier.minimumInteractiveComponentSize()
                                .size(IconButtonDefaults.mediumContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide)),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = context.getString(R.string.generic_add),
                            )
                        }
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(48.dp))
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 8.dp)
                    ) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            maxItemsInEachRow = 4
                        ) {
                            repeat(rhythm.value.timeSignature.first) { i ->
                                BeatShape(i+1)
                            }
                        }
                    }
                }
            }

            composable("time_signature",
                enterTransition = {
                    enterTransition(true)
                },
                exitTransition = {
                    exitTransition(false)
                }
            ) {
                if(expanded) {
                    Row {
                        TimeSignature(
                            modifier = Modifier.fillMaxHeight()
                                .weight(0.75f)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        )
                        BeatChanger(
                            modifier = Modifier.fillMaxHeight()
                                .weight(1f)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        )
                    }
                } else {
                    Column {
                        TimeSignature(
                            modifier = Modifier.fillMaxWidth()
                                .weight(0.75f)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        )
                        BeatChanger(
                            modifier = Modifier.fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun TimeSignature(modifier: Modifier) {
        Box(
            modifier = modifier,
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
                    .aspectRatio(1f)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainer,
                        MaterialShapes.Bun.toShape(0)
                    )
                    .align(Alignment.Center)
            ) {
                Row(
                    modifier = Modifier.weight(1f)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = {
                            rhythm.value = rhythm.value.copy(timeSignature = Pair((rhythm.value.timeSignature.first - 1).coerceIn(1..32),
                                rhythm.value.timeSignature.second))
                            checkRhythm(rhythm.value)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                            contentDescription = getString(R.string.generic_subtract),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Box(
                        modifier = Modifier.weight(1f)
                            .fillMaxHeight(0.8f)
                            .align(Alignment.CenterVertically),
                        contentAlignment = Alignment.Center
                    ) {
                        MusicFont.Number.TimeSignatureLine(
                            rhythm.value.timeSignature.first,
                            MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = {
                            rhythm.value = rhythm.value.copy(timeSignature = Pair((rhythm.value.timeSignature.first + 1).coerceIn(1..32),
                                rhythm.value.timeSignature.second))
                            checkRhythm(rhythm.value)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                            contentDescription = getString(R.string.generic_add),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                Row(
                    modifier = Modifier.weight(1f)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = {
                            rhythm.value = rhythm.value.copy(timeSignature = Pair(rhythm.value.timeSignature.first, (rhythm.value.timeSignature.second / 2)
                                .coerceIn(1..32)))
                            checkRhythm(rhythm.value)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                            contentDescription = getString(R.string.generic_subtract),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Box(
                        modifier = Modifier.weight(1f)
                            .fillMaxHeight(0.8f)
                            .align(Alignment.CenterVertically),
                        contentAlignment = Alignment.Center
                    ) {
                        MusicFont.Number.TimeSignatureLine(
                            rhythm.value.timeSignature.second,
                            MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = {
                            rhythm.value = rhythm.value.copy(timeSignature = Pair(rhythm.value.timeSignature.first, (rhythm.value.timeSignature.second * 2)
                                .coerceIn(1..32)))
                            checkRhythm(rhythm.value)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                            contentDescription = getString(R.string.generic_add),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun BeatChanger(modifier: Modifier) {
        var custom = rhythm.value.subdivision != 0

        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(getString(R.string.simple_editor_beat),
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                    verticalArrangement = Arrangement.Center
                ) {
                    ToggleButton(
                        checked = !custom,
                        onCheckedChange = {
                            custom = false
                            rhythm.value = rhythm.value.copy(subdivision = 0)
                            checkRhythm(rhythm.value)
                        },
                        shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                        contentPadding = ButtonDefaults.ContentPadding
                    ) {
                        Text(getString(R.string.simple_editor_beat_auto))
                    }

                    ToggleButton(
                        checked = custom,
                        onCheckedChange = {
                            custom = true
                            rhythm.value = rhythm.value.copy(subdivision = rhythm.value.timeSignature.second)
                            checkRhythm(rhythm.value)
                        },
                        shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                        contentPadding = ButtonDefaults.ContentPadding
                    ) {
                        Text(getString(R.string.simple_editor_beat_custom))
                    }
                }
            }
            Spacer(Modifier.weight(1f))

            FlowColumn(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                maxItemsInEachColumn = 2
            ) {
                for(i in 0..5) { // 1 - 32
                    val duration = 1f / 1.shl(i)
                    val measureDuration = rhythm.value.timeSignature.first / rhythm.value.timeSignature.second.toFloat()

                    val checked = rhythm.value.subdivision == 1.shl(i)
                    val enabled = measureDuration % duration < 1e-6f || abs(duration - (measureDuration % duration)) < 1e-6f
                    if(checked && !enabled) {
                        rhythm.value = rhythm.value.copy(subdivision = 0)
                    }
                    NoteButton(1.shl(i), false, custom, checked, enabled) {
                        rhythm.value = rhythm.value.copy(subdivision = 1.shl(i))
                        checkRhythm(rhythm.value)
                    }
                }
                for(i in 2..5) { // 4 - 32, triplets
                    val duration = 1f / 1.shl(i) * 2 / 3f
                    val measureDuration = rhythm.value.timeSignature.first / rhythm.value.timeSignature.second.toFloat()

                    val checked = rhythm.value.subdivision == (1.shl(i) * 3 / 2f).toInt()
                    val enabled = measureDuration % duration < 1e-6f || abs(duration - (measureDuration % duration)) < 1e-6f
                    if(checked && !enabled) {
                        rhythm.value = rhythm.value.copy(subdivision = 0)
                    }
                    NoteButton(1.shl(i), true, custom, checked, enabled) {
                        rhythm.value = rhythm.value.copy(subdivision = (1.shl(i) * 3 / 2f).toInt())
                        checkRhythm(rhythm.value)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
        }
    }
    @Composable
    fun NoteButton(value: Int, triplet: Boolean, custom: Boolean, checked: Boolean, enabled: Boolean, onClick: () -> Unit) {
        Box(
            modifier = Modifier.size(72.dp)
                .padding(4.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if(!enabled || !custom) MaterialTheme.colorScheme.surfaceContainer
                    else if(checked) {
                        if(isPrimary) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.tertiaryContainer
                    } else MaterialTheme.colorScheme.surfaceContainerHigh
                )
                .clickable(custom) {
                    if(enabled) {
                        onClick()
                    } else {
                        Toast.makeText(this, R.string.simple_editor_invalid_beat, Toast.LENGTH_SHORT).show()
                    }
                }
        ) {
            val noteColor = if(!enabled || !custom) MaterialTheme.colorScheme.onSurfaceVariant
                else if(checked) {
                    if(isPrimary) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onTertiaryContainer
                } else MaterialTheme.colorScheme.onSurface
            MusicFont.Notation.NoteCentered(
                note = MusicFont.Notation.entries.find { it.char == MusicFont.Notation.convert(value) }
                    ?: MusicFont.Notation.N_QUARTER,
                color = noteColor,
                size = if(triplet) 40.dp else 56.dp,
                modifier = Modifier.align(Alignment.Center)
                    .offset(y = if(triplet) 8.dp else 0.dp)
            )

            if(triplet) {
                Row(modifier = Modifier.align(Alignment.TopCenter)) {
                    Box(
                        modifier = Modifier.height(1.dp)
                            .padding(horizontal = 4.dp)
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                            .background(noteColor)
                    )
                    Text(
                        text = "3",
                        color = noteColor,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.align(Alignment.CenterVertically)
                            .padding(4.dp)
                    )
                    Box(
                        modifier = Modifier.height(1.dp)
                            .padding(horizontal = 4.dp)
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                            .background(noteColor)
                    )
                }
            }

        }
    }
    fun checkRhythm(value: SimpleRhythm): Boolean {
        if (getRhythm(value) != null) return true

        // fallback: switch to automatic beats and use denominator as subdivision
        val autoFixed = value.copy(subdivision = 0)
        if (getRhythm(autoFixed) == null) { // shouldn't happen
            error = true
            return false
        }
        error = false
        rhythm.value = autoFixed
        previewRhythm = getRhythm(autoFixed)
        Toast.makeText(this, R.string.simple_editor_error_adjusted, Toast.LENGTH_SHORT).show()
        return true
    }

    fun getRhythm(value: SimpleRhythm): Rhythm? {
        val timeSignature = if(value.timeSignature.second == 0) (value.timeSignature.first to 4) else value.timeSignature
        val subdivision = if(value.subdivision == 0) timeSignature.second else value.subdivision
        val isTuplet = (subdivision and (subdivision - 1)) != 0
        val duration = 1.0 / subdivision
        val measureDuration = timeSignature.first / timeSignature.second.toDouble()

        var remaining = measureDuration
        var emphasizeNext = value.emphasis != 1
        val newMeasure = Measure(timeSignature, arrayListOf<RhythmElement>().apply {
            while(remaining > 1e-6) {
                if(isTuplet) {
                    add(RhythmTuplet(
                        ratio = 3 to 2,
                        notes = ArrayList<RhythmNote>().apply {
                            repeat(3) {
                                if (remaining <= 0) return@repeat
                                add(RhythmNote(
                                    stemDirection = if(emphasizeNext) StemDirection.UP else StemDirection.DOWN,
                                    baseDuration = duration,
                                    dots = 0
                                ))
                                remaining -= duration
                                emphasizeNext = when (value.emphasis) {
                                    0 -> true
                                    3 -> !emphasizeNext
                                    else -> false
                                }
                            }
                        }
                    ))
                } else {
                    add(RhythmNote(
                        stemDirection = if(emphasizeNext) StemDirection.UP else StemDirection.DOWN,
                        baseDuration = duration,
                        dots = 0
                    ))
                    remaining -= duration
                    emphasizeNext = when (value.emphasis) {
                        0 -> true
                        3 -> !emphasizeNext
                        else -> false
                    }
                }
            }
        })
        if(remaining < -1e-6) return null

        val newRhythm = Rhythm(listOf(newMeasure))
        previewRhythm = newRhythm

        return newRhythm
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun BeatShape(beat: Int) {
        Box(
            modifier = Modifier.padding(8.dp)
                .size(40.dp)
        ) {
            val shape = when(beat) {
                1 -> MaterialShapes.Circle.toShape(0)
                2 -> MaterialShapes.Pill.toShape(0)
                else -> null
            }
            if(shape == null) {
                Box(
                    modifier = Modifier.size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurface,
                            RoundedPolygon(numVertices = beat, rounding = CornerRounding(radius = .2f)).normalized().toShape(-90)
                        )
                ) {
                    Text(
                        text = "$beat",
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.onSurface, shape)
                ) {
                    Text(
                        text = "$beat",
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}