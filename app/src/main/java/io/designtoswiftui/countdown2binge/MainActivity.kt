package io.designtoswiftui.countdown2binge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.designtoswiftui.countdown2binge.services.settings.SettingsRepository
import io.designtoswiftui.countdown2binge.ui.navigation.BottomNavBar
import io.designtoswiftui.countdown2binge.ui.navigation.NavGraph
import io.designtoswiftui.countdown2binge.ui.navigation.Screen
import io.designtoswiftui.countdown2binge.ui.navigation.shouldShowBottomBar
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Countdown2BingeTheme {
                val hasCompletedOnboarding by settingsRepository.hasCompletedOnboarding.collectAsState(initial = null)

                // Show nothing while loading onboarding state
                if (hasCompletedOnboarding != null) {
                    MainScreen(hasCompletedOnboarding = hasCompletedOnboarding!!)
                }
            }
        }
    }
}

@Composable
fun MainScreen(hasCompletedOnboarding: Boolean) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val startDestination = if (hasCompletedOnboarding) {
        Screen.Timeline.route
    } else {
        Screen.Onboarding.route
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Background,
        bottomBar = {
            AnimatedVisibility(
                visible = shouldShowBottomBar(currentRoute),
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            startDestination = startDestination
        )
    }
}
