package dev.cognitivity.chronal.rhythm.player.elements

class Pause(
    startTime: Long,
    endTime: Long,
    beats: Int? = null
) : PlayerRhythmElement(startTime, endTime, beats) {
    constructor(startTime: Long, beats: Int, tempo: Int, maxEnd: Long = Long.MAX_VALUE) : this(
        startTime,
        endTime = minOf(startTime + (beats * (60.0 / tempo * 1000)).toLong(), maxEnd),
        beats
    )

    override fun writePeriod(sound: FloatArray): FloatArray {
        val sampleRate = 48000
        val duration = (endTime - startTime) / 1000.0
       return FloatArray((sampleRate * duration).toInt())
    }
}