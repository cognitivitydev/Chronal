package dev.cognitivity.chronal.rhythm.player.elements

class SetTempo(
    startTime: Long,
    endTime: Long,
    beats: Int? = null,
    val tempo: Int,
) : PlayerRhythmElement(startTime, endTime, beats) {

    constructor(startTime: Long, tempo: Int, beats: Int, maxEnd: Long = Long.MAX_VALUE) : this(
        startTime,
        endTime = minOf((startTime + (beats * (60.0 / tempo * 1000)).toLong()), maxEnd),
        beats = minOf(beats, ((maxEnd - startTime) / (60.0 / tempo * 1000)).toInt()),
        tempo,
    )

    override fun writePeriod(sound: FloatArray): FloatArray {
        val sampleRate = 48000
        val durationMs = endTime - startTime
        val totalSamples = (durationMs * sampleRate / 1000).toInt()
        val output = FloatArray(totalSamples)

        val beatIntervalMs = 60_000.0 / tempo
        val beatIntervalSamples = (beatIntervalMs * sampleRate / 1000).toInt()

        var pos = 0
        while (pos < totalSamples) {
            val copyLen = minOf(sound.size, totalSamples - pos)
            System.arraycopy(sound, 0, output, pos, copyLen)
            pos += beatIntervalSamples
        }

        return output
    }
}