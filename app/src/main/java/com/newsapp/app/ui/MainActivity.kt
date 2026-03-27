package com.newsapp.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.newsapp.app.SportNewsApp
import com.newsapp.app.ui.navigation.NavGraph
import com.newsapp.app.ui.navigation.Screen
import com.newsapp.app.ui.screens.AdvancedWebViewScreen
import com.newsapp.app.ui.theme.SportNewsTheme
import com.newsapp.app.ui.viewmodel.MainViewModel
import com.newsapp.app.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val appState by mainViewModel.appState.collectAsState()
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()

            SportNewsTheme(darkTheme = isDarkTheme) {
                when (val state = appState) {
                    is MainViewModel.AppState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is MainViewModel.AppState.WebView -> {
                        AdvancedWebViewScreen(
                            initialUrl = state.url,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    is MainViewModel.AppState.NormalApp -> {
                        MainScreen()
                    }
                }
            }
        }
    }
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val qrScannerLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        SportNewsApp.amplitude.track("QR Scan Completed", mapOf("has_result" to (result.contents != null).toString()))
    }

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem(Screen.Scores, "Scores", Icons.Filled.Sports, Icons.Outlined.Sports),
        BottomNavItem(Screen.Videos, "Videos", Icons.Filled.PlayCircle, Icons.Outlined.PlayCircle),
        BottomNavItem(Screen.Favorites, "Saved", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
        BottomNavItem(Screen.Settings, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
    )

    val showBottomBar = currentDestination?.hierarchy?.any { dest ->
        bottomNavItems.any { it.screen.route == dest.route }
    } == true

    Scaffold(
        floatingActionButton = {
            if (showBottomBar) {
                FloatingActionButton(
                    onClick = {
                        SportNewsApp.amplitude.track("QR Scanner Opened")
                        val options = ScanOptions().apply {
                            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                            setPrompt("Scan QR code")
                            setCameraId(0)
                            setBeepEnabled(false)
                            setOrientationLocked(true)
                        }
                        qrScannerLauncher.launch(options)
                    },
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "QR Scanner",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                SportNewsApp.amplitude.track("Tab Selected", mapOf("tab" to item.label))
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavGraph(navController = navController)
        }
    }
}
