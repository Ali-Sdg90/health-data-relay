package com.alisadeghi.autohealthsync.ui

import android.Manifest
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.content.ContextCompat
import com.alisadeghi.autohealthsync.R
import com.alisadeghi.autohealthsync.app.AutoHealthSyncApp
import com.alisadeghi.autohealthsync.backup.BackupOutcome
import com.alisadeghi.autohealthsync.backup.BackupTrigger
import com.alisadeghi.autohealthsync.drive.AuthorizationOutcome
import com.alisadeghi.autohealthsync.model.ActivitySeverity
import com.alisadeghi.autohealthsync.model.AppState
import com.alisadeghi.autohealthsync.model.BackupSettings
import com.alisadeghi.autohealthsync.model.ConnectionState
import com.alisadeghi.autohealthsync.model.localTime
import com.alisadeghi.autohealthsync.model.normalized
import com.alisadeghi.autohealthsync.system.BackgroundAccessStatus
import com.alisadeghi.autohealthsync.util.DateUtils
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as AutoHealthSyncApp).container
    private val healthStatus = MutableStateFlow(ConnectionState.CHECKING)
    private val driveStatus = MutableStateFlow(ConnectionState.CHECKING)
    private val isBackingUp = MutableStateFlow(false)
    private val statusText = MutableStateFlow<UiText?>(null)
    private val selectedBackupDate = MutableStateFlow(DateUtils.today())
    private val notificationGranted = MutableStateFlow(hasNotificationPermission())
    private val backgroundAccess = MutableStateFlow(container.backgroundAccessManager.status)
    private val eventChannel = Channel<UiEvent>(Channel.BUFFERED)

    val healthPermissions: Set<String>
        get() = container.healthManager.requestedPermissions

    val events = eventChannel.receiveAsFlow()

    private val backupActionState = combine(
        isBackingUp,
        statusText,
        selectedBackupDate,
    ) { backingUp, text, date -> BackupActionState(backingUp, text, date) }

    private val setupState = combine(
        notificationGranted,
        backgroundAccess,
    ) { notifications, background -> SystemSetupState(notifications, background) }

    val uiState = combine(
        container.stateStore.state,
        healthStatus,
        driveStatus,
        backupActionState,
        setupState,
    ) { state, health, drive, backupAction, setup ->
        MainUiState(
            appState = state,
            isAppStateLoaded = true,
            healthState = health,
            driveState = drive,
            isBackingUp = backupAction.isBackingUp,
            operationStatus = backupAction.status,
            selectedBackupDate = backupAction.date,
            notificationGranted = setup.notificationGranted,
            backgroundAccess = setup.backgroundAccess,
            nextBackupEpochMillis = DateUtils.nextBackup(state.backupSettings.localTime())
                .toInstant()
                .toEpochMilli(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState())

    init {
        refreshConnections()
    }

    fun refreshConnections() {
        notificationGranted.value = hasNotificationPermission()
        backgroundAccess.value = container.backgroundAccessManager.status
        viewModelScope.launch {
            healthStatus.value = ConnectionState.CHECKING
            healthStatus.value = when {
                !container.healthManager.isAvailable -> ConnectionState.UNAVAILABLE
                container.healthManager.hasCompleteAccess() -> ConnectionState.CONNECTED
                else -> ConnectionState.ACTION_REQUIRED
            }

            driveStatus.value = ConnectionState.CHECKING
            driveStatus.value = try {
                when (container.driveAuthorizationManager.authorize()) {
                    is AuthorizationOutcome.Authorized -> ConnectionState.CONNECTED
                    is AuthorizationOutcome.UserActionRequired -> ConnectionState.ACTION_REQUIRED
                    is AuthorizationOutcome.Unavailable -> ConnectionState.UNAVAILABLE
                }
            } catch (_: Exception) {
                ConnectionState.ACTION_REQUIRED
            }
        }
    }

    fun requestDriveConnection() {
        viewModelScope.launch {
            driveStatus.value = ConnectionState.CHECKING
            try {
                when (val outcome = container.driveAuthorizationManager.authorize()) {
                    is AuthorizationOutcome.Authorized -> onDriveConnected()
                    is AuthorizationOutcome.UserActionRequired -> {
                        driveStatus.value = ConnectionState.ACTION_REQUIRED
                        eventChannel.send(UiEvent.ResolveDriveAuthorization(outcome.pendingIntent))
                    }
                    is AuthorizationOutcome.Unavailable -> {
                        driveStatus.value = ConnectionState.UNAVAILABLE
                        eventChannel.send(UiEvent.Message(backupMessage(outcome.reason)))
                    }
                }
            } catch (_: Exception) {
                driveStatus.value = ConnectionState.ACTION_REQUIRED
                eventChannel.send(UiEvent.Message(resource(R.string.message_drive_auth_start_failed)))
            }
        }
    }

    fun completeDriveConnection(data: Intent?) {
        if (data == null) {
            driveStatus.value = ConnectionState.ACTION_REQUIRED
            return
        }
        viewModelScope.launch {
            try {
                when (container.driveAuthorizationManager.completeAuthorization(data)) {
                    is AuthorizationOutcome.Authorized -> onDriveConnected()
                    else -> driveStatus.value = ConnectionState.ACTION_REQUIRED
                }
            } catch (_: Exception) {
                driveStatus.value = ConnectionState.ACTION_REQUIRED
                eventChannel.send(UiEvent.Message(resource(R.string.message_drive_access_denied)))
            }
        }
    }

    fun onHealthPermissionsResult(granted: Set<String>) {
        viewModelScope.launch {
            if (granted.containsAll(healthPermissions)) {
                healthStatus.value = ConnectionState.CONNECTED
                container.stateStore.addActivity(ActivitySeverity.SUCCESS, "Health Connect connected")
            } else {
                healthStatus.value = ConnectionState.ACTION_REQUIRED
                container.stateStore.addActivity(
                    ActivitySeverity.WARNING,
                    "Health permission missing",
                    "Backups require all requested read permissions",
                )
                eventChannel.send(UiEvent.Message(resource(R.string.message_health_access_incomplete)))
            }
        }
    }

    fun requestNotificationPermission() {
        viewModelScope.launch {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || hasNotificationPermission()) {
                notificationGranted.value = true
            } else {
                eventChannel.send(UiEvent.RequestNotificationPermission)
            }
        }
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        notificationGranted.value = granted || hasNotificationPermission()
    }

    fun openBatterySettings() {
        viewModelScope.launch {
            eventChannel.send(
                UiEvent.OpenSystemSettings(container.backgroundAccessManager.batterySettingsIntent()),
            )
        }
    }

    fun openAutoStartSettings() {
        viewModelScope.launch {
            val intent = container.backgroundAccessManager.autoStartSettingsIntent()
                ?: container.backgroundAccessManager.appDetailsIntent()
            eventChannel.send(UiEvent.OpenSystemSettings(intent))
        }
    }

    fun confirmAutoStart() {
        viewModelScope.launch {
            container.stateStore.setAutoStartConfirmed(true)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            val state = container.stateStore.current()
            val background = container.backgroundAccessManager.status
            val autoStartReady = !background.autoStartSettingsAvailable || state.autoStartConfirmed
            val ready = healthStatus.value == ConnectionState.CONNECTED &&
                driveStatus.value == ConnectionState.CONNECTED &&
                background.batteryAccessGranted && autoStartReady
            if (!ready) {
                backgroundAccess.value = background
                eventChannel.send(UiEvent.Message(resource(R.string.message_complete_setup)))
                return@launch
            }
            container.stateStore.completeOnboarding()
            container.backupScheduler.ensureNextBackupScheduled()
            container.stateStore.addActivity(
                ActivitySeverity.SUCCESS,
                "Setup completed",
                "Automatic backups are ready",
            )
            eventChannel.send(UiEvent.Message(resource(R.string.message_backups_ready)))
        }
    }

    fun requestHealthConnection() {
        viewModelScope.launch {
            if (container.healthManager.isAvailable) {
                eventChannel.send(UiEvent.RequestHealthPermissions)
            } else {
                eventChannel.send(UiEvent.OpenHealthConnectStore)
            }
        }
    }

    fun openHealthConnect() {
        viewModelScope.launch { eventChannel.send(UiEvent.OpenHealthConnect) }
    }

    fun openGoogleDrive() {
        viewModelScope.launch {
            eventChannel.send(UiEvent.OpenGoogleDrive(container.stateStore.current().driveFolderId))
        }
    }

    fun saveSettings(settings: BackupSettings) {
        viewModelScope.launch {
            val normalized = settings.normalized()
            container.stateStore.setBackupSettings(normalized)
            container.backupScheduler.rescheduleNextBackup(normalized)
            eventChannel.send(UiEvent.Message(resource(R.string.message_settings_saved)))
        }
    }

    fun backupNow() {
        if (isBackingUp.value) return
        viewModelScope.launch {
            val date = selectedBackupDate.value
            val dateLabel = backupDateLabel(date)
            isBackingUp.value = true
            statusText.value = resource(R.string.message_collecting_summary, dateLabel)
            when (val outcome = container.backupCoordinator.run(BackupTrigger.MANUAL, date)) {
                is BackupOutcome.Success -> {
                    statusText.value = if (outcome.updatedExisting) {
                        resource(R.string.message_backup_updated, dateLabel)
                    } else {
                        resource(R.string.message_backup_safe, dateLabel)
                    }
                    eventChannel.send(UiEvent.Message(resource(R.string.message_backup_completed, outcome.fileName)))
                }
                is BackupOutcome.ActionRequired -> {
                    statusText.value = backupMessage(outcome.message)
                    refreshConnections()
                    eventChannel.send(UiEvent.Message(backupMessage(outcome.message)))
                }
                is BackupOutcome.RetryableFailure -> {
                    statusText.value = backupMessage(outcome.message)
                    eventChannel.send(UiEvent.Message(resource(R.string.message_backup_try_again)))
                }
                is BackupOutcome.PermanentFailure -> {
                    statusText.value = backupMessage(outcome.message)
                    eventChannel.send(UiEvent.Message(backupMessage(outcome.message)))
                }
            }
            isBackingUp.value = false
        }
    }

    fun selectBackupDate(date: LocalDate) {
        if (!date.isAfter(DateUtils.today())) {
            selectedBackupDate.value = date
            statusText.value = null
        }
    }

    private suspend fun onDriveConnected() {
        driveStatus.value = ConnectionState.CONNECTED
        container.stateStore.addActivity(ActivitySeverity.SUCCESS, "Google Drive connected")
        eventChannel.send(UiEvent.Message(resource(R.string.message_drive_connected)))
    }

    private fun hasNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    private fun backupDateLabel(date: LocalDate): String {
        val context = getApplication<Application>()
        val today = DateUtils.today()
        return when (date) {
            today -> context.getString(R.string.date_today_lower)
            today.minusDays(1) -> context.getString(R.string.date_yesterday_lower)
            else -> date.format(
                DateTimeFormatter.ofPattern(
                    "MMM d, yyyy",
                    context.resources.configuration.locales[0],
                ),
            )
        }
    }
}

