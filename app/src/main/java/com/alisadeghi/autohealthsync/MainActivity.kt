package com.alisadeghi.autohealthsync

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.HealthConnectClient
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alisadeghi.autohealthsync.ui.MainScreen
import com.alisadeghi.autohealthsync.ui.MainViewModel
import com.alisadeghi.autohealthsync.ui.UiEvent
import com.alisadeghi.autohealthsync.ui.resolve
import com.alisadeghi.autohealthsync.ui.theme.AutoHealthSyncTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AutoHealthSyncTheme {
                val viewModel: MainViewModel = viewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                val snackbar = remember { SnackbarHostState() }
                val curtain = remember { Animatable(0f) }
                val transitionScope = rememberCoroutineScope()
                var languageChanging by remember { mutableStateOf(false) }
                val visibleLanguage = rememberUpdatedState(LocalConfiguration.current.locales[0].language)
                val healthLauncher = rememberLauncherForActivityResult(
                    PermissionController.createRequestPermissionResultContract(),
                    viewModel::onHealthPermissionsResult,
                )
                val driveLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartIntentSenderForResult(),
                ) { result -> viewModel.completeDriveConnection(result.data) }
                val notificationLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission(),
                    viewModel::onNotificationPermissionResult,
                )

                LaunchedEffect(Unit) {
                    viewModel.syncAppLanguage(AppCompatDelegate.getApplicationLocales()[0]?.language)
                }

                LaunchedEffect(Unit) {
                    viewModel.events.collect { event ->
                        when (event) {
                            is UiEvent.Message -> snackbar.showSnackbar(event.text.resolve(this@MainActivity))
                            is UiEvent.ResolveDriveAuthorization -> driveLauncher.launch(
                                IntentSenderRequest.Builder(event.pendingIntent.intentSender).build(),
                            )
                            UiEvent.RequestHealthPermissions -> healthLauncher.launch(viewModel.healthPermissions)
                            UiEvent.RequestNotificationPermission -> notificationLauncher.launch(
                                android.Manifest.permission.POST_NOTIFICATIONS,
                            )
                            UiEvent.OpenHealthConnectStore -> openHealthConnectStore()
                            UiEvent.OpenHealthConnect -> openHealthConnect()
                            is UiEvent.OpenGoogleDrive -> openGoogleDrive(event.folderId)
                            is UiEvent.OpenSystemSettings -> openSystemSettings(event.intent)
                        }
                    }
                }
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                    viewModel.refreshConnections()
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Scaffold(
                        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
                        containerColor = Color.Transparent,
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        snackbarHost = { SnackbarHost(snackbar) },
                    ) { padding ->
                        MainScreen(
                            state = state,
                            onHealthConnect = viewModel::requestHealthConnection,
                            onDriveConnect = viewModel::requestDriveConnection,
                            onOpenHealthConnect = viewModel::openHealthConnect,
                            onOpenGoogleDrive = viewModel::openGoogleDrive,
                            onBackupNow = viewModel::backupNow,
                            onBackupDateChange = viewModel::selectBackupDate,
                            onSaveSettings = viewModel::saveSettings,
                            onRequestNotifications = viewModel::requestNotificationPermission,
                            onOpenBatterySettings = viewModel::openBatterySettings,
                            onOpenAutoStartSettings = viewModel::openAutoStartSettings,
                            onConfirmAutoStart = viewModel::confirmAutoStart,
                            onCompleteOnboarding = viewModel::completeOnboarding,
                            onLanguageChange = { languageTag ->
                                if (!languageChanging && visibleLanguage.value != languageTag) {
                                    languageChanging = true
                                    transitionScope.launch {
                                        try {
                                            curtain.animateTo(
                                                1f,
                                                tween(durationMillis = 140, easing = FastOutSlowInEasing),
                                            )
                                            withFrameNanos { }
                                            delay(24)
                                            viewModel.changeLanguage(languageTag, ::setAppLanguage)
                                            snapshotFlow { visibleLanguage.value }
                                                .first { it == languageTag }
                                            withFrameNanos { }
                                            delay(50)
                                            curtain.animateTo(
                                                0f,
                                                tween(durationMillis = 180, easing = FastOutSlowInEasing),
                                            )
                                        } finally {
                                            curtain.snapTo(0f)
                                            languageChanging = false
                                        }
                                    }
                                }
                            },
                            onTestModeChange = viewModel::setTestModeEnabled,
                            onExitTestPreview = viewModel::exitTestPreview,
                            contentPadding = padding,
                        )
                    }
                }
                if (languageChanging) {
                    Dialog(
                        onDismissRequest = { },
                        properties = DialogProperties(
                            dismissOnBackPress = false,
                            dismissOnClickOutside = false,
                            usePlatformDefaultWidth = false,
                            decorFitsSystemWindows = false,
                        ),
                    ) {
                        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
                        DisposableEffect(dialogWindow) {
                            dialogWindow?.apply {
                                setDimAmount(0f)
                                setBackgroundDrawableResource(android.R.color.transparent)
                                setLayout(
                                    WindowManager.LayoutParams.MATCH_PARENT,
                                    WindowManager.LayoutParams.MATCH_PARENT,
                                )
                            }
                            onDispose { }
                        }
                        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = curtain.value)))
                    }
                }
            }
        }
    }

    private fun setAppLanguage(languageTag: String) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(languageTag),
        )
    }

    private fun openHealthConnectStore() {
        val market = "market://details?id=com.google.android.apps.healthdata".toUri()
        val web = "https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata".toUri()
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, market)) }
            .onFailure { startActivity(Intent(Intent.ACTION_VIEW, web)) }
    }

    private fun openHealthConnect() {
        runCatching {
            startActivity(HealthConnectClient.getHealthConnectManageDataIntent(this))
        }.onFailure {
            openHealthConnectStore()
        }
    }

    private fun openGoogleDrive(folderId: String?) {
        val uri = if (folderId == null) {
            "https://drive.google.com/drive/my-drive".toUri()
        } else {
            "https://drive.google.com/drive/folders/$folderId".toUri()
        }
        val driveIntent = Intent(Intent.ACTION_VIEW, uri).setPackage(GOOGLE_DRIVE_PACKAGE)
        runCatching { startActivity(driveIntent) }
            .onFailure { startActivity(Intent(Intent.ACTION_VIEW, uri)) }
    }

    private fun openSystemSettings(intent: Intent) {
        runCatching { startActivity(intent) }
            .onFailure {
                startActivity(
                    Intent(
                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        "package:$packageName".toUri(),
                    ),
                )
            }
    }

    companion object {
        private const val GOOGLE_DRIVE_PACKAGE = "com.google.android.apps.docs"
    }
}
