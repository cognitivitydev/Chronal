package dev.cognitivity.chronal.ui.metronome

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.MusicFont
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.SimpleRhythm
import dev.cognitivity.chronal.rhythm.metronome.Rhythm
import dev.cognitivity.chronal.toSp
import dev.cognitivity.chronal.ui.metronome.windows.secondaryEnabled
import dev.cognitivity.chronal.ui.metronome.windows.showRhythmPrimary
import dev.cognitivity.chronal.ui.metronome.windows.showRhythmSecondary

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ColumnScope.RhythmButtons(weight: Float, navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val simpleRhythmPrimary = ChronalApp.getInstance().settings.metronomeSimpleRhythm.value
    val parsedRhythmPrimary = Rhythm.deserialize(ChronalApp.getInstance().settings.metronomeRhythm.value)
    val isAdvancedPrimary = simpleRhythmPrimary == SimpleRhythm(0 to 0, 0, 0)

    val simpleRhythmSecondary = ChronalApp.getInstance().settings.metronomeSimpleRhythmSecondary.value
    val parsedRhythmSecondary = Rhythm.deserialize(ChronalApp.getInstance().settings.metronomeRhythmSecondary.value)
    val isAdvancedSecondary = simpleRhythmSecondary == SimpleRhythm(0 to 0, 0, 0)
    Row(
        modifier = Modifier
            .weight(weight)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(modifier = Modifier
            .padding(16.dp, 0.dp, 16.dp, 0.dp)
            .fillMaxWidth()
            .fillMaxHeight(0.5f)
            .weight(1f)
            .align(Alignment.CenterVertically)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable {
                showRhythmPrimary = true
            }
        ) {
            DrawContent(parsedRhythmPrimary, simpleRhythmPrimary, isAdvancedPrimary, MaterialTheme.colorScheme.onPrimaryContainer)
        }
        val secondaryBackground by animateColorAsState(
            targetValue = if(currentRoute == "conductor") MaterialTheme.colorScheme.surfaceContainerLow
                else if(secondaryEnabled) MaterialTheme.colorScheme.tertiaryContainer
                else MaterialTheme.colorScheme.surfaceContainer,
            animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
            label = "secondaryBackground"
        )
        val secondaryText by animateColorAsState(
            targetValue = if(currentRoute == "conductor") MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                else if(secondaryEnabled) MaterialTheme.colorScheme.onTertiaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
            animationSpec = MotionScheme.expressive().defaultEffectsSpec(),
            label = "secondaryText"
        )
        Box(modifier = Modifier
            .padding(16.dp, 0.dp, 16.dp, 0.dp)
            .fillMaxWidth()
            .fillMaxHeight(0.5f)
            .weight(1f)
            .align(Alignment.CenterVertically)
            .clip(RoundedCornerShape(16.dp))
            .background(secondaryBackground)
            .clickable {
                showRhythmSecondary = true
            }
        ) {
            DrawContent(parsedRhythmSecondary, simpleRhythmSecondary, isAdvancedSecondary, secondaryText)
        }
    }
}

@Composable
fun DrawContent(rhythm: Rhythm, simpleRhythm: SimpleRhythm, isAdvanced: Boolean, textColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val timeSignature = rhythm.measures[0].timeSig
        Box(modifier = Modifier.fillMaxHeight(0.75f)) {
            MusicFont.Number.TimeSignature(timeSignature.first, timeSignature.second, textColor)
        }
        Box(
            modifier = Modifier.fillMaxHeight()
                .width(IntrinsicSize.Min)
                .align(Alignment.CenterVertically)
        ) {
            val subdivision = if(isAdvanced) rhythm.measures[0].timeSig.second else simpleRhythm.subdivision
            val isTuplet = (subdivision and (subdivision - 1)) != 0
            val noteValue = if(!isTuplet) subdivision else (subdivision / (3f / 2f)).toInt()
            val char = MusicFont.Notation.convert(noteValue, false)
            val offset = MusicFont.Notation.entries.find { it.char == char }?.offset ?: Offset(0f, 0f)

            Text(
                text = char.toString(),
                color = textColor,
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.bravuratext)),
                    fontSize = 64.dp.toSp()
                ),
                modifier = Modifier.align(Alignment.Center)
                    .offset(64.dp * offset.x, 64.dp * offset.y)
                    .offset(0.dp, if(isTuplet) 8.dp else 0.dp)
            )
            if(isTuplet) {
                Row(
                    modifier = Modifier.align(Alignment.TopCenter)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier.height(1.dp)
                            .padding(horizontal = 8.dp)
                            .weight(1f)
                            .defaultMinSize(minWidth = 16.dp)
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
                            .padding(horizontal = 8.dp)
                            .weight(1f)
                            .defaultMinSize(minWidth = 16.dp)
                            .align(Alignment.CenterVertically)
                            .background(textColor)
                    )
                }
            }
        }
    }
}