private data class BackupActionState(
    val isBackingUp: Boolean,
    val status: UiText?,
    val date: LocalDate,
)

private data class SystemSetupState(
    val notificationGranted: Boolean,
    val backgroundAccess: BackgroundAccessStatus,
)

private fun resource(resourceId: Int, vararg arguments: Any): UiText =
    UiText.Resource(resourceId, arguments.toList())

private fun backupMessage(message: String): UiText = when (message) {
    "Google Drive did not return an access token" -> resource(R.string.error_drive_token_missing)
    "Health Connect permission required" -> resource(R.string.error_health_permission_required)
    "Google Drive authorization required" -> resource(R.string.error_drive_authorization_required)
    "Required access was revoked" -> resource(R.string.error_access_revoked)
    "Could not create the daily JSON" -> resource(R.string.error_json_creation)
    "Network or Google Drive request failed" -> resource(R.string.error_network_drive)
    "Health backup could not be completed" -> resource(R.string.error_backup_failed)
    else -> UiText.Raw(message)
}

data class MainUiState(
    val appState: AppState = AppState(),
    val isAppStateLoaded: Boolean = false,
    val healthState: ConnectionState = ConnectionState.CHECKING,
    val driveState: ConnectionState = ConnectionState.CHECKING,
    val isBackingUp: Boolean = false,
    val operationStatus: UiText? = null,
    val selectedBackupDate: LocalDate = DateUtils.today(),
    val notificationGranted: Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU,
    val backgroundAccess: BackgroundAccessStatus = BackgroundAccessStatus(),
    val nextBackupEpochMillis: Long = DateUtils.nextBackup().toInstant().toEpochMilli(),
) {
    val autoStartReady: Boolean
        get() = !backgroundAccess.autoStartSettingsAvailable || appState.autoStartConfirmed

    val requiredSetupComplete: Boolean
        get() = healthState == ConnectionState.CONNECTED &&
            driveState == ConnectionState.CONNECTED &&
            backgroundAccess.batteryAccessGranted && autoStartReady

    val showOnboarding: Boolean
        get() = isAppStateLoaded && !appState.onboardingCompleted
}

sealed interface UiEvent {
    data class ResolveDriveAuthorization(val pendingIntent: PendingIntent) : UiEvent
    data class Message(val text: UiText) : UiEvent
    data object RequestHealthPermissions : UiEvent
    data object RequestNotificationPermission : UiEvent
    data object OpenHealthConnectStore : UiEvent
    data object OpenHealthConnect : UiEvent
    data class OpenGoogleDrive(val folderId: String?) : UiEvent
    data class OpenSystemSettings(val intent: Intent) : UiEvent
}
