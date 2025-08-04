package dev.cognitivity.chronal.ui.metronome.sheets

import android.view.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.MusicFont
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.SimpleRhythm
import dev.cognitivity.chronal.toSp
import kotlin.math.pow

@Composable
fun EditSubdivision(window: Window, primary: Boolean, expanded: Boolean) {
    val setting = if(primary) ChronalApp.getInstance().settings.metronomeSimpleRhythm else ChronalApp.getInstance().settings.metronomeSimpleRhythmSecondary
    var value by remember { mutableStateOf(setting.value) }

    if(!expanded) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier.align(Alignment.CenterHorizontally)
                    .padding(4.dp)
            ) {
                ClockPreview(primary, value)
            }

            LazyHorizontalGrid(
                modifier = Modifier.fillMaxWidth().height(192.dp),
                rows = GridCells.Fixed(2),
            ) {
                items(10) { i ->
                    SubdivisionNote(window, primary, value, i) {
                        value = it
                    }
                }
            }
            EmphasisSelector(window, primary, value) {
                value = it
            }
        }
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier.align(Alignment.CenterVertically)
                    .padding(4.dp)
            ) {
                ClockPreview(primary, value)
            }

            LazyVerticalGrid(
                modifier = Modifier.width(192.dp)
                    .fillMaxHeight(),
                columns = GridCells.Fixed(2),
            ) {
                items(10) { i ->
                    SubdivisionNote(window, primary, value, i) {
                        value = it
                    }
                }
            }
            Column(
                modifier = Modifier.fillMaxHeight()
                    .width(IntrinsicSize.Min)
            ) {
                EmphasisSelector(window, primary, value) {
                    value = it
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun SubdivisionNote(window: Window, primary: Boolean, value: SimpleRhythm, i: Int, onUpdate: (SimpleRhythm) -> Unit = {}) {
    val ltr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val isTuplet = i >= 6
    val noteValue = if(isTuplet) 2.0.pow(i - 4.0).toInt() else 2.0.pow(i.toDouble()).toInt()
    val tupletValue = if(isTuplet) 3 else 2
    val duration = (noteValue * (tupletValue.toDouble() / 2)).toInt()
    val selected = value.subdivision == duration

    val containerColor = if(selected) {
        if(primary) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.tertiaryContainer
    } else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if(selected) {
        if(primary) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onTertiaryContainer
    } else MaterialTheme.colorScheme.onSecondaryContainer
    Box(
        modifier = Modifier.padding(8.dp)
            .fillMaxHeight()
            .aspectRatio(1f, matchHeightConstraintsFirst = true)
            .background(containerColor, RoundedCornerShape(8.dp))
            .clickable {
                val oldValue = value
                var newValue = value.copy(subdivision = duration)
                if(!setRhythm(window, newValue, primary, retry = false)) {
                    newValue = oldValue
                }
                onUpdate(newValue)
            },
    ) {
        val char = MusicFont.Notation.convert(noteValue)
        val offset = MusicFont.Notation.entries.find { it.char == char }?.offset ?: Offset(0f, 0f)
        val size = if(isTuplet) 48.dp else 64.dp
        Text(
            text = char.toString(),
            color = textColor,
            style = TextStyle(
                fontFamily = FontFamily(Font(R.font.bravuratext)),
                fontSize = size.toSp()
            ),
            modifier = Modifier.align(Alignment.Center)
                .offset(size * offset.x * (if(ltr) 1 else -1), size * offset.y)
                .offset(0.dp, if(isTuplet) 8.dp else 0.dp)
        )

        if(isTuplet) {
            Row(modifier = Modifier.align(Alignment.TopCenter)) {
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
                    style = MaterialTheme.typography.labelLarge,
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

@Composable
fun ColumnScope.EmphasisSelector(window: Window, primary: Boolean, value: SimpleRhythm, onUpdate: (SimpleRhythm) -> Unit = {}) {
    Text(context.getString(R.string.metronome_emphasis),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.align(Alignment.CenterHorizontally)
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
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
                        selectedColor = if (primary) MaterialTheme.colorScheme.primary
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

        EmphasisButton(R.string.metronome_emphasis_all, value.emphasis == 0) {
            val newValue = value.copy(emphasis = 0)
            onUpdate(newValue)
            setRhythm(window, newValue, primary, retry = false)
        }
        EmphasisButton(R.string.metronome_emphasis_none, value.emphasis == 1) {
            val newValue = value.copy(emphasis = 1)
            onUpdate(newValue)
            setRhythm(window, newValue, primary, retry = false)
        }
        EmphasisButton(R.string.metronome_emphasis_first, value.emphasis == 2) {
            val newValue = value.copy(emphasis = 2)
            onUpdate(newValue)
            setRhythm(window, newValue, primary, retry = false)
        }
        EmphasisButton(R.string.metronome_emphasis_alternate, value.emphasis == 3) {
            val newValue = value.copy(emphasis = 3)
            onUpdate(newValue)
            setRhythm(window, newValue, primary, retry = false)
        }
    }
}
