package dev.cognitivity.chronal.rhythm.metronome

data class Beat(
    val duration: Double,
    val isHigh: Boolean,
    val measure: Int,
    val index: Int
)