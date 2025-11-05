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

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.MetronomePreset
import dev.cognitivity.chronal.MetronomeState
import dev.cognitivity.chronal.MusicFont
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.glance.ClockWidgetReceiver
import dev.cognitivity.chronal.ui.theme.MetronomeTheme
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.zip.GZIPOutputStream

class PresetActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MetronomeTheme {
                MainContent()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun MainContent() {
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        val settings = ChronalApp.getInstance().settings
        val setting = settings.metronomePresets
        val presets = remember { mutableStateListOf<MetronomePreset>().apply { addAll(setting.value) } }

        var showCreateDialog by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(getString(R.string.presets_title))
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { finish() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = getString(R.string.generic_back)
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showCreateDialog = true },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = getString(R.string.presets_create)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(getString(R.string.presets_create), style = MaterialTheme.typography.bodyLarge)
                }
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(8.dp)
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presets.size) { i ->
                    var preset = presets[i]

                    var showDialog by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxSize()
                            .clickable { showDialog = true }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = getString(R.string.presets_bpm, preset.state.bpm),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                            contentDescription = getString(R.string.generic_edit),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if(showDialog) {
                        var renameDialog by remember { mutableStateOf(false) }
                        var deleteDialog by remember { mutableStateOf(false) }

                        Dialog(
                            onDismissRequest = { showDialog = false },
                            properties = DialogProperties(
                                usePlatformDefaultWidth = false,
                                decorFitsSystemWindows = false
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                TopAppBar(
                                    title = {
                                        Text(getString(R.string.presets_edit_title))
                                    },
                                    navigationIcon = {
                                        IconButton(
                                            onClick = { showDialog = false }
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                                contentDescription = getString(R.string.generic_back)
                                            )
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    )
                                )
                                Column(
                                    modifier = Modifier.verticalScroll(rememberScrollState())
                                ) {
                                    DialogContent(preset,
                                        onDismiss = { showDialog = false },
                                        onRename = { renameDialog = it },
                                        onDelete = { deleteDialog = it },
                                        onUpdate = { newPreset ->
                                            preset = newPreset
                                            presets[i] = newPreset
                                            setting.value = presets.toMutableList()
                                            scope.launch {
                                                settings.save()
                                            }
                                        }
                                    )
                                }
                            }

                            if(renameDialog) {
                                var newName by remember { mutableStateOf(preset.name) }
                                AlertDialog(
                                    onDismissRequest = { renameDialog = false },
                                    title = { Text(getString(R.string.presets_rename)) },
                                    text = {
                                        OutlinedTextField(
                                            value = newName,
                                            onValueChange = { newName = it },
                                            label = { Text(getString(R.string.presets_rename_hint)) },
                                            singleLine = true
                                        )
                                    },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                preset = preset.copy(name = newName)
                                                presets[i] = preset
                                                setting.value = presets
                                                renameDialog = false
                                                scope.launch {
                                                    settings.save()
                                                }
                                            }
                                        ) {
                                            Text(getString(R.string.generic_confirm))
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(
                                            onClick = { renameDialog = false }
                                        ) {
                                            Text(getString(R.string.generic_cancel))
                                        }
                                    }
                                )
                            }
                            if(deleteDialog) {
                                AlertDialog(
                                    onDismissRequest = { deleteDialog = false },
                                    title = { Text(getString(R.string.presets_delete)) },
                                    text = { Text(getString(R.string.presets_delete_confirm, preset.name)) },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                presets.removeAt(i)
                                                setting.value = presets
                                                deleteDialog = false
                                                showDialog = false
                                                scope.launch {
                                                    settings.save()
                                                    snackbarHostState.showSnackbar(
                                                        message = getString(R.string.presets_deleted, preset.name),
                                                        actionLabel = getString(R.string.generic_undo),
                                                        duration = SnackbarDuration.Short
                                                    ).let { result ->
                                                        if (result == SnackbarResult.ActionPerformed) {
                                                            presets.add(preset)
                                                            settings.metronomePresets.value = presets
                                                            settings.save()
                                                        }
                                                    }
                                                }
                                            }
                                        ) {
                                            Text(getString(R.string.generic_confirm))
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(
                                            onClick = { deleteDialog = false }
                                        ) {
                                            Text(getString(R.string.generic_cancel))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            if(showCreateDialog) {
                var newName by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showCreateDialog = false },
                    title = { Text(getString(R.string.presets_create)) },
                    text = {
                        OutlinedTextField(
                            placeholder = { Text(getString(R.string.presets_new_name))},
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text(getString(R.string.presets_create_hint)) },
                            singleLine = true,
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val metronome = ChronalApp.getInstance().metronome
                                val metronomeSecondary = ChronalApp.getInstance().metronomeSecondary
                                val newPreset = MetronomePreset(
                                    name = newName.trim().ifBlank { getString(R.string.presets_new_name) },
                                    state = MetronomeState(
                                        bpm = metronome.bpm,
                                        beatValuePrimary = metronome.beatValue,
                                        beatValueSecondary = metronomeSecondary.beatValue,
                                        secondaryEnabled = metronomeSecondary.active
                                    ),
                                    primaryRhythm = metronome.getRhythm(),
                                    secondaryRhythm = metronomeSecondary.getRhythm(),
                                    primarySimpleRhythm = settings.metronomeSimpleRhythm.value,
                                    secondarySimpleRhythm = settings.metronomeSimpleRhythmSecondary.value
                                )
                                presets.add(newPreset)
                                setting.value = presets
                                scope.launch {
                                    settings.save()
                                    showCreateDialog = false
                                    snackbarHostState.showSnackbar(
                                        message = getString(R.string.presets_created, newPreset.name),
                                        actionLabel = getString(R.string.generic_undo),
                                        duration = SnackbarDuration.Short
                                    ).let { result ->
                                        if (result == SnackbarResult.ActionPerformed) {
                                            presets.remove(newPreset)
                                            setting.value = presets
                                            settings.save()
                                        }
                                    }
                                }
                            }
                        ) {
                            Text(getString(R.string.generic_confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showCreateDialog = false }
                        ) {
                            Text(getString(R.string.generic_cancel))
                        }
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun ColumnScope.DialogContent(preset: MetronomePreset, onDismiss: () -> Unit, onRename: (Boolean) -> Unit, onDelete: (Boolean) -> Unit, onUpdate: (MetronomePreset) -> Unit) {
        val scope = rememberCoroutineScope()
        val settings = ChronalApp.getInstance().settings
        var checked by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = preset.name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 32.dp)
            )

            val created = preset.timestamp
            val time = SimpleDateFormat.getDateTimeInstance(2, 2).format(created)
            Text(
                text = getString(R.string.presets_created_at, time),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )
            val size = SplitButtonDefaults.MediumContainerHeight
            SplitButtonLayout(
                leadingButton = {
                    SplitButtonDefaults.LeadingButton(
                        modifier = Modifier.heightIn(size),
                        shapes = SplitButtonDefaults.leadingButtonShapesFor(size),
                        contentPadding = SplitButtonDefaults.leadingButtonContentPaddingFor(size),
                        onClick = {
                            onDismiss()

                            val metronome = ChronalApp.getInstance().metronome
                            val metronomeSecondary = ChronalApp.getInstance().metronomeSecondary
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

                            scope.launch {
                                settings.save()
                                finish()
                            }
                        }
                    ) {
                        Text(getString(R.string.presets_use), style = ButtonDefaults.textStyleFor(size))
                    }
                },
                trailingButton = {
                    SplitButtonDefaults.TrailingButton(
                        modifier = Modifier.heightIn(size),
                        shapes = SplitButtonDefaults.trailingButtonShapesFor(size),
                        contentPadding = SplitButtonDefaults.trailingButtonContentPaddingFor(size),
                        checked = checked,
                        onCheckedChange = { checked = it }
                    ) {
                        val rotation: Float by
                        animateFloatAsState(
                            targetValue = if (checked) 180f else 0f,
                            label = "Trailing Icon Rotation",
                        )
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            modifier = Modifier.size(SplitButtonDefaults.trailingButtonIconSizeFor(size))
                                .graphicsLayer {
                                    this.rotationZ = rotation
                                },
                            contentDescription = getString(if(checked) R.string.generic_menu_collapse else R.string.generic_menu_expand),
                        )
                    }
                    DropdownMenu(
                        expanded = checked,
                        onDismissRequest = { checked = false },
                    ) {
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = getString(R.string.presets_rename),
                                )
                            },
                            text = { Text(getString(R.string.presets_rename)) },
                            onClick = {
                                checked = false
                                onRename(true)
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_music_note_24),
                                    contentDescription = getString(R.string.presets_set_current),
                                )
                            },
                            text = { Text(getString(R.string.presets_set_current)) },
                            onClick = {
                                checked = false
                                val metronome = ChronalApp.getInstance().metronome
                                val metronomeSecondary = ChronalApp.getInstance().metronomeSecondary

                                val newPreset = preset.copy(
                                    state = MetronomeState(
                                        bpm = metronome.bpm,
                                        beatValuePrimary = metronome.beatValue,
                                        beatValueSecondary = metronomeSecondary.beatValue,
                                        secondaryEnabled = metronomeSecondary.active
                                    ),
                                    primaryRhythm = metronome.getRhythm(),
                                    secondaryRhythm = metronomeSecondary.getRhythm(),
                                    primarySimpleRhythm = settings.metronomeSimpleRhythm.value,
                                    secondarySimpleRhythm = settings.metronomeSimpleRhythmSecondary.value,
                                )

                                scope.launch {
                                    settings.save()
                                    onUpdate(newPreset)
                                }
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                   painter = painterResource(R.drawable.outline_widgets_24),
                                    contentDescription = getString(R.string.presets_widget_create),
                                )
                            },
                            text = { Text(getString(R.string.presets_widget_create)) },
                            onClick = {
                                checked = false

                                val widgetManager = AppWidgetManager.getInstance(context)
                                val widgetProvider = ComponentName(context, ClockWidgetReceiver::class.java)

                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val successCallback = PendingIntent.getActivity(
                                        context,
                                        0,
                                        Intent(context, WidgetConfigurationActivity::class.java)
                                            .putExtra("preset", preset.toJson().toString()),
                                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                    )

                                    widgetManager.requestPinAppWidget(widgetProvider, null, successCallback)
                                }
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Share,
                                    contentDescription = getString(R.string.presets_share),
                                )
                            },
                            text = { Text(getString(R.string.presets_share)) },
                            onClick = {
                                checked = false
                                val file = ChronalApp.getInstance().filesDir.resolve("${preset.name}.chp")
                                val content = preset.toJson().toString()
                                val compressed = ByteArrayOutputStream().use {
                                    GZIPOutputStream(it).use { gzip ->
                                        gzip.write(content.toByteArray(Charsets.UTF_8))
                                    }
                                    it.toByteArray()
                                }
                                val base64 = Base64.encode(compressed, Base64.DEFAULT)
                                file.writeBytes(base64)


                                val uri = FileProvider.getUriForFile(this@PresetActivity, "${packageName}.fileprovider", file)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/octet-stream"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                startActivity(Intent.createChooser(intent, "getString(R.string.presets_share_title)"))
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = getString(R.string.presets_delete),
                                )
                            },
                            text = { Text(getString(R.string.presets_delete)) },
                            onClick = {
                                checked = false
                                onDelete(true)
                            }
                        )
                    }
                }
            )
        }

        Row(
            modifier = Modifier.padding(top = 32.dp)
                .align(Alignment.CenterHorizontally),
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_music_note_24),
                contentDescription = getString(R.string.presets_bpm, preset.state.bpm),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = getString(R.string.presets_bpm, preset.state.bpm),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(32.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = getString(R.string.presets_rhythm_primary),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                RhythmInfo(preset, true)
            }

            val enabled = preset.state.secondaryEnabled
            Column(
                modifier = Modifier.weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if(enabled) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainer)
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = getString(R.string.presets_rhythm_secondary),
                    style = MaterialTheme.typography.titleMedium,
                    color = if(enabled) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                RhythmInfo(preset, false, preset.state.secondaryEnabled)
            }
        }
    }

    @Composable
    fun RhythmInfo(
        preset: MetronomePreset,
        isPrimary: Boolean,
        enabled: Boolean = true
    ) {
        val textColor = if(enabled) {
            if(isPrimary) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
        } else MaterialTheme.colorScheme.onSurface
        val rhythm = if(isPrimary) preset.primaryRhythm else preset.secondaryRhythm
        val simpleRhythm = if(isPrimary) preset.primarySimpleRhythm else preset.secondarySimpleRhythm
        val isAdvanced = simpleRhythm.timeSignature == 0 to 0
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if(!enabled) {
                Box(
                    modifier = Modifier.height(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = getString(R.string.presets_disabled),
                        tint = textColor,
                        modifier = Modifier.size(64.dp)
                            .align(Alignment.Center)
                    )
                }
                return
            }
            val timeSignature = preset.primaryRhythm.measures[0].timeSig
            Box(modifier = Modifier.weight(1f).height(64.dp)) {
                Box(
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    MusicFont.Number.TimeSignature(timeSignature.first, timeSignature.second, textColor)
                }
            }
            Box(
                modifier = Modifier.fillMaxHeight()
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                val subdivision = if(isAdvanced) rhythm.measures[0].timeSig.second else simpleRhythm.subdivision
                val isTuplet = (subdivision and (subdivision - 1)) != 0
                val noteValue = if(!isTuplet) subdivision else (subdivision / (3f / 2f)).toInt()
                val char = MusicFont.Notation.convert(noteValue, false)

                MusicFont.Notation.NoteCentered(
                    note = MusicFont.Notation.entries.find { it.char == char } ?: MusicFont.Notation.N_QUARTER,
                    color = textColor,
                    size = 64.dp,
                    modifier = Modifier.align(Alignment.Center)
                )
                if(isTuplet) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .offset(y = (-16).dp),
                    ) {
                        Box(
                            modifier = Modifier.height(1.dp)
                                .padding(horizontal = 4.dp)
                                .weight(1f)
                                .align(Alignment.CenterVertically)
                                .background(textColor)
                        )
                        Text(
                            text = "3",
                            color = textColor,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.align(Alignment.CenterVertically)
                                .padding(4.dp)
                        )
                        Box(
                            modifier = Modifier.height(1.dp)
                                .padding(horizontal = 4.dp)
                                .weight(1f)
                                .align(Alignment.CenterVertically)
                                .background(textColor)
                        )
                    }
                }
            }
        }
    }
}

