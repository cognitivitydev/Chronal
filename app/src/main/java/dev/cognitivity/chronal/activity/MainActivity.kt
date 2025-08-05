package dev.cognitivity.chronal.activity

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Path
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.VibratorManager
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.cognitivity.chronal.ChronalApp
import dev.cognitivity.chronal.R
import dev.cognitivity.chronal.ui.metronome.windows.MetronomePageMain
import dev.cognitivity.chronal.ui.metronome.windows.activity
import dev.cognitivity.chronal.ui.settings.windows.SettingsPageMain
import dev.cognitivity.chronal.ui.theme.MetronomeTheme
import dev.cognitivity.chronal.ui.tuner.windows.TunerPageMain
import dev.cognitivity.chronal.widgets.ClockWidget
import dev.cognitivity.chronal.widgets.PresetListWidget
import dev.cognitivity.chronal.widgets.TunerWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

lateinit var audioManager: AudioManager
var vibratorManager: VibratorManager? = null

class MainActivity : ComponentActivity() {
    fun runActivity(activity: Class<*>) {
        val k = Intent(this, activity)
        startActivity(k)
    }

    var microphoneEnabled by mutableStateOf(false)
    var showMicrophoneDialog by mutableStateOf(false)
    val microphonePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { permission ->
        microphoneEnabled = permission
        showMicrophoneDialog = !permission
    }
    val fileActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == RESULT_OK) {
            activity.startActivity(
                Intent(this, AudioPlayerActivity::class.java).apply {
                    putExtra("file", it.data?.data.toString())
                }
            )
        }
    }


    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        vibratorManager = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager else null
        microphoneEnabled = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        val splashScreen = installSplashScreen()
        var keepSplashScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }
        lifecycleScope.launch {
            val app = ChronalApp.getInstance()
            while(!app.isInitialized()) {
                delay(50)
            }

            setContent {
                MetronomeTheme {
                    MainContent()
                }
            }

            keepSplashScreen = false
        }

    }

    override fun onPause() {
        super.onPause()
        CoroutineScope(Dispatchers.IO).launch {
            ClockWidget().updateAll(this@MainActivity)
            PresetListWidget().updateAll(this@MainActivity)
            TunerWidget().updateAll(this@MainActivity)
        }
        if(ChronalApp.getInstance().isInitialized()) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(1)
        }
    }

    override fun onResume() {
        super.onResume()
        microphoneEnabled = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
    @Composable
    fun MainContent() {
        val navController = rememberNavController()
        val sizeClass = calculateWindowSizeClass(this)
        var expanded = false

        when(sizeClass.widthSizeClass) {
            WindowWidthSizeClass.Expanded -> {
                expanded = true
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            bottomBar = {
                if(!expanded) {
                    NavigationBar(navController)
                }
            },
        ) { innerPadding ->
            if(expanded) {
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    NavigationRail(navController)
                    NavigationHost(navController, true, innerPadding)
                }
            } else NavigationHost(navController, false, innerPadding)

            if(showMicrophoneDialog) {
                AlertDialog(
                    onDismissRequest = { showMicrophoneDialog = false },
                    confirmButton = @Composable {
                        TextButton(onClick = {
                            showMicrophoneDialog = false
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = ("package:$packageName").toUri()
                            startActivity(intent)
                        }) {
                            Text(getString(R.string.generic_settings))
                        }
                    },
                    dismissButton = @Composable {
                        TextButton(onClick = {
                            showMicrophoneDialog = false
                        }) {
                            Text(getString(R.string.generic_cancel))
                        }
                    },
                    icon = @Composable {
                        Icon(
                            painter = painterResource(R.drawable.outline_warning_24),
                            contentDescription = getString(R.string.generic_warning),
                        )
                    },
                    title = @Composable {
                        Text(getString(R.string.tuner_missing_permission_title))
                    },
                    text = @Composable {
                        Text(getString(R.string.tuner_missing_permission_text))
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun NavigationHost(navController: NavHostController, expanded: Boolean, padding: PaddingValues) {
        val path = Path()
        path.moveTo(0f, 0f)
        path.cubicTo(0.05f, 0f, 0.133333f, 0.06f, 0.166666f, 0.4f)
        path.cubicTo(0.208333f, 0.82f, 0.25f, 1f, 1f, 1f)

        val enterTransition: (Boolean) -> EnterTransition = { forward ->
            if (expanded) {
                slideInVertically(MotionScheme.expressive().slowSpatialSpec(), initialOffsetY = { if(forward) it else -it })
            } else {
                slideInHorizontally(MotionScheme.expressive().slowSpatialSpec(), initialOffsetX = { if(forward) it else -it })
            }
        }

        val exitTransition: (Boolean) -> ExitTransition = { forward ->
            if(expanded) {
                slideOutVertically(MotionScheme.expressive().slowSpatialSpec(), targetOffsetY = { if(forward) -it else it })
            } else {
                slideOutHorizontally(MotionScheme.expressive().slowSpatialSpec(), targetOffsetX = { if(forward) -it else it })
            }
        }
        val startDestination = intent.getStringExtra("destination") ?: "metronome"
        intent.removeExtra("destination")

        NavHost(
            modifier = Modifier.fillMaxSize(),
            navController = navController,
            startDestination = startDestination,
        ) {
            composable("metronome",
                enterTransition = {
                    enterTransition(false)
                },
                exitTransition = {
                    exitTransition(true)
                }
            ) {
                MetronomePageMain(window, expanded, this@MainActivity, padding)
                ChronalApp.getInstance().tuner?.stop()
            }
            composable("tuner",
                enterTransition = {
                    val from = initialState.destination.route
                    enterTransition(from == "metronome")
                },
                exitTransition = {
                    val to = targetState.destination.route
                    exitTransition(to == "settings")
                }
            ) {
                if(ChronalApp.getInstance().isInitialized() && !ChronalApp.getInstance().metronome.playing) {
                    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(1)
                }
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                TunerPageMain(expanded, padding, this@MainActivity)
            }
            composable("settings",
                enterTransition = {
                    enterTransition(true)
                },
                exitTransition = {
                    exitTransition(false)
                }
            ) {
                if(!ChronalApp.getInstance().metronome.playing) {
                    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(1)
                }
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                SettingsPageMain(expanded, padding)
                ChronalApp.getInstance().tuner?.stop()
            }
        }
    }

    private val items = listOf(
        NavigationItem(R.string.page_metronome, "metronome", NavigationIcon.ResourceIcon(R.drawable.baseline_music_note_24)),
        NavigationItem(R.string.page_tuner, "tuner", NavigationIcon.ResourceIcon(R.drawable.baseline_graphic_eq_24)),
        NavigationItem(R.string.page_settings, "settings", NavigationIcon.VectorIcon(Icons.Outlined.Settings),
            NavigationIcon.VectorIcon(Icons.Filled.Settings)
        )
    )

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun NavigationBar(navController: NavHostController) {
        val showDialog by remember { mutableStateOf(false) }
        if (showDialog) {
            requestPermission()
        }
        ShortNavigationBar(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            items.forEach { item ->
                ShortNavigationBarItem(
                    selected = currentRoute == item.route,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        when (val icon = item.icon) {
                            is NavigationIcon.ResourceIcon -> {
                                Icon(
                                    painter = painterResource(
                                        if(currentRoute == item.route)
                                                (item.selectedIcon as NavigationIcon.ResourceIcon).resourceId
                                        else icon.resourceId
                                    ),
                                    contentDescription = "${getString(item.label)} icon"
                                )
                            }
                            is NavigationIcon.VectorIcon -> {
                                Icon(
                                    imageVector = (
                                        if(currentRoute == item.route)
                                            (item.selectedIcon as NavigationIcon.VectorIcon).imageVector
                                        else icon.imageVector
                                    ),
                                    contentDescription = "${getString(item.label)} icon"
                                )
                            }
                        }
                    },
                    label = { Text(getString(item.label), style = MaterialTheme.typography.bodyMedium) }
                )
            }
        }
    }

    @Composable
    fun NavigationRail(navController: NavHostController) {
        val showDialog by remember { mutableStateOf(false) }
        if (showDialog) {
            requestPermission()
        }
        NavigationRail(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            Spacer(modifier = Modifier.weight(1f))
            items.forEach { item ->
                NavigationRailItem(
                    selected = currentRoute == item.route,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        when (val icon = item.icon) {
                            is NavigationIcon.ResourceIcon -> {
                                Icon(
                                    painter = painterResource(
                                        if(currentRoute == item.route)
                                            (item.selectedIcon as NavigationIcon.ResourceIcon).resourceId
                                        else icon.resourceId
                                    ),
                                    contentDescription = "${item.label} icon"
                                )
                            }
                            is NavigationIcon.VectorIcon -> {
                                Icon(
                                    imageVector = (
                                            if(currentRoute == item.route)
                                                (item.selectedIcon as NavigationIcon.VectorIcon).imageVector
                                            else icon.imageVector
                                            ),
                                    contentDescription = "${item.label} icon"
                                )
                            }
                        }
                    },
                    label = { Text(getString(item.label), style = MaterialTheme.typography.bodyMedium) }
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }

    private fun requestPermission() {
        Toast.makeText(this, ActivityCompat.shouldShowRequestPermissionRationale(
            this@MainActivity, Manifest.permission.RECORD_AUDIO).toString(), Toast.LENGTH_SHORT).show()
        ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.RECORD_AUDIO), 0)
    }
}

data class NavigationItem(
    val label: Int,
    val route: String,
    val icon: NavigationIcon,
    val selectedIcon: NavigationIcon = icon
)

sealed class NavigationIcon {
    data class ResourceIcon(val resourceId: Int) : NavigationIcon()
    data class VectorIcon(val imageVector: ImageVector) : NavigationIcon()
}
