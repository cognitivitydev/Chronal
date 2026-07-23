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

import android.util.Log
import dev.cognitivity.chronal.R

data class SoundPack(
    val id: String,
    val nameRes: Int,
    val type: SoundType,
    val assets: List<Sound>
) {
    fun getSound(pitch: Int): Sound? {
        return assets.find { it.pitch == pitch }
    }

    companion object {
        val BUILTIN_CLICK = SoundPack(
            id = "builtin/click",
            nameRes = R.string.sound_click,
            type = SoundType.ATONAL,
            assets = listOf(
                Sound.Resource(R.raw.click_hi, 0),
                Sound.Resource(R.raw.click_lo, 1),
            )
        )
        val BUILTIN_SINE = SoundPack(
            id = "builtin/sine",
            nameRes = R.string.sound_sine,
            type = SoundType.ATONAL,
            assets = listOf(
                Sound.Resource(R.raw.sine_hi, 0),
                Sound.Resource(R.raw.sine_lo, 1),
            )
        )
        val BUILTIN_SQUARE = SoundPack(
            id = "builtin/square",
            nameRes = R.string.sound_square,
            type = SoundType.ATONAL,
            assets = listOf(
                Sound.Resource(R.raw.square_hi, 0),
                Sound.Resource(R.raw.square_lo, 1),
            )
        )
        val BUILTIN_CLAP = SoundPack(
            id = "builtin/clap",
            nameRes = R.string.sound_clap,
            type = SoundType.ATONAL,
            assets = listOf(
                Sound.Resource(R.raw.clap_hi, 0),
                Sound.Resource(R.raw.clap_lo, 1),
            )
        )
        val BUILTIN_BLOCK = SoundPack(
            id = "builtin/block",
            nameRes = R.string.sound_block,
            type = SoundType.ATONAL,
            assets = listOf(
                Sound.Resource(R.raw.block_hi, 0),
                Sound.Resource(R.raw.block_lo, 1),
            )
        )
        val BUILTIN_BELL = SoundPack(
            id = "builtin/bell",
            nameRes = R.string.sound_bell,
            type = SoundType.ATONAL,
            assets = listOf(
                Sound.Resource(R.raw.bell_hi, 0),
                Sound.Resource(R.raw.bell_lo, 1),
            )
        )
        val BUILTIN_TAMBOURINE = SoundPack(
            id = "builtin/tambourine",
            nameRes = R.string.sound_tambourine,
            type = SoundType.ATONAL,
            assets = listOf(
                Sound.Resource(R.raw.tambourine_hi, 0),
                Sound.Resource(R.raw.tambourine_lo, 1),
            )
        )

        const val DEFAULT_ID = "builtin/click"

        private val BUILTIN_PACKS = listOf(
            BUILTIN_CLICK,
            BUILTIN_SINE,
            BUILTIN_SQUARE,
            BUILTIN_CLAP,
            BUILTIN_BELL,
            BUILTIN_TAMBOURINE,
            BUILTIN_BLOCK,
        )

        private val customPacks = mutableMapOf<String, SoundPack>()

        fun builtins(): List<SoundPack> = BUILTIN_PACKS

        fun register(pack: SoundPack) {
            if (!pack.id.startsWith("builtin/")) {
                customPacks[pack.id] = pack
            }
        }

        fun registerAll(packs: Iterable<SoundPack>) {
            packs.forEach { register(it) }
        }

        fun clearRegistered() {
            customPacks.clear()
        }

        fun all(): List<SoundPack> = BUILTIN_PACKS + customPacks.values

        fun byId(id: String?): SoundPack? {
            if (id == null) return null
            Log.d("SoundPack", "Looking for SoundPack with id: $id: ${all().map { it.id }}")
            return all().find { it.id == id }
        }

        fun default(): SoundPack = BUILTIN_CLICK
    }
}