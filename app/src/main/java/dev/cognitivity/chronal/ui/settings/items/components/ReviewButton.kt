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

package dev.cognitivity.chronal.ui.settings.items.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ReviewButton(onDismiss: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .defaultMinSize(minHeight = 72.dp)
            .clickable {
                showReview()
                onDismiss()
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(stringResource(R.string.settings_feedback_review_title),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(stringResource(R.string.settings_feedback_review_text),
                    style = MaterialTheme.typography.bodyMediumEmphasized,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f)
                )
            }
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .width(1.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.75f), CircleShape)
            )
            IconButton(onClick = {
                delayReview()
                onDismiss()
            }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.generic_close),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.75f)
                )
            }
        }
    }
}

private fun showReview() {
    val uri = "https://play.google.com/store/apps/details?id=dev.cognitivity.chronal&reviewId=0".toUri()
    ChronalApp.getInstance().startActivity(
        Intent(Intent.ACTION_VIEW, uri)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
    disableReview()
}

private fun delayReview() {
    val displayCount = Settings.REVIEW_COUNT.get()
    when(displayCount) {
        // 2 weeks
        0 -> Settings.REVIEW_TIMESTAMP.set(System.currentTimeMillis() + 14 * 24 * 60 * 60 * 1000L)
        // 4 weeks
        1 -> Settings.REVIEW_TIMESTAMP.set(System.currentTimeMillis() + 28 * 24 * 60 * 60 * 1000L)
        // 8 weeks
        2 -> Settings.REVIEW_TIMESTAMP.set(System.currentTimeMillis() + 56 * 24 * 60 * 60 * 1000L)

        3 -> {
            disableReview()
            return
        }
    }
    CoroutineScope(Dispatchers.IO).launch {
        Settings.REVIEW_TIMESTAMP.save()
        Settings.REVIEW_COUNT.save(displayCount + 1)
    }
}

private fun disableReview() {
    CoroutineScope(Dispatchers.IO).launch {
        Settings.REVIEW_TIMESTAMP.save(-1)
        Settings.REVIEW_COUNT.save(-1)
    }
}