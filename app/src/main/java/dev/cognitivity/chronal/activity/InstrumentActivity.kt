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

package dev.cognitivity.chronal.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.material3.SearchBarDefaults.InputField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.settings.types.json.Instrument
import dev.cognitivity.chronal.settings.types.json.InstrumentCategory
import dev.cognitivity.chronal.settings.types.json.Instruments
import dev.cognitivity.chronal.tuner.NoteSystem
import dev.cognitivity.chronal.tuner.Pitch
import dev.cognitivity.chronal.tuner.PitchClass
import dev.cognitivity.chronal.ui.theme.MetronomeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor

class InstrumentActivity : ComponentActivity() {
    private var editingInstrument: Instrument? = null
    private var editingInstrumentCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MetronomeTheme {
                MainContent()
            }
        }
    }

    @Composable
    fun MainContent() {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = "instruments"
        ) {
            composable("instruments") {
                InstrumentsPage(navController)
            }
            composable("add_instrument",
                enterTransition = { scaleIn() + fadeIn() },
                exitTransition = { scaleOut() + fadeOut() }
            ) {
                AddInstrumentPage(
                    onDismissRequest = {
                        editingInstrument = null
                        editingInstrumentCategory = null
                        navController.popBackStack()
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun InstrumentsPage(navController: NavController) {
        var instruments by remember { mutableStateOf(Settings.INSTRUMENTS.get()) }
        var instrument by remember { mutableStateOf(Settings.PRIMARY_INSTRUMENT.get()) }

        val scope = rememberCoroutineScope()
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

        var searching by remember { mutableStateOf(false) }
        var search by remember { mutableStateOf("") }
        val searchBarState = rememberSearchBarState()

        var resetDialog by remember { mutableStateOf(false) }

        if(searching) {
            BackHandler {
                searching = false
                scope.launch {
                    searchBarState.animateToCollapsed()
                }
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = { Text(text = getString(R.string.instrument_title)) },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                finish()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = getString(R.string.generic_back)
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                resetDialog = true
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_reset_wrench_24),
                                contentDescription = getString(R.string.instrument_reset_instruments_title)
                            )
                        }
                        IconButton(
                            onClick = {
                                searching = true
                                scope.launch {
                                    searchBarState.animateToExpanded()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = getString(R.string.generic_search)
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
            floatingActionButton = {
                SmallExtendedFloatingActionButton(
                    onClick = {
                        navController.navigate("add_instrument")
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = getString(R.string.instrument_add)
                        )
                    },
                    text = {
                        Text(
                            text = getString(R.string.instrument_add),
                            style = MaterialTheme.typography.titleMediumEmphasized
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 56.dp)
            ) {
                instrumentList(instruments.categories, instrument, scope,
                    onSelect = { instrument = it },
                    onEdit = {
                        navController.navigate("add_instrument")
                    },
                    onDelete = {
                        instruments = Settings.INSTRUMENTS.get()
                    }
                )
            }

            ExpandedFullScreenSearchBar(
                inputField = {
                    InputField(
                        query = search,
                        onQueryChange = {
                            search = it
                        },
                        onSearch = { },
                        expanded = true,
                        onExpandedChange = { },
                        enabled = true,
                        placeholder = {
                            Text(getString(R.string.instrument_search))
                        },
                        leadingIcon = {
                            IconButton(
                                onClick = {
                                    searching = false
                                    scope.launch {
                                        searchBarState.animateToCollapsed()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                    contentDescription = getString(R.string.generic_back)
                                )
                            }
                        },
                        trailingIcon = {
                            if (search.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        search = ""
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = getString(R.string.generic_search_clear)
                                    )
                                }
                            }
                        },
                        interactionSource = null,
                    )
                },
                state = searchBarState
            ) {
                val found = arrayListOf<InstrumentCategory>()
                for (category in instruments.categories) {
                    val name = category.name
                    val instruments = category.instruments
                    val filtered = instruments.filter {
                        it.name.contains(search, true)
                                || it.shortened.replace(".", "").contains(search.replace(".", ""), true)
                                || category.name.contains(search, true)
                    }
                    if(filtered.isNotEmpty()) {
                        found.add(InstrumentCategory(name, filtered))
                    }
                }
                LazyColumn {
                    instrumentList(found, instrument, scope,
                        onSelect = { instrument = it },
                        onEdit = {
                            searching = false
                            scope.launch { searchBarState.snapTo(0f) }
                            navController.navigate("add_instrument")
                        },
                        onDelete = {
                            instruments = Settings.INSTRUMENTS.get()
                        }
                    )
                }
            }
        }

        if(resetDialog) {
            AlertDialog(
                onDismissRequest = { resetDialog = false },
                title = { Text(getString(R.string.instrument_reset_instruments_title)) },
                text = { Text(getString(R.string.instrument_reset_instruments_text)) },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            Settings.INSTRUMENTS.save(Instruments.default())
                            instruments = Settings.INSTRUMENTS.get()
                        }
                        resetDialog = false
                    }) {
                        Text(getString(R.string.generic_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { resetDialog = false }) {
                        Text(getString(R.string.generic_cancel))
                    }
                }
            )
        }
    }

    private fun LazyListScope.instrumentList(
        categories: List<InstrumentCategory>,
        selected: Instrument,
        scope: CoroutineScope,
        onSelect: (Instrument) -> Unit,
        onEdit: () -> Unit,
        onDelete: () -> Unit
    ) {
        items(categories) { category ->
            InstrumentCategoryItem(category, selected, scope, onSelect, onEdit, onDelete)
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun InstrumentCategoryItem(
        category: InstrumentCategory,
        selected: Instrument,
        scope: CoroutineScope,
        onSelect: (Instrument) -> Unit,
        onEdit: () -> Unit,
        onDelete: () -> Unit
    ) {
        val instruments = category.instruments

        Surface(
            modifier = Modifier.fillMaxWidth()
                .padding(16.dp, 8.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(20.dp, 8.dp)
                )

                for(instrument in instruments) {
                    val topRounded = instruments.indexOf(instrument) == 0
                    val bottomRounded = instruments.indexOf(instrument) == instruments.size - 1
                    InstrumentItem(instrument, topRounded, bottomRounded, selected,
                        onSelect = {
                            onSelect(instrument)
                            scope.launch {
                                Settings.PRIMARY_INSTRUMENT.save(instrument)
                                Settings.TRANSPOSE_NOTES.save(true)
                            }
                        },
                        onEdit = {
                            editingInstrument = instrument
                            editingInstrumentCategory = category.name
                            onEdit()
                        },
                        onDelete = {
                            scope.launch {
                                val categories = Settings.INSTRUMENTS.get().categories
                                val newCategories = categories.toMutableList()
                                val category = categories.first { it.name == category.name }
                                val newInstruments = category.instruments.toMutableList()
                                newInstruments.remove(instrument)
                                if(newInstruments.isEmpty()) {
                                    newCategories.remove(category)
                                } else {
                                    newCategories[categories.indexOf(category)] = InstrumentCategory(category.name, newInstruments)
                                }
                                Settings.INSTRUMENTS.save(Instruments(newCategories))
                                onDelete()
                            }
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun InstrumentItem(
        instrument: Instrument,
        topRounded: Boolean,
        bottomRounded: Boolean,
        selected: Instrument,
        onSelect: () -> Unit,
        onEdit: () -> Unit,
        onDelete: () -> Unit,
    ) {
        val name = instrument.name
        val transposition = instrument.transposition
        val description = getDescription(transposition)

        val context = LocalContext.current
        val isSelected = instrument == selected

        val topCorner = animateDpAsState(
            targetValue = if(isSelected) 20.dp
                else if(topRounded) 12.dp else 6.dp,
            animationSpec = MaterialTheme.motionScheme.fastSpatialSpec()
        )
        val bottomCorner = animateDpAsState(
            targetValue = if(isSelected) 20.dp
                else if(bottomRounded) 12.dp else 6.dp,
            animationSpec = MaterialTheme.motionScheme.fastSpatialSpec()
        )
        val shape = RoundedCornerShape(
            topStart = topCorner.value,
            topEnd = topCorner.value,
            bottomStart = bottomCorner.value,
            bottomEnd = bottomCorner.value
        )
        val containerColor = animateColorAsState(
            targetValue = if(isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
            animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
        )
        val titleColor = animateColorAsState(
            targetValue = if(isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
        )
        val descriptionColor = animateColorAsState(
            targetValue = if(isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
        )

        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = 1.dp)
                .defaultMinSize(minHeight = 64.dp)
                .clip(shape)
                .background(containerColor.value)
                .clickable {
                    Toast.makeText(context, getString(R.string.instrument_selected, name), Toast.LENGTH_SHORT).show()
                    onSelect()
                }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = titleColor.value
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = descriptionColor.value
                )
            }

            IconButton(
                onClick = onEdit,
            ) {
                Icon(
                    imageVector = if(isSelected) Icons.Default.Edit else Icons.Outlined.Edit,
                    contentDescription = getString(R.string.generic_edit),
                    tint = titleColor.value
                )
            }
            if (isSelected) {
                IconButton(
                    onClick = {}
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = getString(R.string.generic_selected),
                        tint = titleColor.value
                    )
                }
            } else {
                IconButton(
                    onClick = onDelete,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = getString(R.string.generic_delete),
                        tint = titleColor.value
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun AddInstrumentPage(onDismissRequest: () -> Unit) {
        val scope = rememberCoroutineScope()
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

        var name by remember { mutableStateOf(editingInstrument?.name ?: "") }
        var nameError by remember { mutableStateOf(false) }

        var shortened by remember { mutableStateOf(editingInstrument?.shortened ?: "") }
        var shortenedError by remember { mutableStateOf(false) }

        var category by remember { mutableStateOf(editingInstrumentCategory ?: "") }
        var categoryError by remember { mutableStateOf(false) }

        val editingInstrumentPitch = if(editingInstrument != null) semitonesToPitch((editingInstrument ?: return).transposition) else null
        var key by remember { mutableStateOf(editingInstrumentPitch?.pitch ?: PitchClass.C) }

        val editingInstrumentOctave = editingInstrumentPitch?.octave?.minus(4)
        var octaveString by remember { mutableStateOf(editingInstrumentOctave?.toString() ?: "0") }
        var octave: Int? by remember { mutableStateOf(editingInstrumentOctave ?: 0) }
        var octaveError by remember { mutableStateOf(false) }

        Scaffold(
            modifier = Modifier.fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(getString(if(editingInstrument != null) R.string.instrument_edit else R.string.instrument_create_new))
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                onDismissRequest()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = getString(R.string.generic_close)
                            )
                        }
                    },
                    actions = {
                        Button(
                            onClick = {
                                scope.launch {
                                    if(editingInstrument != null) {
                                        editInstrument(
                                            category = editingInstrumentCategory ?: return@launch,
                                            oldInstrument = editingInstrument ?: return@launch,
                                            newInstrument = Instrument(name, shortened, key.toSemitones(octave ?: 0))
                                        )
                                    } else {
                                        saveNewInstrument(
                                            category = category,
                                            instrument = Instrument(name, shortened, key.toSemitones(octave ?: 0))
                                        )
                                    }
                                    onDismissRequest()
                                }
                            },
                            enabled = name.isNotBlank() && shortened.isNotBlank() && octave != null && abs(octave ?: 0) <= 4,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = getString(R.string.generic_confirm)
                            )
                            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                            Text(getString(R.string.generic_confirm))
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                NameField(name, nameError,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp, 24.dp, 6.dp, 6.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                ShortenedField(shortened, shortenedError,
                    onValueChange = {
                        shortened = it
                        shortenedError = it.isBlank()
                    },
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                CategoryField(category, categoryError,
                    onValueChange = {
                        category = it
                        categoryError = it.isBlank()
                    },
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    KeyField(key,
                        onValueChange = { key = it },
                        modifier = Modifier.weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp, 6.dp, 6.dp, 24.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    OctaveField(octaveString, octaveError,
                        onValueChange = {
                            octaveString = it.replace(Regex("[^\\d-]"), "")
                            octave = octaveString.toIntOrNull()
                            octaveError = octaveString.isBlank() || octave == null
                                    || abs(octave ?: return@OctaveField) > 4
                        },
                        modifier = Modifier.weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp, 6.dp, 24.dp, 6.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }

    @Composable
    private fun NameField(name: String, nameError: Boolean, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
        OutlinedTextField(
            value = name,
            onValueChange = onValueChange,
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.outline_text_fields_24),
                    contentDescription = null,
                )
            },
            label = {
                Text(getString(R.string.instrument_create_name))
            },
            supportingText = {
                Text(getString(if (nameError) R.string.generic_required_field else R.string.instrument_create_name_text))
            },
            isError = nameError,
            modifier = modifier,
            singleLine = true
        )
    }

    @Composable
    private fun ShortenedField(shortened: String, shortenedError: Boolean, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
        OutlinedTextField(
            value = shortened,
            onValueChange = onValueChange,
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.outline_short_text_24),
                    contentDescription = null,
                )
            },
            label = {
                Text(getString(R.string.instrument_create_name_shortened))
            },
            supportingText = {
                Text(
                    getString(
                        if (shortenedError) R.string.generic_required_field
                        else R.string.instrument_create_name_shortened_text
                    )
                )
            },
            isError = shortenedError,
            modifier = modifier,
            singleLine = true
        )
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun CategoryField(category: String, categoryError: Boolean, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = modifier
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = onValueChange,
                label = {
                    Text(getString(R.string.instrument_create_category))
                },
                supportingText = {
                    Text(getString(if (categoryError) R.string.generic_required_field else R.string.instrument_create_category_text))
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.outline_shapes_24),
                        contentDescription = null,
                    )
                },
                isError = categoryError,
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true)
                    .fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                val matchingCategories = Settings.INSTRUMENTS.get().categories.map { it.name }
                    .filter { it.contains(category, true) }

                val categories = if(matchingCategories.size == 1 && matchingCategories[0] == category) {
                    Settings.INSTRUMENTS.get().categories.map { it.name }
                } else matchingCategories

                categories.forEach { existingCategory ->
                    DropdownMenuItem(
                        text = { Text(existingCategory) },
                        checkedLeadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Check,
                                modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                                contentDescription = null
                            )
                        },
                        checked = category == existingCategory,
                        onCheckedChange = { onValueChange(existingCategory) },
                        shapes = MenuDefaults.itemShape(categories.indexOf(existingCategory), categories.size),
                        colors = MenuDefaults.selectableItemColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun KeyField(key: PitchClass, onValueChange: (PitchClass) -> Unit, modifier: Modifier = Modifier) {
        val noteSystem = NoteSystem.entries[Settings.NOTE_NAMES.get()]
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = modifier
        ) {
            OutlinedTextField(
                value = noteSystem.getName(key).enharmonic ?: noteSystem.getName(key).name,
                onValueChange = { onValueChange(key) },
                readOnly = true,
                label = {
                    Text(getString(R.string.instrument_create_key))
                },
                supportingText = {
                    Text(getString(R.string.instrument_create_key_text))
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.baseline_music_note_24),
                        contentDescription = null,
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    .fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                val pitches = listOf(
                    PitchClass.C, PitchClass.F, PitchClass.As, PitchClass.Ds, PitchClass.Gs, PitchClass.Cs,
                    PitchClass.Fs, PitchClass.B, PitchClass.E, PitchClass.A, PitchClass.D, PitchClass.G
                )
                pitches.forEach { pitch ->
                    val pitchName = noteSystem.getName(pitch).enharmonic ?: noteSystem.getName(pitch).name
                    DropdownMenuItem(
                        text = { Text(pitchName) },
                        checkedLeadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Check,
                                modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                                contentDescription = null
                            )
                        },
                        checked = key == pitch,
                        onCheckedChange = { onValueChange(pitch) },
                        shapes = MenuDefaults.itemShape(PitchClass.entries.indexOf(pitch), PitchClass.entries.size),
                        colors = MenuDefaults.selectableItemColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    )
                }
            }
        }
    }
    @Composable
    private fun OctaveField(octaveString: String, octaveError: Boolean, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
        Box(
            modifier = modifier,
        ) {
            OutlinedTextField(
                value = octaveString,
                onValueChange = onValueChange,
                label = {
                    Text(getString(R.string.instrument_create_octave))
                },
                supportingText = {
                    Text(getString(
                        if(octaveString.isEmpty()) {
                            if(octaveError) R.string.generic_required_field
                            else R.string.instrument_create_octave_text
                        }
                        else if(octaveString.toIntOrNull() == null) R.string.generic_number_invalid
                        else if(abs(octaveString.toInt()) > 4) R.string.instrument_create_octave_range
                        else R.string.instrument_create_octave_text
                    ))
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.outline_swap_vert_24),
                        contentDescription = null,
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = octaveError ||
                        (octaveString.isNotEmpty() && (octaveString.toIntOrNull() == null || abs(octaveString.toInt()) > 4)),
                singleLine = true
            )
        }
    }

    private suspend fun editInstrument(category: String, oldInstrument: Instrument, newInstrument: Instrument) {
        val categories = Settings.INSTRUMENTS.get().categories
        val newCategories = categories.toMutableList()
        val category = categories.firstOrNull { it.name == category } ?: return
        val newInstruments = category.instruments.toMutableList()
        newInstruments[newInstruments.indexOf(oldInstrument)] = newInstrument
        newCategories[newCategories.indexOf(category)] = category.copy(instruments = newInstruments)

        Settings.TRANSPOSE_NOTES.save(true)
        Settings.PRIMARY_INSTRUMENT.save(newInstrument)
        Settings.INSTRUMENTS.set(Instruments(newCategories))
    }

    private suspend fun saveNewInstrument(category: String, instrument: Instrument) {
        val categories = Settings.INSTRUMENTS.get().categories
        val newCategories = categories.toMutableList()
        
        if(categories.none { it.name == category }) {
            newCategories.add(InstrumentCategory(category, listOf(instrument)))
        } else {
            val category = categories.first { it.name == category }
            val newInstruments = category.instruments.toMutableList()
            newInstruments.add(instrument)
            newCategories[categories.indexOf(category)] = InstrumentCategory(category.name, newInstruments)
        }

        val newInstruments = Instruments(
            categories = newCategories
        )
        Settings.TRANSPOSE_NOTES.save(true)
        Settings.PRIMARY_INSTRUMENT.save(instrument)
        Settings.INSTRUMENTS.save(newInstruments)
    }

    private fun semitonesToPitch(semitones: Int): Pitch {
        return Pitch(
            pitch = PitchClass.fromSemitone(semitones),
            octave = floor(semitones / 12.0).toInt() + 4
        )
    }

    private fun getDescription(transposition: Int): String {
        val pitch = semitonesToPitch(transposition)

        val c4 = Pitch(
            pitch = PitchClass.C,
            octave = 4
        ).toDisplayName(octaveVisible = true).name
        val noteName = pitch.toDisplayName(octaveVisible = true).enharmonic ?: pitch.toDisplayName(octaveVisible = true).name

        return getString(R.string.instrument_tuning, c4, noteName)
    }
}