package dev.cognitivity.chronal.ui.settings.windows

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.ColorScheme
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.Setting
import dev.cognitivity.chronal.activity.CreditsActivity
import dev.cognitivity.chronal.activity.HelpActivity
import dev.cognitivity.chronal.activity.InstrumentActivity
import dev.cognitivity.chronal.activity.LatencyActivity
import dev.cognitivity.chronal.activity.MainActivity
import dev.cognitivity.chronal.activity.TempoMarkingsActivity
import dev.cognitivity.chronal.ui.settings.ExpandableButtonRow
import dev.cognitivity.chronal.ui.settings.ExpandableOption
import dev.cognitivity.chronal.ui.settings.ExpandableSlider
import dev.cognitivity.chronal.ui.theme.MetronomeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.floor

private var showFeedback by mutableStateOf(false)
private var showDeveloperOptions by mutableStateOf(ChronalApp.getInstance().settings.showDeveloperOptions.value)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPageMain(expanded: Boolean, padding: PaddingValues) {
    val scope = rememberCoroutineScope()
    val categories = LinkedHashMap<String, ArrayList<Setting<*>>>().apply {
        ChronalApp.getInstance().settings.keyMap.forEach { entry ->
            val category = context.getString(entry.key.category)
            val setting = entry.value
            if (!containsKey(category)) {
                if(category == context.getString(R.string.setting_category_internal)) {
                    if(showDeveloperOptions) put(category, arrayListOf())
                } else {
                    put(category, arrayListOf())
                }
            }
            get(category)?.add(setting)
        }
    }

    if(expanded) {
        SettingsPageExpanded(categories, scope, LocalContext.current)
    } else {
        SettingsPageCompact(categories, scope, LocalContext.current, padding)
    }

    if(showFeedback) {
        ModalBottomSheet(
            onDismissRequest = {
                showFeedback = false
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            Text(context.getString(R.string.settings_feedback_send),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, "https://github.com/cognitivitydev/Chronal/issues".toUri())
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        ChronalApp.getInstance().startActivity(intent)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_bug_report_24),
                    contentDescription = context.getString(R.string.settings_feedback_open_issue_text),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = context.getString(R.string.settings_feedback_open_issue_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = context.getString(R.string.settings_feedback_open_issue_text),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, "https://crowdin.com/project/chronal".toUri())
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        ChronalApp.getInstance().startActivity(intent)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_public_24),
                    contentDescription = context.getString(R.string.settings_feedback_translate_title),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = context.getString(R.string.settings_feedback_translate_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = context.getString(R.string.settings_feedback_translate_text),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=dev.cognitivity.chronal".toUri())
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        ChronalApp.getInstance().startActivity(intent)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_rate_review_24),
                    contentDescription = context.getString(R.string.settings_feedback_review_title),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = context.getString(R.string.settings_feedback_review_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = context.getString(R.string.settings_feedback_review_text),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, "mailto:cognitivitydev@gmail.com".toUri())
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        ChronalApp.getInstance().startActivity(intent)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.MailOutline,
                    contentDescription = context.getString(R.string.settings_feedback_email_title),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = context.getString(R.string.settings_feedback_email_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = context.getString(R.string.settings_feedback_email_text),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun BoxScope.MoreSettingsDropdown() {
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
    ) {
        DropdownMenuItem(onClick = {
            val intent = Intent(Intent.ACTION_VIEW, "https://github.com/cognitivitydev/Chronal".toUri())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ChronalApp.getInstance().startActivity(intent)
        },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.baseline_code_24),
                    contentDescription = context.getString(R.string.settings_menu_view_source),
                )
            },
            text = { Text(context.getString(R.string.settings_menu_view_source)) },
        )
        DropdownMenuItem(onClick = {
            ChronalApp.getInstance().startActivity(
                Intent(context, CreditsActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.outline_public_24),
                    contentDescription = context.getString(R.string.settings_menu_open_source_credits),
                )
            },
            text = { Text(context.getString(R.string.settings_menu_open_source_credits)) },
        )
        HorizontalDivider()
        DropdownMenuItem(onClick = {
            showFeedback = true
        },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.outline_feedback_24),
                    contentDescription = context.getString(R.string.settings_menu_send_feedback),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            text = { Text(context.getString(R.string.settings_menu_send_feedback)) }
        )
        DropdownMenuItem(onClick = {
            ChronalApp.getInstance().startActivity(
                Intent(context, HelpActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.baseline_help_outline_24),
                    contentDescription = context.getString(R.string.settings_menu_help),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            text = { Text(context.getString(R.string.settings_menu_help)) }
        )
        if(ChronalApp.getInstance().developmentBuild && !showDeveloperOptions) {
            HorizontalDivider()
            DropdownMenuItem(onClick = {
                showDeveloperOptions = true
                ChronalApp.getInstance().settings.showDeveloperOptions.value = true
                scope.launch {
                    ChronalApp.getInstance().settings.save()
                }
            },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.baseline_code_24),
                        contentDescription = context.getString(R.string.settings_menu_reveal_developer),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = { Text(context.getString(R.string.settings_menu_reveal_developer)) }
            )
        }
    }

    IconButton(
        onClick = {
            expanded = !expanded
        },
        modifier = Modifier.align(Alignment.Center)
    ) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = context.getString(R.string.generic_more_options),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
