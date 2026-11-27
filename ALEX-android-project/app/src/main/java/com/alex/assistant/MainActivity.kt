package com.alex.assistant

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alex.assistant.service.AlexForegroundService
import com.alex.assistant.ui.AlexViewModel
import com.alex.assistant.ui.screens.HomeScreen
import com.alex.assistant.ui.screens.MemoryScreen
import com.alex.assistant.ui.screens.OnboardingScreen
import com.alex.assistant.ui.screens.PermissionsScreen
import com.alex.assistant.ui.screens.SettingsScreen
import com.alex.assistant.ui.theme.AlexTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AlexViewModel by viewModels {
        AlexViewModel.factory((application as AlexApplication).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as AlexApplication).container

        setContent {
            AlexTheme {
                val uiState by viewModel.uiState.collectAsState()
                val navController = rememberNavController()

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) {
                    viewModel.refreshTelemetryAndPermissions()
                }

                LaunchedEffect(uiState.settings.backgroundListeningEnabled) {
                    val serviceIntent = Intent(this@MainActivity, AlexForegroundService::class.java)
                    if (uiState.settings.backgroundListeningEnabled) {
                        startForegroundService(serviceIntent)
                    } else {
                        stopService(serviceIntent)
                    }
                }

                val startDestination = if (uiState.settings.onboardingCompleted) "home" else "onboarding"

                NavHost(navController = navController, startDestination = startDestination) {
                    composable("onboarding") {
                        OnboardingScreen(
                            selectedLanguage = uiState.settings.languagePreference,
                            onSelectLanguage = viewModel::setLanguagePreference,
                            onContinueToPermissions = { navController.navigate("permissions") }
                        )
                    }
                    composable("permissions") {
                        PermissionsScreen(
                            permissions = uiState.permissions,
                            onRequestPermission = { perm ->
                                when (perm.key) {
                                    "notification_listener" -> container.permissionsManager.openNotificationListenerSettings()
                                    "exact_alarm" -> container.permissionsManager.openExactAlarmSettings()
                                    else -> permissionLauncher.launch(perm.key)
                                }
                            },
                            onContinue = {
                                viewModel.completeOnboarding()
                                navController.navigate("home") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("home") {
                        HomeScreen(
                            uiState = uiState,
                            onInputChanged = viewModel::onInputChanged,
                            onSendCommand = { viewModel.sendCommand(it) },
                            onToggleMic = viewModel::toggleVoiceInput,
                            onOpenMemory = { navController.navigate("memory") },
                            onOpenSettings = { navController.navigate("settings") }
                        )
                    }
                    composable("memory") {
                        MemoryScreen(
                            memories = uiState.memories,
                            onSaveMemory = viewModel::saveMemory,
                            onDeleteMemory = viewModel::deleteMemory,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            settings = uiState.settings,
                            onToggleBackgroundListening = viewModel::setBackgroundListening,
                            onSpeechRateChange = viewModel::setSpeechRate,
                            onOpenPermissions = { navController.navigate("permissions") },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshTelemetryAndPermissions()
    }
}
