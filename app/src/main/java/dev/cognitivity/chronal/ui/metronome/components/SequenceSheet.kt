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

package dev.cognitivity.chronal.ui.metronome.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.settings.types.json.SequenceStep
import dev.cognitivity.chronal.ui.metronome.MetronomeViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private const val MAX_BARS = 99

private data class SequenceStepEntry(val id: Long, val step: SequenceStep)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SequenceSheet(viewModel: MetronomeViewModel, onDismissRequest: () -> Unit) {
    val sequence by viewModel.sequence.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val position by viewModel.sequencePosition.collectAsState()

    var nextId by remember { mutableLongStateOf(0L) }
    val stepEntries = remember {
        mutableStateListOf<SequenceStepEntry>().apply {
            viewModel.sequence.value.steps.forEach { step ->
                add(SequenceStepEntry(nextId++, step))
            }
        }
    }

    fun commitSteps() {
        viewModel.setSequenceSteps(stepEntries.map { it.step })
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_playlist_play_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.metronome_sequence),
                    style = MaterialTheme.typography.headlineMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                        .padding(horizontal = 8.dp)
                )
                Switch(
                    checked = sequence.enabled,
                    enabled = stepEntries.isNotEmpty(),
                    onCheckedChange = { viewModel.setSequenceEnabled(it) }
                )
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

            if (stepEntries.isEmpty()) {
                Text(
                    text = stringResource(R.string.metronome_sequence_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                )
            } else {
                val lazyListState = rememberLazyListState()
                val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
                    stepEntries.add(to.index, stepEntries.removeAt(from.index))
                    commitSteps()
                }

                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxWidth()
                        .weight(1f, fill = false)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    itemsIndexed(stepEntries, key = { _, entry -> entry.id }) { index, entry ->
                        ReorderableItem(reorderableState, key = entry.id) {
                            val isActive = sequence.enabled && position?.stepIndex == index
                            SequenceStepRow(
                                viewModel = viewModel,
                                entry = entry,
                                topRounded = index == 0,
                                bottomRounded = index == stepEntries.size - 1,
                                isActive = isActive,
                                activeBar = if (isActive) position?.bar else null,
                                dragHandle = {
                                    Icon(
                                        painter = painterResource(R.drawable.outline_drag_handle_24),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.padding(start = 12.dp)
                                            .draggableHandle()
                                    )
                                },
                                onStepChanged = { newStep ->
                                    stepEntries[index] = entry.copy(step = newStep)
                                    commitSteps()
                                },
                                onDelete = {
                                    stepEntries.removeAt(index)
                                    commitSteps()
                                }
                            )
                        }
                    }
                }
            }

            FilledTonalButton(
                onClick = {
                    val trackIndex = stepEntries.lastOrNull()?.step?.trackIndex?.coerceIn(0, tracks.size - 1) ?: 0
                    stepEntries.add(SequenceStepEntry(nextId++, SequenceStep(trackIndex = trackIndex, bars = 1)))
                    commitSteps()
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
                    .padding(vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Text(
                    text = stringResource(R.string.metronome_sequence_add_step),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SequenceStepRow(
    viewModel: MetronomeViewModel,
    entry: SequenceStepEntry,
    topRounded: Boolean,
    bottomRounded: Boolean,
    isActive: Boolean,
    activeBar: Int?,
    dragHandle: @Composable () -> Unit,
    onStepChanged: (SequenceStep) -> Unit,
    onDelete: () -> Unit,
) {
    val tracks by viewModel.tracks.collectAsState()
    val step = entry.step
    val track = tracks.getOrNull(step.trackIndex)
    var trackPickerExpanded by remember { mutableStateOf(false) }

    val shape = RoundedCornerShape(
        topStart = if (topRounded) 12.dp else 6.dp,
        topEnd = if (topRounded) 12.dp else 6.dp,
        bottomStart = if (bottomRounded) 12.dp else 6.dp,
        bottomEnd = if (bottomRounded) 12.dp else 6.dp
    )

    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(shape)
            .background(
                if (isActive) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        dragHandle()

        Box(
            modifier = Modifier.weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    .clickable { trackPickerExpanded = true }
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (track != null) {
                    Box(
                        modifier = Modifier.size(12.dp)
                            .clip(CircleShape)
                            .background(track.color.getPalette().color)
                    )
                }
                Text(
                    text = track?.name ?: "—",
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            DropdownMenu(
                expanded = trackPickerExpanded,
                onDismissRequest = { trackPickerExpanded = false }
            ) {
                for ((index, option) in tracks.withIndex()) {
                    DropdownMenuItem(
                        text = { Text(option.name) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier.size(12.dp)
                                    .clip(CircleShape)
                                    .background(option.color.getPalette().color)
                            )
                        },
                        onClick = {
                            trackPickerExpanded = false
                            onStepChanged(step.copy(trackIndex = index))
                        }
                    )
                }
            }
        }

        if (isActive && activeBar != null) {
            Text(
                text = "$activeBar/${step.bars}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        IconButton(
            onClick = { onStepChanged(step.copy(bars = (step.bars - 1).coerceAtLeast(1))) },
            enabled = step.bars > 1
        ) {
            Text(
                text = "−",
                style = MaterialTheme.typography.titleMediumEmphasized
            )
        }
        Text(
            text = pluralStringResource(R.plurals.metronome_sequence_bars, step.bars, step.bars),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        IconButton(
            onClick = { onStepChanged(step.copy(bars = (step.bars + 1).coerceAtMost(MAX_BARS))) },
            enabled = step.bars < MAX_BARS
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.titleMediumEmphasized
            )
        }

        IconButton(
            onClick = onDelete
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.metronome_sequence_remove_step),
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}