@Suppress("UNCHECKED_CAST")
fun DrawSetting(
    setting: Setting<*>,
    scope: CoroutineScope,
    context: Context
) {
    if(setting.menu != null) {
        when (setting.menu.type) {
            "Launch" -> {
                MenuOption(
                    setting = setting,
                    onClick = {
                        when(setting.menu.id) {
                            "Instrument" -> {
                                context.startActivity(
                                    Intent(context, InstrumentActivity::class.java)
                                )
                            }
                            "Markings" -> {
                                context.startActivity(
                                    Intent(context, TempoMarkingsActivity::class.java)
                                )
                            }
                            else -> {
                                Log.w("SettingsUI", "Unknown menu type: ${setting.menu.id}")
                            }
                        }
                    }
                )
            }
            "Expandable" -> {
                ExpandableOption(
                    setting = setting,
                ) {
                    when (setting.menu.id) {
                        "Colors" -> {
                            ColorSetting(setting as Setting<ColorScheme>)
                        }

                        "Accidentals" -> {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                ExpandableButtonRow(
                                    setting = setting,
                                    labels = listOf(
                                        R.string.setting_accidental_sharps,
                                        R.string.setting_accidental_flats,
                                        R.string.setting_accidentals_sharps_flats
                                    ).map { context.getString(it) },
                                ) { index ->
                                    scope.launch {
                                        (setting as Setting<Int>).value = index
                                        ChronalApp.getInstance().settings.save()
                                    }
                                    index
                                }
                            }
                        }

                        "Note" -> {
                            NoteSetting(setting as Setting<Int>)
                        }

                        "Percentage" -> {
                            ExpandableSlider(
                                setting, 0f..1f, "0%", "50%",
                                onValueChange = { value ->
                                    scope.launch {
                                        (setting as Setting<Float>).value = value
                                        ChronalApp.getInstance().settings.save()
                                    }
                                },
                                valueText = { value ->
                                    "${floor(value * 100).toInt()}%"
                                }
                            )
                        }

                        "Latency" -> {
                            ExpandableSlider(
                                setting, 0f..500f, "0 ms", "50-150 ms",
                                onValueChange = { value ->
                                    scope.launch {
                                        (setting as Setting<Int>).value = value.toInt()
                                        ChronalApp.getInstance().settings.save()
                                    }
                                },
                                valueText = { value ->
                                    "${value.toInt()}"
                                }
                            ) {
                                Button(
                                    modifier = Modifier.padding(8.dp)
                                        .align(Alignment.CenterHorizontally),
                                    onClick = {
                                        context.startActivity(
                                            Intent(context, LatencyActivity::class.java)
                                        )
                                    }
                                ) {
                                    Text(context.getString(R.string.setting_action_test_latency))
                                }
                            }
                        }

                        "Frequency" -> {
                            ExpandableSlider(
                                setting, 415f..466f, context.getString(R.string.tuner_hz, 415),
                                context.getString(R.string.tuner_hz, 440),
                                onValueChange = { value ->
                                    scope.launch {
                                        (setting as Setting<Int>).value = value.toInt()
                                        ChronalApp.getInstance().settings.save()
                                    }
                                },
                                valueText = { value ->
                                    context.getString(R.string.tuner_hz, value.toInt())
                                }
                            )
                        }

                        else -> {
                            throw Error("Invalid menu type")
                        }
                    }
                }
            }
        }
        return
    }
    when (setting.default) {
        is Boolean -> {
            var isChecked by remember {
                mutableStateOf(setting.value as Boolean)
            }

            SwitchOption(
                setting = setting,
                isChecked = isChecked,
                onCheckedChange = { newValue ->
                    isChecked = newValue
                    scope.launch {
                        (setting as Setting<Boolean>).value = newValue
                        ChronalApp.getInstance().settings.save()
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ColorSetting(setting: Setting<ColorScheme>) {
    var selection by remember { mutableStateOf(setting.value) }
    var initialValue by remember { mutableStateOf(setting.value) }
    val scope = rememberCoroutineScope()

    val surfaceContainerLow = MaterialTheme.colorScheme.surfaceContainerLow
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(context.getString(R.string.setting_color_scheme_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(ColorScheme.Color.entries) { color ->
                val selected = selection.color == color
                MetronomeTheme(color) {
                    Box(
                        modifier = Modifier.size(80.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable {
                                selection = selection.copy(
                                    color = color,
                                    contrast = if(color == ColorScheme.Color.SYSTEM) ColorScheme.Contrast.SYSTEM
                                    else if(selection.contrast == ColorScheme.Contrast.SYSTEM) ColorScheme.Contrast.LOW
                                    else selection.contrast
                                )
                            }
                            .border(2.dp, if(selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(24.dp))
                            .border(5.dp, if(selected) surfaceContainerLow
                                else MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(24.dp))
                    ) {
                        Box(
                            modifier = Modifier.size(56.dp)
                                .align(Alignment.Center)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        Text(context.getString(R.string.setting_color_theme_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
        ) {
            repeat(3) { i ->
                val theme = ColorScheme.Theme.entries[i]
                ToggleButton(
                    checked = selection.theme == theme,
                    onCheckedChange = {
                        selection = selection.copy(theme = theme)
                    },
                    shapes = when (i) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        2 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                    colors = ToggleButtonDefaults.toggleButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    contentPadding = ButtonDefaults.ContentPadding
                ) {
                    if (selection.theme == theme) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = context.getString(R.string.generic_selected),
                        )
                        Spacer(modifier = Modifier.width(ToggleButtonDefaults.IconSpacing))
                    }
                    Text(
                        text = when(theme) {
                            ColorScheme.Theme.SYSTEM -> context.getString(R.string.setting_color_theme_system)
                            ColorScheme.Theme.LIGHT -> context.getString(R.string.setting_color_theme_light)
                            ColorScheme.Theme.DARK -> context.getString(R.string.setting_color_theme_dark)
                        }
                    )
                }
            }
        }

        Text(context.getString(R.string.setting_color_contrast_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
        ) {
            repeat(4) { i ->
                val contrast = ColorScheme.Contrast.entries[i]
                ToggleButton(
                    checked = selection.contrast == contrast,
                    onCheckedChange = {
                        selection = selection.copy(contrast = contrast)
                    },
                    shapes = when (i) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        3 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                    colors = ToggleButtonDefaults.toggleButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    contentPadding = ButtonDefaults.ContentPadding,
                    enabled = if(selection.color == ColorScheme.Color.SYSTEM) false else i != 0
                ) {
                    if (selection.contrast == contrast) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = context.getString(R.string.generic_selected),
                        )
                        Spacer(modifier = Modifier.width(ToggleButtonDefaults.IconSpacing))
                    }
                    Text(
                        text = when(contrast) {
                            ColorScheme.Contrast.SYSTEM -> context.getString(R.string.setting_color_contrast_system)
                            ColorScheme.Contrast.LOW -> context.getString(R.string.setting_color_contrast_low)
                            ColorScheme.Contrast.MEDIUM -> context.getString(R.string.setting_color_contrast_medium)
                            ColorScheme.Contrast.HIGH -> context.getString(R.string.setting_color_contrast_high)
                        }
                    )
                }
            }
        }
        if(selection.color == ColorScheme.Color.SYSTEM) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = context.getString(R.string.generic_info),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = context.getString(R.string.setting_color_contrast_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }

        if(selection != initialValue) {
            FilledTonalButton(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                onClick = {
                    scope.launch {
                        setting.value = selection
                        ChronalApp.getInstance().settings.save()
                        context.startActivity(
                            Intent(context, MainActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .putExtra("destination", "settings")
                        )
                    }
                },
            ) {
                Text(context.getString(R.string.setting_color_save_reload))
            }
        }
    }
}

@Composable
fun NoteSetting(setting: Setting<Int>) {
    var selection by remember { mutableIntStateOf(setting.value) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        repeat(6) { index ->
            val interactionSource = remember { MutableInteractionSource() }
            Row(
                modifier = Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) {
                    selection = index
                    scope.launch {
                        setting.value = index
                        ChronalApp.getInstance().settings.save()
                    }
                },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = selection == index,
                    onClick = {
                        selection = index
                        scope.launch {
                            setting.value = index
                            ChronalApp.getInstance().settings.save()
                        }
                    },
                    interactionSource = interactionSource
                )
                val text = when (index) {
                    0 -> R.string.setting_note_name_english to R.string.setting_note_example_english
                    1 -> R.string.setting_note_name_solfege_english to R.string.setting_note_example_solfege_english
                    2 -> R.string.setting_note_name_solfege_chromatic to R.string.setting_note_example_solfege_chromatic
                    3 -> R.string.setting_note_name_solfege_latin to R.string.setting_note_example_solfege_latin
                    4 -> R.string.setting_note_name_german to R.string.setting_note_example_german
                    5 -> R.string.setting_note_name_nashville to R.string.setting_note_example_nashville
                    else -> R.string.generic_unknown to R.string.generic_unknown
                }
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = context.getString(text.first),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (selection == index) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = context.getString(text.second),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selection == index) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth()
            .padding(20.dp, 8.dp)
    )
}

@Composable
fun Divider() {
    Box(
        modifier = Modifier.fillMaxWidth()
            .padding(12.dp, 4.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
fun SettingOption(
    name: String, hint: String, onClick: () -> Unit,
    interactionSource: MutableInteractionSource? = remember { MutableInteractionSource() },
    indication: Indication? = LocalIndication.current,
    button: @Composable () -> Unit) {
    Column(
        modifier = Modifier.clip(RoundedCornerShape(24.dp))
            .clickable(interactionSource = interactionSource, indication = indication) {
                onClick()
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterVertically)
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                if(hint.isNotBlank()) {
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Box(
                modifier = Modifier.align(Alignment.CenterVertically)
                    .padding(end = 2.dp)
            ) {
                button()
            }
        }
    }
}

@Composable
fun SwitchOption(
    setting: Setting<*>,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    SettingOption(
        name = context.getString(setting.key.settingName),
        hint = context.getString(setting.hint),
        onClick = { onCheckedChange(!isChecked) },
        interactionSource = interactionSource,
        indication = null
    ) {
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            interactionSource = interactionSource
        )
    }
}

@Composable
fun MenuOption(
    setting: Setting<*>,
    onClick: () -> Unit
) {
    SettingOption(
        name = context.getString(setting.key.settingName),
        hint = context.getString(setting.hint),
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(R.drawable.baseline_arrow_right_24),
            contentDescription = context.getString(R.string.setting_action_navigate_menu),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsFooter() {
    val context = LocalContext.current
    val versionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName

    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = context.getString(R.string.app_name),
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = context.getString(R.string.app_name),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = context.getString(R.string.settings_footer_version, versionName),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if(ChronalApp.getInstance().developmentBuild) {
                Text(
                    text = context.getString(R.string.settings_footer_development_build),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}