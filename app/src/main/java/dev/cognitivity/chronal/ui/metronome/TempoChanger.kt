package dev.cognitivity.chronal.ui.metronome

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.ui.metronome.windows.metronome
import dev.cognitivity.chronal.ui.metronome.windows.setBPM
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BoxScope.TempoChanger() {
    Column(
        modifier = Modifier.align(Alignment.Center)
    ) {
        Row {
            val scope = rememberCoroutineScope()

            Column(
                modifier = Modifier.align(Alignment.CenterVertically)
                    .padding(2.dp, 0.dp, 2.dp, 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowUp,
                    contentDescription = context.getString(R.string.metronome_increase_tempo),
                    modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    var isHeld = true
                                    scope.launch {
                                        setBPM(metronome.bpm + 1)
                                        delay(500)
                                        while (isHeld) {
                                            setBPM(metronome.bpm + 1)
                                            delay(50)
                                        }
                                    }
                                    tryAwaitRelease()
                                    isHeld = false
                                }
                            )
                        },
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = "Decrease tempo",
                    modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    var isHeld = true
                                    scope.launch {
                                        setBPM(metronome.bpm - 1)

                                        delay(500)
                                        while (isHeld) {
                                            setBPM(metronome.bpm - 1)
                                            delay(50)
                                        }
                                    }
                                    tryAwaitRelease()
                                    isHeld = false
                                }
                            )
                        },
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = metronome.bpm.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Visible
            )
            Text(
                modifier = Modifier.offset(y = (-4).dp)
                    .align(Alignment.Bottom),
                text = context.getString(R.string.metronome_bpm),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Visible
            )
        }

        val resId = when (metronome.bpm) {
            in 1  ..24  -> R.string.metronome_tempo_larghissimo
            in 25 ..39  -> R.string.metronome_tempo_grave
            in 40 ..49  -> R.string.metronome_tempo_lento
            in 50 ..59  -> R.string.metronome_tempo_largo
            in 60 ..66  -> R.string.metronome_tempo_larghetto
            in 67 ..76  -> R.string.metronome_tempo_adagio
            in 77 ..108 -> R.string.metronome_tempo_andante
            in 109..120 -> R.string.metronome_tempo_moderato
            in 121..132 -> R.string.metronome_tempo_allegretto
            in 133..143 -> R.string.metronome_tempo_allegro
            in 144..159 -> R.string.metronome_tempo_vivace
            in 160..199 -> R.string.metronome_tempo_presto
            in 200..500 -> R.string.metronome_tempo_prestissimo
            else -> R.string.metronome_tempo_unknown
        }

        Text(
            text = context.getString(resId),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.tertiary,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.align(Alignment.CenterHorizontally)
                .offset(y = (-8).dp)
        )
    }
}
