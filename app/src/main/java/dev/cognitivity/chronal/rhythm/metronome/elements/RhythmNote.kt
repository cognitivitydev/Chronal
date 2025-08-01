package dev.cognitivity.chronal.rhythm.metronome.elements

data class RhythmNote(
    val display: String,
    val isRest: Boolean,
    val isInverted: Boolean,
    val duration: Double,
    val dots: Int
) : RhythmElement()