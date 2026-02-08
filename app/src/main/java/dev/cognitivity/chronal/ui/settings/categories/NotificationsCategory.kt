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

package dev.cognitivity.chronal.ui.settings.categories

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.settings.Settings
import dev.cognitivity.chronal.ui.settings.categories.NotificationsCategory.NotificationPermissionContent
import dev.cognitivity.chronal.ui.settings.items.SettingItem
import dev.cognitivity.chronal.ui.settings.items.SettingMeta
import dev.cognitivity.chronal.ui.settings.data.SettingsCategory

private var showNotificationWarning by mutableStateOf(true)
private var notificationsEnabled by mutableStateOf(NotificationManagerCompat.from(ChronalApp.getInstance()).areNotificationsEnabled())

object NotificationsCategory : SettingsCategory(
    id = "notifications",
    title = R.string.page_settings_notifications,
    icon = R.drawable.baseline_notifications_24,
    iconColor = { MaterialTheme.colorScheme.onPrimaryContainer },
    iconContainer = { MaterialTheme.colorScheme.primaryContainer },
    items = listOf(
        SettingItem.Element {
            NotificationPermissionContent()
        },
        SettingItem.Switch(
            meta = SettingMeta(R.string.setting_name_metronome_controls_notification),
            setting = Settings.METRONOME_CONTROLS_NOTIFICATION
        ),
    )
) {
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun NotificationPermissionContent() {
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permission ->
            notificationsEnabled = permission
            if (!permission) {
                openNotificationSettings()
            }
        }

        fun request() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                openNotificationSettings()
            }
        }

        if (!notificationsEnabled && showNotificationWarning) {
            Column(
                modifier = Modifier.padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable {
                        request()
                    }
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        context.getString(R.string.settings_notifications_no_permission),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        modifier = Modifier.weight(1f)
                    )
                    FilledTonalIconButton(
                        onClick = { showNotificationWarning = false },
                        modifier = Modifier.minimumInteractiveComponentSize()
                            .size(IconButtonDefaults.extraSmallContainerSize(IconButtonDefaults.IconButtonWidthOption.Uniform))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = context.getString(R.string.generic_close),
                            modifier = Modifier.size(IconButtonDefaults.extraSmallIconSize),
                        )
                    }
                }
                Text(context.getString(R.string.settings_notifications_no_permission_description),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                Button(
                    onClick = { request() },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(context.getString(R.string.settings_notifications_no_permission_description))
                }
            }
        }
    }

    private fun openNotificationSettings() {
        val intent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
            } else {
                Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
            }

        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}