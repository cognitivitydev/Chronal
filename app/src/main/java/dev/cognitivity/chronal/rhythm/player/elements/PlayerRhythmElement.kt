package dev.cognitivity.chronal.rhythm.player.elements

sealed class PlayerRhythmElement(val startTime: Long, val endTime: Long, val beats: Int? = null) {
    abstract fun writePeriod(sound: FloatArray): FloatArray
}