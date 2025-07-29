package dev.cognitivity.chronal.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.ui.theme.MetronomeTheme

class HelpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MetronomeTheme {
                MainContent()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun MainContent() {
        var page by remember { mutableIntStateOf(0) }

        val enterTransition: (Boolean) -> EnterTransition = { forward ->
            slideInHorizontally(MotionScheme.expressive().slowSpatialSpec(), initialOffsetX = { if(forward) it else -it })
        }

        val exitTransition: (Boolean) -> ExitTransition = { forward ->
            slideOutHorizontally(MotionScheme.expressive().slowSpatialSpec(), targetOffsetX = { if(forward) -it else it })
        }

        val navHostController = rememberNavController()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(getString(R.string.help_title))
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { finish() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = getString(R.string.generic_back)
                            )
                        }
                    }
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(innerPadding)
            ) {
                PrimaryTabRow(
                    selectedTabIndex = page,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Tab(
                        text = { Text(getString(R.string.page_metronome)) },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.baseline_music_note_24),
                                contentDescription = getString(R.string.page_metronome),
                            )
                        },
                        selected = page == 0,
                        onClick = {
                            page = 0
                            navHostController.navigate("metronome")
                        }
                    )
                    Tab(
                        text = { Text(getString(R.string.page_tuner)) },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.baseline_graphic_eq_24),
                                contentDescription = getString(R.string.page_tuner),
                            )
                        },
                        selected = page == 1,
                        onClick = {
                            page = 1
                            navHostController.navigate("tuner")
                        }
                    )
                }

                NavHost(
                    modifier = Modifier.fillMaxSize(),
                    navController = navHostController,
                    startDestination = "metronome",
                ) {
                    composable("metronome",
                        enterTransition = {
                            enterTransition(false)
                        },
                        exitTransition = {
                            exitTransition(true)
                        }
                    ) {
                        MetronomeHelp()
                    }
                    composable("tuner",
                        enterTransition = {
                            enterTransition(true)
                        },
                        exitTransition = {
                            exitTransition(false)
                        }
                    ) {
                        TunerHelp()
                    }
                }
            }
        }
    }

    @Composable
    fun Question(question: Int, answer: Int) {
        var expanded by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier.fillMaxWidth()
                .clickable {
                    expanded = !expanded
                }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getString(question),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if(expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = getString(if(expanded) R.string.generic_menu_collapse else R.string.generic_menu_expand),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if(expanded) {
                Text(
                    text = getString(answer),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    @Composable
    fun MetronomeHelp() {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Question(
                    question = R.string.help_metronome_edit_rhythm_title,
                    answer = R.string.help_metronome_edit_rhythm
                )
            }
            item {
                Question(
                    question = R.string.help_metronome_edit_rhythm_simple_title,
                    answer = R.string.help_metronome_edit_rhythm_simple
                )
            }
            item {
                Question(
                    question = R.string.help_metronome_edit_rhythm_advanced_title,
                    answer = R.string.help_metronome_edit_rhythm_advanced
                )
            }
        }
    }

    @Composable
    fun TunerHelp() {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Question(
                    question = R.string.help_tuner_detection_title,
                    answer = R.string.help_tuner_detection
                )
            }
            item {
                Question(
                    question = R.string.help_tuner_transpose_title,
                    answer = R.string.help_tuner_transpose
                )
            }
        }
    }
}