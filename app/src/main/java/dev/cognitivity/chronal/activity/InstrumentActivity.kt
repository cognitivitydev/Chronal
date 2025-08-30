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

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.Instrument
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.ui.WavyHorizontalLine
import dev.cognitivity.chronal.ui.theme.MetronomeTheme
import dev.cognitivity.chronal.ui.tuner.windows.keyToSemitones
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs

class InstrumentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MetronomeTheme {
                MainContent()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainContent() {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
        val json = resources.openRawResource(R.raw.instruments).bufferedReader().use { it.readText() }
        val parsed = Gson().fromJson(json, JsonObject::class.java)
        var searching by remember { mutableStateOf(false) }
        var search by remember { mutableStateOf("") }
        var editPopup by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val searchBarState = rememberSearchBarState()

        var setting by remember {
            mutableStateOf(ChronalApp.getInstance().settings.primaryInstrument.value)
        }

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
                        IconButton(
                            onClick = {
                                editPopup = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = getString(R.string.instrument_add)
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            }
        ) { innerPadding ->

            if(editPopup) {
                var name by remember { mutableStateOf("") }
                var nameError by remember { mutableStateOf(false) }
                var shortened by remember { mutableStateOf("") }
                var shortenedError by remember { mutableStateOf(false) }
                var key by remember { mutableStateOf("") }
                var displayKey by remember { mutableStateOf("") }
                var keyError by remember { mutableStateOf(false) }
                var octave by remember { mutableStateOf("") }
                var octaveError by remember { mutableStateOf(false) }
                var expanded by remember { mutableStateOf(false) }

                val options = listOf("C", "F", "B♭", "E♭", "A♭", "D♭ / C♯", "G♭ / F♯", "C♭ / B", "E", "A", "D", "G")

                val octaveString by remember(octave) { mutableStateOf(if(octave.isEmpty() || octave.toIntOrNull() == null) ""
                    else if (octave.toInt() > 0) {
                        if(octave.toInt() == 1) getString(R.string.instrument_octave_up, octave.toInt())
                        else getString(R.string.instrument_octaves_up, octave.toInt())
                    } else {
                        if(abs(octave.toInt()) == 1) getString(R.string.instrument_octave_down, abs(octave.toInt()))
                        else getString(R.string.instrument_octaves_down, abs(octave.toInt()))
                    }
                ) }

                val description by remember(key, octave) {
                    mutableStateOf(
                        if(key.isEmpty() || octave.toIntOrNull() == null) {
                            getString(R.string.instrument_tune_unknown)
                        } else if (key == "C" && octave.toInt() == 0) {
                            getString(R.string.instrument_tune_concert)
                        } else if (key == "C") {
                            getString(R.string.instrument_tune_octave, octaveString, 4 + octave.toInt())
                        } else if (octave.toInt() == 0) {
                            getString(R.string.instrument_tune_key, key, key)
                        } else {
                            getString(R.string.instrument_tune_key_octave, key, octave, key, 4 + octave.toInt())
                        }
                    )
                }

                AlertDialog(
                    onDismissRequest = { },
                    confirmButton = @Composable {
                        TextButton(onClick = {
//                            editPopup = false
                            nameError = name.isEmpty()
                            shortenedError = shortened.isEmpty()
                            keyError = key.isEmpty()
                            octaveError = octave.isEmpty()

                            if(!nameError && !shortenedError && !keyError && !octaveError
                                && octave.toIntOrNull() != null && abs(octave.toInt()) <= 4
                            ) {
                                CoroutineScope(Dispatchers.Default).launch {
                                    setting.name = name
                                    setting.shortened = shortened
                                    setting.transposition = keyToSemitones(key, octave.toInt())
                                }
                                editPopup = false
                            }
                        }) {
                            Text(getString(R.string.generic_save))
                        }
                    },
                    dismissButton = @Composable {
                        TextButton(onClick = {
                            editPopup = false
                        }) {
                            Text(getString(R.string.generic_discard))
                        }
                    },
                    title = @Composable {
                        Text(getString(R.string.instrument_create_new))
                    },
                    text = @Composable {
                        Column {
                            OutlinedTextField(
                                value = name,
                                onValueChange = {
                                    name = it
                                    nameError = false
                                },
                                label = {
                                    Text(getString(R.string.instrument_create_name))
                                },
                                supportingText = {
                                    if(nameError) {
                                        Text(getString(R.string.generic_required_field))
                                    }
                                },
                                isError = nameError,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = shortened,
                                onValueChange = {
                                    shortened = it
                                    shortenedError = false
                                },
                                label = {
                                    Text(getString(R.string.instrument_create_name_shortened))
                                },
                                supportingText = {
                                    Text(getString(
                                        if(shortenedError) R.string.generic_required_field
                                        else R.string.instrument_create_name_shortened_text
                                    ))
                                },
                                isError = shortenedError,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(Modifier.height(12.dp))
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = it }
                            ) {
                                OutlinedTextField(
                                    value = displayKey,
                                    onValueChange = {
                                        key = it.replace(Regex(" / .*"), "")
                                        displayKey = it
                                        keyError = false
                                    },
                                    readOnly = true,
                                    label = {
                                        Text("Key")
                                    },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                    },
                                    supportingText = {
                                        if(keyError) {
                                            Text(getString(R.string.generic_required_field))
                                        }
                                    },
                                    isError = keyError,
                                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                                        .fillMaxWidth(),
                                    singleLine = true
                                )

                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    options.forEach { option ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(option,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                            },
                                            onClick = {
                                                key = option.replace(Regex(" / .*"), "")
                                                displayKey = option
                                                keyError = false
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = octave,
                                onValueChange = {
                                    octave = it.replace(Regex("[^\\d-]"), "")
                                    octaveError = false
                                },
                                label = {
                                    Text(getString(R.string.instrument_create_octave))
                                },
                                supportingText = {
                                    Text(getString(
                                        if(octave.isEmpty()) {
                                            if(octaveError) R.string.generic_required_field
                                            else R.string.instrument_create_octave_text
                                        }
                                        else if(octave.toIntOrNull() == null) R.string.generic_number_invalid
                                        else if(abs(octave.toInt()) > 4) R.string.instrument_create_octave_range
                                        else R.string.instrument_create_octave_text
                                    ))
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                isError = octaveError ||
                                        (octave.isNotEmpty() && (octave.toIntOrNull() == null || abs(octave.toInt()) > 4)),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            WavyHorizontalLine(
                                modifier = Modifier.padding(8.dp, 16.dp)
                                    .fillMaxWidth()
                                    .align(Alignment.CenterHorizontally)
                            )
                            Text(
                                text = description,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    },
                    properties = DialogProperties(
                        dismissOnBackPress = false,
                        dismissOnClickOutside = false
                    )
                )
            }

            LazyColumn(
                modifier = Modifier.padding(innerPadding)
                    .fillMaxSize()
            ) {
                for ((_, value) in parsed.entrySet()) {
                    val type = value.asJsonObject
                    val name = type.get("name").asString
                    val instruments = type.get("instruments").asJsonArray

                    item {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth()
                                .padding(20.dp, 8.dp)
                        )
                    }

                    for (element in instruments) {
                        val instrument = element.asJsonObject

                        item {
                            InstrumentItem(
                                instrument = instrument,
                                selected = setting,
                                onSelect = { newInstrument ->
                                    setting = newInstrument
                                    scope.launch {
                                        ChronalApp.getInstance().settings.primaryInstrument.value = newInstrument
                                        ChronalApp.getInstance().settings.save()
                                    }
                                }
                            )
                        }
                    }
                }
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
                state = searchBarState,

            ) {
                LazyColumn {
                    val found = JsonObject()
                    for ((key, value) in parsed.entrySet()) {
                        val type = value.asJsonObject
                        val name = type.get("name").asString
                        val instruments = type.get("instruments").asJsonArray

                        found.add(key, JsonObject())
                        val category = found.get(key).asJsonObject
                        category.addProperty("name", name)
                        category.add("instruments", JsonArray())

                        if (search.isEmpty() || name.contains(search, true)) {
                            category["instruments"].asJsonArray.addAll(instruments.map { it.asJsonObject })
                        } else {
                            category["instruments"].asJsonArray.addAll(instruments.map { it.asJsonObject }.filter {
                                it.get("name").asString.contains(search, true)
                                        || it.get("shortened").asString.contains(search, true)
                                        || it.get("shortened").asString.replace(".", "")
                                    .contains(search, true)
                            })
                        }

                        if (category["instruments"].asJsonArray.size() == 0) {
                            found.remove(key)
                        }
                    }

                    items(found.entrySet().size) { index ->
                        val type = found.entrySet().elementAt(index).value.asJsonObject
                        val name = type.get("name").asString
                        val instruments = type.get("instruments").asJsonArray

                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.fillMaxWidth()
                                .padding(20.dp, 8.dp)
                        )

                        for (element in instruments) {
                            val instrument = element.asJsonObject

                            InstrumentItem(instrument, setting) { newSetting ->
                                setting = newSetting
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun InstrumentItem(
        instrument: JsonObject,
        selected: Instrument,
        onSelect: (Instrument) -> Unit
    ) {
        val name = instrument.get("name").asString
        val shortened = instrument.get("shortened").asString
        val transposition = instrument.get("transposition").asJsonObject
        val key = transposition.get("key").asString
        val octave = transposition.get("octave").asInt

        val octaveString = getString(
            if (octave > 0) {
                if(octave == 1) R.string.instrument_octave_up else R.string.instrument_octaves_up
            } else {
                if(abs(octave) == 1) R.string.instrument_octave_down else R.string.instrument_octaves_down
            },
            abs(octave)
        )

        val description = if(key == "C" && octave == 0) {
            getString(R.string.instrument_tune_concert)
        } else if (key == "C") {
            getString(R.string.instrument_tune_octave, octaveString, 4 + octave)
        } else if (octave == 0) {
            getString(R.string.instrument_tune_key, key, key)
        } else {
            getString(R.string.instrument_tune_key_octave, key, octaveString, key, 4 + octave)
        }

        val context = LocalContext.current
        val isSelected = selected.name == name && selected.transposition == keyToSemitones(key, octave)

        Row(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .clickable {
                    Toast.makeText(context, getString(R.string.instrument_selected, name), Toast.LENGTH_SHORT).show()
                    onSelect(Instrument(name, shortened, keyToSemitones(key, octave)))
                }
                .padding(16.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = getString(R.string.generic_selected),
                    modifier = Modifier.align(Alignment.CenterVertically)
                        .padding(8.dp),
                    tint = MaterialTheme.colorScheme.surfaceTint
                )
            }
        }
    }

    private fun JsonArray.addAll(list: List<JsonObject>) {
        for(item in list) {
            this.add(item)
        }
    }

    private tailrec fun Context.getActivityWindow(): Window? =
        when (this) {
            is Activity -> window
            is ContextWrapper -> baseContext.getActivityWindow()
            else -> null
        }
}