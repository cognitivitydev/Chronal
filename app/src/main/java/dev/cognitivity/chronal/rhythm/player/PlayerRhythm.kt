package dev.cognitivity.chronal.rhythm.player

import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.rhythm.player.elements.PlayerRhythmElement
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

data class PlayerRhythm(
    val elements: List<PlayerRhythmElement>,
) {
    fun toWav(file: File) {
        val res = R.raw.sine_hi
        val sound = readWavStream(ChronalApp.getInstance().resources.openRawResource(res))

        RandomAccessFile(file, "rw").use { wavFile ->
            wavFile.setLength(0)

            val emptyHeader = ByteArray(44)
            wavFile.write(emptyHeader)

            var totalSampleCount = 0L

            BufferedOutputStream(FileOutputStream(wavFile.fd)).use { outputStream ->
                for (element in elements) {
                    val period = element.writePeriod(sound)
                    for (sample in period) {
                        val clamped = (sample.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
                        outputStream.write(
                            byteArrayOf(
                                (clamped.toInt() and 0xFF).toByte(),
                                ((clamped.toInt() shr 8) and 0xFF).toByte()
                            )
                        )
                        totalSampleCount++
                    }
                }
            }

            val dataSize = totalSampleCount * 2

            wavFile.seek(0)
            val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
                put("RIFF".toByteArray())
                putInt(36 + dataSize.toInt())
                put("WAVE".toByteArray())
                put("fmt ".toByteArray())
                putInt(16) // chunk size
                putShort(1) // audio format
                putShort(1) // channels
                putInt(48000) // sample rate
                putInt(48000 * 2) // byte rate
                putShort(2) // block align
                putShort(16) // bits per sample
                put("data".toByteArray())
                putInt(dataSize.toInt())
            }.array()
            wavFile.write(header)
        }
    }

    private fun readWavStream(inputStream: InputStream): FloatArray {
        val content = readStreamToBytes(inputStream)
        val dataIndex = getDataIndex(content)
        if (dataIndex < 0) {
            throw RuntimeException("No data header found")
        }

        if (dataIndex + 8 > content.size) {
            throw RuntimeException("WAV file truncated")
        }

        val dataSizeBytes = ByteBuffer.wrap(content, dataIndex + 4, 4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .int

        val dataStart = dataIndex + 8

        if (dataStart + dataSizeBytes > content.size) {
            throw RuntimeException("Data chunk size exceeds file size")
        }

        val byteBuffer = ByteBuffer.wrap(content, dataStart, dataSizeBytes)
            .order(ByteOrder.LITTLE_ENDIAN)
        val floatBuffer = byteBuffer.asFloatBuffer()

        val data = FloatArray(floatBuffer.remaining())
        floatBuffer[data]
        return data
    }

    @Throws(IOException::class)
    private fun readStreamToBytes(input: InputStream): ByteArray {
        val buffer = ByteArrayOutputStream()
        val data = ByteArray(4096)
        var read: Int

        while ((input.read(data, 0, data.size).also { read = it }) != -1) {
            buffer.write(data, 0, read)
        }
        return buffer.toByteArray()
    }

    private fun getDataIndex(array: ByteArray): Int {
        val data = "data".toByteArray(StandardCharsets.US_ASCII)
        outer@ for (i in 0..<array.size - data.size + 1) {
            for (j in data.indices) {
                if (array[i + j] != data[j]) {
                    continue@outer
                }
            }
            return i
        }
        return -1
    }
}