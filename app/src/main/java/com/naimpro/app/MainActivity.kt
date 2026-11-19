package com.naimpro.app

import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.naimpro.app.ui.AppViewModel
import com.naimpro.app.ui.screens.AboutScreen
import com.naimpro.app.ui.screens.CalendarScreen
import com.naimpro.app.ui.screens.ContentScreen
import com.naimpro.app.ui.screens.HomeScreen
import com.naimpro.app.ui.screens.LocationPickerScreen
import com.naimpro.app.ui.screens.MonthlyScreen
import com.naimpro.app.ui.screens.MoreScreen
import com.naimpro.app.ui.screens.QiblaScreen
import com.naimpro.app.ui.screens.SettingsScreen
import com.naimpro.app.ui.screens.TrackingScreen
import com.naimpro.app.ui.screens.ZikirScreen
import com.naimpro.app.ui.theme.NaimTheme
import com.naimpro.app.ui.util.applyLanguage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build(),
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedClosableObjects()
                    .detectLeakedSqlLiteObjects()
                    .penaltyLog()
                    .build(),
            )
        }

        val container = NaimApplication.container(this)
        splash.setKeepOnScreenCondition { false }

        setContent {
            val vm: AppViewModel = viewModel(factory = AppViewModel.Factory(container))
            val settings by vm.settings.collectAsStateWithLifecycle()

            LaunchedEffect(settings.language) {
                applyLanguage(this@MainActivity, settings.language.tag)
            }

            NaimTheme(settings = settings) {
                NaimApp(vm)
            }
        }
    }
}

private enum class Destination(
    val route: String,
    val icon: ImageVector,
    val labelRes: Int,
) {
    HOME("home", Icons.Default.Home, R.string.nav_home),
    QIBLA("qibla", Icons.Outlined.Explore, R.string.nav_qibla),
    MONTHLY("monthly", Icons.Outlined.CalendarMonth, R.string.nav_monthly),
    MORE("more", Icons.Default.MoreHoriz, R.string.nav_more),
}

@Composable
private fun NaimApp(vm: AppViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBar = Destination.entries.any { it.route == currentRoute }

    fun navigate(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    Destination.entries.forEach { destination ->
                        val selected =
                            backStackEntry?.destination?.hierarchy
                                ?.any { it.route == destination.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = { if (!selected) navigate(destination.route) },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = stringResource(destination.labelRes),
                                )
                            },
                            label = { Text(stringResource(destination.labelRes)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.HOME.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(Destination.HOME.route) {
                HomeScreen(
                    vm = vm,
                    onPickLocation = { navController.navigate("location") },
                    onOpenSettings = { navController.navigate("settings") },
                    onOpenQibla = { navController.navigate("qibla-screen") },
                    onOpenMonthly = { navController.navigate("monthly-screen") },
                    onOpenTracking = { navController.navigate("tracking") },
                    onOpenZikir = { navController.navigate("zikir") },
                    onOpenContent = { navController.navigate("content/0") },
                )
            }
            composable(Destination.QIBLA.route) {
                QiblaScreen(vm = vm, onBack = { navController.popBackStack() })
            }
            composable(Destination.MONTHLY.route) {
                MonthlyScreen(vm = vm, onBack = { navController.popBackStack() })
            }
            composable(Destination.MORE.route) {
                MoreScreen(
                    vm = vm,
                    onOpenTracking = { navController.navigate("tracking") },
                    onOpenZikir = { navController.navigate("zikir") },
                    onOpenContent = { navController.navigate("content/0") },
                    onOpenEsma = { navController.navigate("content/0") },
                    onOpenCalendar = { navController.navigate("calendar") },
                    onOpenSettings = { navController.navigate("settings") },
                    onOpenAbout = { navController.navigate("about") },
                )
            }
            composable("qibla-screen") {
                QiblaScreen(vm = vm, onBack = { navController.popBackStack() })
            }
            composable("monthly-screen") {
                MonthlyScreen(vm = vm, onBack = { navController.popBackStack() })
            }
            composable("location") {
                LocationPickerScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() },
                    onSelected = { navController.popBackStack() },
                )
            }
            composable("settings") {
                SettingsScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() },
                    onOpenAbout = { navController.navigate("about") },
                )
            }
            composable("tracking") {
                TrackingScreen(vm = vm, onBack = { navController.popBackStack() })
            }
            composable("zikir") {
                ZikirScreen(vm = vm, onBack = { navController.popBackStack() })
            }
            composable("calendar") {
                CalendarScreen(vm = vm, onBack = { navController.popBackStack() })
            }
            composable("content/{tab}") { entry ->
                val tab = entry.arguments?.getString("tab")?.toIntOrNull() ?: 0
                ContentScreen(initialTab = tab, onBack = { navController.popBackStack() })
            }
            composable("about") {
                AboutScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
