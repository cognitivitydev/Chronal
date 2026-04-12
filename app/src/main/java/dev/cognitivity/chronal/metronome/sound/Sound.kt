/*
 * Chronal: Metronome app for Android
 * Copyright (C) 2026  cognitivity
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.cognitivity.chronal.metronome.sound

import android.content.Context
import java.io.File
import java.io.InputStream

sealed class Sound(open val pitch: Int) {
    abstract val key: String
    abstract fun openStream(context: Context): InputStream?

    data class Resource(
        val resId: Int,
        override val pitch: Int,
    ) : Sound(pitch) {
        override val key: String = "res:$resId"

        override fun openStream(context: Context): InputStream? {
            if (resId == 0) return null
            return try {
                context.resources.openRawResource(resId)
            } catch (_: Exception) {
                null
            }
        }
    }

    data class File(
        val relativePath: String,
        override val pitch: Int,
    ) : Sound(pitch) {
        override val key: String = "file:$relativePath"

        override fun openStream(context: Context): InputStream? {
            val file = File(context.filesDir, relativePath)
            if (!file.isFile) return null
            return file.inputStream()
        }

        companion object {
            fun import(
                context: Context,
                relativePath: String,
                pitch: Int,
                inputStream: InputStream,
                overwrite: Boolean = true,
            ): File {
                val file = File(context.filesDir, relativePath)
                file.parentFile?.mkdirs()
                if(!file.exists() || overwrite) {
                    inputStream.use { source ->
                        file.outputStream().use { target ->
                            source.copyTo(target)
                        }
                    }
                }
                return File(relativePath, pitch)
            }
        }
    }
}