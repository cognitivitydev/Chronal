package dev.cognitivity.chronal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class MusicFont {
    enum class Number(val char: Char) {
        ZERO('\uE080'),
        ONE('\uE081'),
        TWO('\uE082'),
        THREE('\uE083'),
        FOUR('\uE084'),
        FIVE('\uE085'),
        SIX('\uE086'),
        SEVEN('\uE087'),
        EIGHT('\uE088'),
        NINE('\uE089');

        companion object {
            fun convert(string: String): String {
                return string.map {
                    when (it) {
                        '0' -> ZERO.char
                        '1' -> ONE.char
                        '2' -> TWO.char
                        '3' -> THREE.char
                        '4' -> FOUR.char
                        '5' -> FIVE.char
                        '6' -> SIX.char
                        '7' -> SEVEN.char
                        '8' -> EIGHT.char
                        '9' -> NINE.char
                        '/' -> '\n'
                        else -> it
                    }
                }.joinToString("")
            }

            @Composable
            fun TimeSignatureLine(
                number: Int,
                color: Color = Color.White,
            ) {
                val glyphs = convert(number.toString()).toCharArray()

                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val fontSize = this@BoxWithConstraints.maxHeight.toSp()
                    val width = maxHeight / 4

                    val textStyle = TextStyle(
                        fontSize = fontSize,
                        fontFamily = FontFamily(Font(R.font.bravuratext)),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize().offset(x = -width * 0.75f, y = (-maxHeight/3))
                    ) {
                        glyphs.forEach {
                            Text(it.toString(), style = textStyle, color = color, modifier = Modifier.padding(horizontal = width/1.75f))
                        }
                    }
                }
            }

            @Composable
            fun TimeSignature(
                numerator: Int = 4,
                denominator: Int = 4,
                color: Color = Color.White,
                lineSpacing: Dp = 0.dp
            ) {
                val numeratorGlyphs = convert(numerator.toString()).toCharArray()
                val denominatorGlyphs = convert(denominator.toString()).toCharArray()

                BoxWithConstraints(
                    modifier = Modifier.fillMaxHeight().aspectRatio(0.4f, true),
                    contentAlignment = Alignment.Center
                ) {
                    val fontSize = this@BoxWithConstraints.maxHeight.toSp()
                    val width = maxHeight / 4

                    val textStyle = TextStyle(
                        fontSize = fontSize,
                        fontFamily = FontFamily(Font(R.font.bravuratext)),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize().offset(x = -width * 0.75f, y = (-maxHeight/2) - (lineSpacing/2))
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(width),
                        ) {
                            numeratorGlyphs.forEach {
                                Text(it.toString(), style = textStyle, color = color)
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(width),
                            modifier = Modifier.offset(y = fontSize.toDp() / 2.25f + lineSpacing)
                        ) {
                            denominatorGlyphs.forEach {
                                Text(it.toString(), style = textStyle, color = color)
                            }
                        }
                    }
                }
            }
        }
    }
    enum class Notation(val char: Char, val offset: Offset = Offset(0f, 0f)) {
        DOT('\uE1E7'),

        N_WHOLE('\uE1D2', Offset(0f, 0.1f)),
        N_HALF('\uE1D3', Offset(0f, 0.4f)),
        N_QUARTER('\uE1D5', Offset(0f, 0.4f)),
        N_EIGHTH('\uE1D7', Offset(0.08f, 0.4f)),
        N_16TH('\uE1D9', Offset(0.08f, 0.4f)),
        N_32ND('\uE1DB', Offset(0.08f, 0.45f)),
        N_64TH('\uE1DD', Offset(0.08f, 0.5f)),
        N_128TH('\uE1DF', Offset(0.08f, 0.6f)),
        N_256TH('\uE1E1', Offset(0.08f, 0.67f)),
        N_512TH('\uE1E3', Offset(0.08f, 0.75f)),
        N_1024TH('\uE1E5', Offset(0.08f, 0.83f)),

        I_WHOLE('\uE1D2', Offset(0f, 0.1f)),
        I_HALF('\uE1D4', Offset(0f, -0.2f)),
        I_QUARTER('\uE1D6', Offset(0f, -0.2f)),
        I_EIGHTH('\uE1D8', Offset(0f, -0.2f)),
        I_16TH('\uE1DA', Offset(0f, -0.2f)),
        I_32ND('\uE1DC', Offset(0f, -0.225f)),
        I_64TH('\uE1DE', Offset(0f, -0.25f)),
        I_128TH('\uE1E0', Offset(0f, -0.3f)),
        I_256TH('\uE1E2', Offset(0f, -0.33f)),
        I_512TH('\uE1E4', Offset(0f, -0.375f)),
        I_1024TH('\uE1E6', Offset(0f, -0.42f)),

        R_WHOLE('\uE4E3', Offset(0f, 0.1f)),
        R_HALF('\uE4E4', Offset(0f, 0.1f)),
        R_QUARTER('\uE4E5', Offset(0f, 0.075f)),
        R_EIGHTH('\uE4E6', Offset(0f, 0.075f)),
        R_16TH('\uE4E7', Offset(0f, 0.075f)),
        R_32ND('\uE4E8', Offset(0f, 0.075f)),
        R_64TH('\uE4E9', Offset(0f, 0.075f)),
        R_128TH('\uE4EA', Offset(0f, 0.075f)),
        R_256TH('\uE4EB', Offset(0f, 0.075f)),
        R_512TH('\uE4EC', Offset(0f, 0.075f)),
        R_1024TH('\uE4ED', Offset(0f, 0.15f))
        ;

        companion object {
            private val noteMap = mapOf(
                N_WHOLE.char to I_WHOLE.char,
                N_HALF.char to I_HALF.char,
                N_QUARTER.char to I_QUARTER.char,
                N_EIGHTH.char to I_EIGHTH.char,
                N_16TH.char to I_16TH.char,
                N_32ND.char to I_32ND.char,
                N_64TH.char to I_64TH.char,
                N_128TH.char to I_128TH.char,
                N_256TH.char to I_256TH.char,
                N_512TH.char to I_512TH.char,
                N_1024TH.char to I_1024TH.char,
            )

            fun toLength(char: Char): Double {
                return when(char) {
                    N_WHOLE.char -> 1/1.0;       I_WHOLE.char -> 1/1.0
                    N_HALF.char -> 1/2.0;        I_HALF.char -> 1/2.0
                    N_QUARTER.char -> 1/4.0;     I_QUARTER.char -> 1/4.0
                    N_EIGHTH.char -> 1/8.0;      I_EIGHTH.char -> 1/8.0
                    N_16TH.char -> 1/16.0;       I_16TH.char -> 1/16.0
                    N_32ND.char -> 1/32.0;       I_32ND.char -> 1/32.0
                    N_64TH.char -> 1/64.0;       I_64TH.char -> 1/64.0
                    N_128TH.char -> 1/128.0;     I_128TH.char -> 1/128.0
                    N_256TH.char -> 1/256.0;     I_256TH.char -> 1/256.0
                    N_512TH.char -> 1/512.0;     I_512TH.char -> 1/512.0
                    N_1024TH.char -> 1/1024.0;   I_1024TH.char -> 1/1024.0

                    R_WHOLE.char -> -1/1.0;      R_HALF.char -> -1/2.0
                    R_QUARTER.char -> -1/4.0;    R_EIGHTH.char -> -1/8.0
                    R_16TH.char -> -1/16.0;      R_32ND.char -> -1/32.0
                    R_64TH.char -> -1/64.0;      R_128TH.char -> -1/128.0
                    R_256TH.char -> -1/256.0;    R_512TH.char -> -1/512.0
                    R_1024TH.char -> -1/1024.0
                    else -> 0.0
                }
            }

            fun convert(int: Int, rest: Boolean = false): Char {
                if(!rest) {
                    return when (int) {
                        1 -> N_WHOLE.char
                        2 -> N_HALF.char
                        4 -> N_QUARTER.char
                        8 -> N_EIGHTH.char
                        16 -> N_16TH.char
                        32 -> N_32ND.char
                        64 -> N_64TH.char
                        128 -> N_128TH.char
                        256 -> N_256TH.char
                        512 -> N_512TH.char
                        1024 -> N_1024TH.char
                        else -> '?'
                    }
                }
                return when (int) {
                    1 -> R_WHOLE.char
                    2 -> R_HALF.char
                    4 -> R_QUARTER.char
                    8 -> R_EIGHTH.char
                    16 -> R_16TH.char
                    32 -> R_32ND.char
                    64 -> R_64TH.char
                    128 -> R_128TH.char
                    256 -> R_256TH.char
                    512 -> R_512TH.char
                    1024 -> R_1024TH.char
                    else -> '?'
                }
            }
            fun convert(char: Char, rest: Boolean = false): Char {
                if(!rest) {
                    return when (char) {
                        '.' -> DOT.char
                        'W' -> N_WHOLE.char
                        'H' -> N_HALF.char
                        'Q' -> N_QUARTER.char
                        'E' -> N_EIGHTH.char
                        'S' -> N_16TH.char
                        'T' -> N_32ND.char
                        'X' -> N_64TH.char
                        'O' -> N_128TH.char
                        'Z' -> N_256TH.char
                        'F' -> N_512TH.char
                        'M' -> N_1024TH.char

                        'w' -> N_WHOLE.char
                        'h' -> I_HALF.char
                        'q' -> I_QUARTER.char
                        'e' -> I_EIGHTH.char
                        's' -> I_16TH.char
                        't' -> I_32ND.char
                        'x' -> I_64TH.char
                        'o' -> I_128TH.char
                        'z' -> I_256TH.char
                        'f' -> I_512TH.char
                        'm' -> I_1024TH.char
                        else -> '?'
                    }
                }
                return when (char.uppercaseChar()) {
                    'W' -> R_WHOLE.char
                    'H' -> R_HALF.char
                    'Q' -> R_QUARTER.char
                    'E' -> R_EIGHTH.char
                    'S' -> R_16TH.char
                    'T' -> R_32ND.char
                    'X' -> R_64TH.char
                    'O' -> R_128TH.char
                    'Z' -> R_256TH.char
                    'F' -> R_512TH.char
                    'M' -> R_1024TH.char
                    else -> '?'
                }
            }
            fun toLetter(char: Char): Char {
                return when (char) {
                    N_WHOLE.char -> 'W'
                    N_HALF.char -> 'H'
                    N_QUARTER.char -> 'Q'
                    N_EIGHTH.char -> 'E'
                    N_16TH.char -> 'S'
                    N_32ND.char -> 'T'
                    N_64TH.char -> 'X'
                    N_128TH.char -> 'O'
                    N_256TH.char -> 'Z'
                    N_512TH.char -> 'F'
                    N_1024TH.char -> 'M'

                    I_WHOLE.char, R_WHOLE.char -> 'w'
                    I_HALF.char, R_HALF.char -> 'h'
                    I_QUARTER.char, R_QUARTER.char -> 'q'
                    I_EIGHTH.char, R_EIGHTH.char -> 'e'
                    I_16TH.char, R_16TH.char -> 's'
                    I_32ND.char, R_32ND.char -> 't'
                    I_64TH.char, R_64TH.char -> 'x'
                    I_128TH.char, R_128TH.char -> 'o'
                    I_256TH.char, R_256TH.char -> 'z'
                    I_512TH.char, R_512TH.char -> 'f'
                    I_1024TH.char, R_1024TH.char -> 'm'

                    else -> '?'
                }
            }
            fun setEmphasis(display: String, emphasized: Boolean): String {
                val map = if(!emphasized) noteMap else noteMap.entries.associate { (key, value) -> value to key }
                return display.map { char ->
                    if (char in map.keys) {
                        map[char]!!
                    } else {
                        char
                    }
                }.joinToString("")
            }
        }
    }
}