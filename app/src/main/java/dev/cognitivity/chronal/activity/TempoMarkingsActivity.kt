package dev.cognitivity.chronal.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.TempoMarking
import dev.cognitivity.chronal.ui.theme.MetronomeTheme
import kotlinx.coroutines.launch

class TempoMarkingsActivity : ComponentActivity() {
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
        val scope = rememberCoroutineScope()

        var showAddDialog by remember { mutableStateOf(false) }
        var showResetDialog by remember { mutableStateOf(false) }

        val settings = ChronalApp.getInstance().settings
        val setting = settings.tempoMarkings
        val markings = remember { mutableStateListOf<TempoMarking>().apply { addAll(setting.value) } }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(getString(R.string.tempo_markings_title))
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
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                showResetDialog = true
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_reset_wrench_24),
                                contentDescription = getString(R.string.tempo_markings_reset_title),
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        showAddDialog = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = getString(R.string.tempo_markings_add_title)
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(markings.size) { i ->
                    var marking = markings[i]

                    var showEditDialog by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable {
                                showEditDialog = true
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = marking.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = getString(R.string.tempo_marking_range, marking.range.first, marking.range.last),
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
                    if(showEditDialog) {
                        MarkingDialog(
                            name = marking.name,
                            range = marking.range,
                            onDismiss = { showEditDialog = false },
                            onConfirm = { new ->
                                if(new == null) {
                                    markings.removeAt(i)
                                    setting.value = markings
                                } else {
                                    marking = new
                                    markings[i] = new
                                    setting.value = markings.toMutableList()
                                }
                                scope.launch {
                                    settings.save()
                                    showEditDialog = false
                                }
                            }
                        )
                    }
                }
                item {
                    Spacer(Modifier.height(64.dp))
                }
            }
            if (showAddDialog) {
                MarkingDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { new ->
                        if(new == null) {
                            showAddDialog = false
                            return@MarkingDialog
                        }
                        markings.add(new)
                        setting.value = markings
                        scope.launch {
                            settings.save()
                            showAddDialog = false
                        }
                    }
                )
            }
            if (showResetDialog) {
                AlertDialog(
                    onDismissRequest = { showResetDialog = false },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.outline_warning_24),
                            contentDescription = getString(R.string.tempo_markings_reset_title)
                        )
                    },
                    title = { Text(getString(R.string.tempo_markings_reset_title)) },
                    text = { Text(getString(R.string.tempo_markings_reset_text)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                markings.clear()
                                markings.addAll(setting.default)
                                setting.value = markings
                                scope.launch {
                                    settings.save()
                                    showResetDialog = false
                                }
                            }
                        ) {
                            Text(getString(R.string.generic_reset))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showResetDialog = false }
                        ) {
                            Text(getString(R.string.generic_cancel))
                        }
                    }
                )
            }
        }
    }

    @Composable
    fun MarkingDialog(name: String? = null, range: IntRange? = null, onDismiss: () -> Unit, onConfirm: (TempoMarking?) -> Unit) {
        var nameText by remember { mutableStateOf(name ?: "") }
        var minText by remember { mutableStateOf(range?.first?.toString() ?: "") }
        var maxText by remember { mutableStateOf(range?.last?.toString() ?: "") }

        var nameErrorMissing by remember { mutableStateOf(false) }

        var minErrorMissing by remember { mutableStateOf(false) }
        val minErrorInvalid = minText.toIntOrNull() == null
        val minErrorLow = (minText.toIntOrNull() ?: 0) < 1
        val minErrorHigh = (minText.toIntOrNull() ?: 0) > 500
        val isErrorMin = minErrorMissing || (minText.isNotEmpty() && (minErrorInvalid || minErrorLow || minErrorHigh))

        var maxErrorMissing by remember { mutableStateOf(false) }
        val maxErrorInvalid = maxText.toIntOrNull() == null
        val maxErrorLow = (maxText.toIntOrNull() ?: 0) < 1
        val maxErrorMin = (maxText.toIntOrNull() ?: 0) < (minText.toIntOrNull() ?: 1)
        val maxErrorHigh = (maxText.toIntOrNull() ?: 0) > 500
        val isErrorMax = maxErrorMissing || (maxText.isNotEmpty() && (maxErrorInvalid || maxErrorLow || maxErrorMin || maxErrorHigh))

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(getString(if(name != null) R.string.tempo_markings_edit_title else R.string.tempo_markings_add_title))
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = {
                            nameText = it
                            nameErrorMissing = it.isEmpty()
                        },
                        label = { Text(getString(R.string.tempo_markings_add_name)) },
                        placeholder = { Text("Andante") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = nameErrorMissing,
                        supportingText = {
                            if(nameErrorMissing) {
                                Text(getString(R.string.tempo_markings_add_name_error))
                            }
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = minText,
                        onValueChange = {
                            minText = it
                            minErrorMissing = it.isEmpty()
                        },
                        label = { Text(getString(R.string.tempo_markings_add_min)) },
                        placeholder = { Text("1") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        isError = isErrorMin,
                        supportingText = {
                            if(minText.isEmpty() && !minErrorMissing) return@OutlinedTextField
                            if (minErrorMissing || minErrorInvalid) {
                                Text(getString(R.string.tempo_markings_add_error_invalid))
                            } else if (minErrorLow) {
                                Text(getString(R.string.tempo_markings_add_error_low))
                            } else if (minErrorHigh) {
                                Text(getString(R.string.tempo_markings_add_error_high))
                            }
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = maxText,
                        onValueChange = {
                            maxText = it
                            maxErrorMissing = it.isEmpty()
                        },
                        label = { Text(getString(R.string.tempo_markings_add_max)) },
                        placeholder = { Text("500") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        isError = isErrorMax,
                        supportingText = {
                            if(maxText.isEmpty() && !maxErrorMissing) return@OutlinedTextField
                            if (maxErrorMissing || maxErrorInvalid) {
                                Text(getString(R.string.tempo_markings_add_error_invalid))
                            } else if (maxErrorMin) {
                                Text(getString(R.string.tempo_markings_add_error_min, minText.toIntOrNull() ?: 1))
                            } else if (maxErrorLow) {
                                Text(getString(R.string.tempo_markings_add_error_low))
                            } else if (maxErrorHigh) {
                                Text(getString(R.string.tempo_markings_add_error_high))
                            }
                        }
                    )
                    if(name != null) {
                        TextButton(
                            onClick = {
                                onConfirm(null)
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = getString(R.string.generic_delete)
                            )
                            Text(getString(R.string.generic_delete))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if(nameText.isEmpty()) {
                            nameErrorMissing = true
                        }
                        if(minText.isEmpty()) {
                            minErrorMissing = true
                        }
                        if(maxText.isEmpty()) {
                            maxErrorMissing = true
                        }
                        if(!minErrorMissing && !maxErrorMissing && !isErrorMin && !isErrorMax) {
                            onConfirm(
                                TempoMarking(
                                    name = nameText,
                                    range = IntRange(
                                        start = minText.toIntOrNull() ?: 1,
                                        endInclusive = maxText.toIntOrNull() ?: 500
                                    )
                                )
                            )
                        }
                    }
                ) {
                    Text(getString(if(name != null) R.string.generic_edit else R.string.generic_add))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismiss()
                    }
                ) {
                    Text(getString(R.string.generic_cancel))
                }
            }
        )
    }
}