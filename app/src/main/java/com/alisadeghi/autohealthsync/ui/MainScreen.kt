package com.alisadeghi.autohealthsync.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alisadeghi.autohealthsync.BuildConfig
import com.alisadeghi.autohealthsync.R
import com.alisadeghi.autohealthsync.model.ActivityEntry
import com.alisadeghi.autohealthsync.model.ActivitySeverity
import com.alisadeghi.autohealthsync.model.BackupSettings
import com.alisadeghi.autohealthsync.model.BackupMetric
import com.alisadeghi.autohealthsync.model.ConnectionState
import com.alisadeghi.autohealthsync.model.FileDateSystem
import com.alisadeghi.autohealthsync.model.MAX_DRIVE_FOLDER_NAME_LENGTH
import com.alisadeghi.autohealthsync.util.DateUtils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun MainScreen(
    state: MainUiState,
    onHealthConnect: () -> Unit,
    onDriveConnect: () -> Unit,
    onOpenHealthConnect: () -> Unit,
    onOpenGoogleDrive: () -> Unit,
    onBackupNow: () -> Unit,
    onBackupDateChange: (LocalDate) -> Unit,
    onSaveSettings: (BackupSettings) -> Unit,
    onRequestNotifications: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    onOpenAutoStartSettings: () -> Unit,
    onConfirmAutoStart: () -> Unit,
    onCompleteOnboarding: () -> Unit,
    onLanguageChange: (String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    var settingsVisible by remember { mutableStateOf(false) }

    if (!state.isAppStateLoaded) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        return
    }

    if (state.showOnboarding) {
        OnboardingScreen(
            state = state,
            onHealthConnect = onHealthConnect,
            onDriveConnect = onDriveConnect,
            onRequestNotifications = onRequestNotifications,
            onOpenBatterySettings = onOpenBatterySettings,
            onOpenAutoStartSettings = onOpenAutoStartSettings,
            onConfirmAutoStart = onConfirmAutoStart,
            onComplete = onCompleteOnboarding,
            onLanguageChange = onLanguageChange,
            contentPadding = contentPadding,
        )
        return
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = contentPadding.calculateTopPadding() + 20.dp,
                bottom = contentPadding.calculateBottomPadding() + 28.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item { AppHeader(onSettingsClick = { settingsVisible = true }) }
            item {
                ConnectionsCard(
                    healthState = state.healthState,
                    driveState = state.driveState,
                    driveFolderName = state.appState.backupSettings.driveFolderName,
                    selectedDate = state.selectedBackupDate,
                    dateSelectionEnabled = !state.isBackingUp,
                    onHealthConnect = onHealthConnect,
                    onDriveConnect = onDriveConnect,
                    onOpenHealthConnect = onOpenHealthConnect,
                    onOpenGoogleDrive = onOpenGoogleDrive,
                    onDateChange = onBackupDateChange,
                )
            }
            item { ScheduleCard(state) }
            item {
                BackupAction(
                    isBackingUp = state.isBackingUp,
                    status = state.operationStatus,
                    enabled = state.healthState == ConnectionState.CONNECTED &&
                        state.driveState == ConnectionState.CONNECTED,
                    onBackupNow = onBackupNow,
                )
            }
            item { RecentActivitySection(state.appState.recentActivity) }
            item { AppFooter() }
        }
    }

    if (settingsVisible) {
        SettingsSheet(
            settings = state.appState.backupSettings,
            onLanguageChange = onLanguageChange,
            onDismiss = { settingsVisible = false },
            onSave = {
                onSaveSettings(it)
                settingsVisible = false
            },
        )
    }
}

@Composable
private fun OnboardingScreen(
    state: MainUiState,
    onHealthConnect: () -> Unit,
    onDriveConnect: () -> Unit,
    onRequestNotifications: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    onOpenAutoStartSettings: () -> Unit,
    onConfirmAutoStart: () -> Unit,
    onComplete: () -> Unit,
    onLanguageChange: (String) -> Unit,
    contentPadding: PaddingValues,
) {
    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    val currentLanguage = LocalConfiguration.current.locales[0].language

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = contentPadding.calculateTopPadding() + 18.dp,
                    bottom = contentPadding.calculateBottomPadding() + 18.dp,
                ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.step_progress, currentStep + 1, ONBOARDING_STEP_COUNT),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    repeat(ONBOARDING_STEP_COUNT) { index ->
                        Box(
                            modifier = Modifier
                                .width(if (index == currentStep) 28.dp else 8.dp)
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index <= currentStep) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant,
                                ),
                        )
                    }
                }
            }
            Spacer(Modifier.height(18.dp))

            AnimatedContent(
                targetState = currentStep,
                modifier = Modifier.weight(1f),
                label = "onboarding-step",
            ) { step ->
                when (step) {
                    0 -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 14.dp),
                    ) {
                        item {
                            OnboardingPageHeader(
                                icon = Icons.Rounded.Favorite,
                                title = stringResource(R.string.welcome_title),
                                body = stringResource(R.string.welcome_body),
                            )
                        }
                        item {
                            Text(
                                stringResource(R.string.choose_language_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                stringResource(R.string.choose_language_body),
                                modifier = Modifier.padding(top = 4.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        item {
                            LanguageChoiceCard(
                                iconLabel = stringResource(R.string.language_english_code),
                                title = stringResource(R.string.language_english_native),
                                subtitle = stringResource(R.string.language_english),
                                selected = currentLanguage != LANGUAGE_PERSIAN,
                                onClick = { onLanguageChange(LANGUAGE_ENGLISH) },
                            )
                        }
                        item {
                            LanguageChoiceCard(
                                iconLabel = stringResource(R.string.language_persian_code),
                                title = stringResource(R.string.language_persian_native),
                                subtitle = stringResource(R.string.language_persian),
                                selected = currentLanguage == LANGUAGE_PERSIAN,
                                onClick = { onLanguageChange(LANGUAGE_PERSIAN) },
                            )
                        }
                    }

                    1 -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 14.dp),
                    ) {
                        item {
                            OnboardingPageHeader(
                                icon = Icons.Rounded.HealthAndSafety,
                                title = stringResource(R.string.permissions_title),
                                body = stringResource(R.string.permissions_body),
                            )
                        }
                        item {
                            SetupStepCard(
                                icon = Icons.Rounded.HealthAndSafety,
                                title = stringResource(R.string.health_connect),
                                description = stringResource(R.string.health_connect_description),
                                complete = state.healthState == ConnectionState.CONNECTED,
                                checking = state.healthState == ConnectionState.CHECKING,
                                unavailable = state.healthState == ConnectionState.UNAVAILABLE,
                                actionLabel = stringResource(
                                    if (state.healthState == ConnectionState.UNAVAILABLE) {
                                        R.string.install_action
                                    } else {
                                        R.string.allow_action
                                    },
                                ),
                                onAction = onHealthConnect,
                            )
                        }
                        item {
                            SetupStepCard(
                                icon = Icons.Rounded.Cloud,
                                title = stringResource(R.string.google_drive),
                                description = stringResource(R.string.google_drive_description),
                                complete = state.driveState == ConnectionState.CONNECTED,
                                checking = state.driveState == ConnectionState.CHECKING,
                                unavailable = state.driveState == ConnectionState.UNAVAILABLE,
                                actionLabel = stringResource(R.string.connect_action),
                                onAction = onDriveConnect,
                            )
                        }
                        item {
                            SetupStepCard(
                                icon = Icons.Rounded.BatteryChargingFull,
                                title = stringResource(R.string.battery_access_title),
                                description = stringResource(
                                    if (state.backgroundAccess.backgroundRestricted) {
                                        R.string.battery_access_restricted
                                    } else {
                                        R.string.battery_access_description
                                    },
                                ),
                                complete = state.backgroundAccess.batteryAccessGranted,
                                actionLabel = stringResource(R.string.open_settings_action),
                                onAction = onOpenBatterySettings,
                            )
                        }
                        if (state.backgroundAccess.autoStartSettingsAvailable) {
                            item {
                                AutoStartStepCard(
                                    manufacturer = state.backgroundAccess.manufacturerName,
                                    complete = state.autoStartReady,
                                    onOpenSettings = onOpenAutoStartSettings,
                                    onConfirm = onConfirmAutoStart,
                                )
                            }
                        }
                        item {
                            SetupStepCard(
                                icon = Icons.Rounded.NotificationsActive,
                                title = stringResource(R.string.backup_notifications),
                                description = stringResource(R.string.backup_notifications_description),
                                complete = state.notificationGranted,
                                optional = true,
                                actionLabel = stringResource(R.string.allow_action),
                                onAction = onRequestNotifications,
                            )
                        }
                    }

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(13.dp),
                        contentPadding = PaddingValues(bottom = 14.dp),
                    ) {
                        item {
                            OnboardingPageHeader(
                                icon = Icons.Rounded.Check,
                                title = stringResource(R.string.ready_to_begin_title),
                                body = stringResource(R.string.ready_to_begin_body),
                                success = true,
                            )
                        }
                        item {
                            Text(
                                stringResource(R.string.how_it_works_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        item {
                            HowItWorksCard(
                                icon = Icons.Rounded.HealthAndSafety,
                                title = stringResource(R.string.how_it_works_collect_title),
                                body = stringResource(R.string.how_it_works_collect_body),
                            )
                        }
                        item {
                            HowItWorksCard(
                                icon = Icons.Rounded.Cloud,
                                title = stringResource(R.string.how_it_works_backup_title),
                                body = stringResource(R.string.how_it_works_backup_body),
                            )
                        }
                        item {
                            HowItWorksCard(
                                icon = Icons.Rounded.Schedule,
                                title = stringResource(R.string.how_it_works_automatic_title),
                                body = stringResource(R.string.how_it_works_automatic_body),
                            )
                        }
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.62f),
                                ),
                                shape = RoundedCornerShape(18.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Icon(
                                        Icons.Rounded.PrivacyTip,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        stringResource(R.string.privacy_reassurance),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (currentStep == 1 && !state.requiredSetupComplete) {
                Text(
                    stringResource(R.string.complete_required_steps),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (currentStep > 0) {
                    OutlinedButton(
                        onClick = { currentStep -= 1 },
                        modifier = Modifier
                            .weight(0.42f)
                            .height(54.dp),
                        shape = RoundedCornerShape(17.dp),
                    ) {
                        Text(stringResource(R.string.back_action), fontWeight = FontWeight.SemiBold)
                    }
                }
                Button(
                    onClick = {
                        if (currentStep == ONBOARDING_STEP_COUNT - 1) onComplete() else currentStep += 1
                    },
                    enabled = currentStep != 1 || state.requiredSetupComplete,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(17.dp),
                ) {
                    Text(
                        stringResource(
                            when (currentStep) {
                                0 -> R.string.continue_action
                                1 -> R.string.next_action
                                else -> R.string.begin_action
                            },
                        ),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageHeader(
    icon: ImageVector,
    title: String,
    body: String,
    success: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(19.dp))
                .background(
                    if (success) Color(0xFF20A67A).copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.primaryContainer,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (success) Color(0xFF16805F) else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(31.dp),
            )
        }
        Text(
            title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LanguageChoiceCard(
    iconLabel: String,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            },
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(17.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.76f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(iconLabel, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (subtitle != title) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (selected) {
                Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun HowItWorksCard(icon: ImageVector, title: String, body: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    body,
                    modifier = Modifier.padding(top = 3.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private const val ONBOARDING_STEP_COUNT = 3
private const val LANGUAGE_ENGLISH = "en"
private const val LANGUAGE_PERSIAN = "fa"

@Composable
private fun SetupStepCard(
    icon: ImageVector,
    title: String,
    description: String,
    complete: Boolean,
    actionLabel: String,
    onAction: () -> Unit,
    checking: Boolean = false,
    unavailable: Boolean = false,
    optional: Boolean = false,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(22.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(17.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (complete) Color(0xFF20A67A).copy(alpha = 0.14f)
                        else MaterialTheme.colorScheme.primaryContainer,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (complete) Icons.Rounded.Check else icon,
                    contentDescription = null,
                    tint = if (complete) Color(0xFF16805F) else MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (optional) {
                        Text(
                            "  ${stringResource(R.string.optional_label)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    when {
                        complete -> stringResource(R.string.ready_label)
                        unavailable -> stringResource(R.string.not_available_label)
                        else -> description
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(8.dp))
            when {
                checking -> CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                complete -> Unit
                else -> TextButton(onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}

@Composable
private fun AutoStartStepCard(
    manufacturer: String,
    complete: Boolean,
    onOpenSettings: () -> Unit,
    onConfirm: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (complete) Color(0xFF20A67A).copy(alpha = 0.14f)
                            else MaterialTheme.colorScheme.primaryContainer,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (complete) Icons.Rounded.Check else Icons.Rounded.RocketLaunch,
                        contentDescription = null,
                        tint = if (complete) Color(0xFF16805F) else MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.width(13.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.auto_start),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        if (complete) {
                            stringResource(R.string.ready_label)
                        } else {
                            stringResource(R.string.auto_start_description, manufacturer)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (!complete) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) { Text(stringResource(R.string.open_settings_action)) }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) { Text(stringResource(R.string.auto_start_confirm_action)) }
                }
            }
        }
    }
}

@Composable
private fun AppFooter() {
    val uriHandler = LocalUriHandler.current
    Text(
        text = stringResource(R.string.footer_credit, BuildConfig.VERSION_NAME),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { uriHandler.openUri(PROJECT_SOURCE_URL) }
            .padding(top = 8.dp, bottom = 8.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
        style = MaterialTheme.typography.labelMedium,
        textAlign = TextAlign.Center,
    )
}

private const val PROJECT_SOURCE_URL = "https://github.com/Ali-Sdg90/health-data-relay"
private const val PRIVACY_POLICY_URL = "https://ali-sdg.is-a.dev/health-data-relay/privacy/"
private const val TERMS_OF_SERVICE_URL = "https://ali-sdg.is-a.dev/health-data-relay/terms/"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSheet(
    settings: BackupSettings,
    onLanguageChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (BackupSettings) -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    var backupHour by remember(settings.backupHour) { mutableStateOf(settings.backupHour) }
    var backupMinute by remember(settings.backupMinute) { mutableStateOf(settings.backupMinute) }
    var folderName by remember(settings.driveFolderName) { mutableStateOf(settings.driveFolderName) }
    var dateSystem by remember(settings.fileDateSystem) { mutableStateOf(settings.fileDateSystem) }
    var includedMetrics by remember(settings.includedMetrics) {
        mutableStateOf(settings.includedMetrics)
    }
    var timePickerVisible by remember { mutableStateOf(false) }
    var backupDataVisible by remember { mutableStateOf(false) }
    var legalExpanded by remember { mutableStateOf(false) }
    val currentLanguage = LocalConfiguration.current.locales[0].language
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 22.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.settings),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.close_settings),
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Spacer(Modifier.height(10.dp))

            SettingsSectionLabel(stringResource(R.string.settings_section_language))
            Spacer(Modifier.height(8.dp))
            LanguageSettingsCard(
                selectedLanguage = currentLanguage,
                onLanguageChange = onLanguageChange,
            )
            Spacer(Modifier.height(16.dp))

            SettingsSectionLabel(stringResource(R.string.settings_section_schedule))
            Spacer(Modifier.height(8.dp))
            SettingsActionCard(
                icon = Icons.Rounded.Schedule,
                title = stringResource(R.string.automatic_backup),
                value = "%02d:%02d".format(backupHour, backupMinute),
                onClick = { timePickerVisible = true },
            )
            Spacer(Modifier.height(16.dp))

            SettingsSectionLabel(stringResource(R.string.settings_section_backup_data))
            Spacer(Modifier.height(8.dp))
            SettingsActionCard(
                icon = Icons.Rounded.Checklist,
                title = stringResource(R.string.included_data),
                value = includedMetrics.summaryLabel(),
                onClick = { backupDataVisible = true },
            )
            Spacer(Modifier.height(16.dp))

            SettingsSectionLabel(stringResource(R.string.settings_section_google_drive))
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it.take(MAX_DRIVE_FOLDER_NAME_LENGTH) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.backup_folder_name)) },
                leadingIcon = { Icon(Icons.Rounded.Folder, contentDescription = null) },
                supportingText = if (folderName.isBlank()) {
                    { Text(stringResource(R.string.enter_folder_name)) }
                } else {
                    null
                },
                isError = folderName.isBlank(),
                singleLine = true,
                shape = RoundedCornerShape(15.dp),
            )
            Spacer(Modifier.height(16.dp))

            SettingsSectionLabel(stringResource(R.string.settings_section_file_date))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DateSystemCard(
                    title = stringResource(R.string.jalali),
                    example = DateUtils.jalaliDate(LocalDate.now(DateUtils.HEALTH_ZONE)),
                    selected = dateSystem == FileDateSystem.JALALI,
                    modifier = Modifier.weight(1f),
                    onClick = { dateSystem = FileDateSystem.JALALI },
                )
                DateSystemCard(
                    title = stringResource(R.string.gregorian),
                    example = DateUtils.gregorianDate(LocalDate.now(DateUtils.HEALTH_ZONE)),
                    selected = dateSystem == FileDateSystem.GREGORIAN,
                    modifier = Modifier.weight(1f),
                    onClick = { dateSystem = FileDateSystem.GREGORIAN },
                )
            }
            Spacer(Modifier.height(18.dp))

            Button(
                onClick = {
                    onSave(
                        BackupSettings(
                            backupHour = backupHour,
                            backupMinute = backupMinute,
                            driveFolderName = folderName,
                            fileDateSystem = dateSystem,
                            includedMetrics = includedMetrics,
                        ),
                    )
                },
                enabled = folderName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(15.dp),
            ) {
                Text(stringResource(R.string.save_settings), fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(18.dp))

            ExpandableSettingsSectionLabel(
                text = stringResource(R.string.settings_section_legal),
                supportingText = stringResource(R.string.privacy_and_terms),
                expanded = legalExpanded,
                onClick = { legalExpanded = !legalExpanded },
            )
            AnimatedVisibility(
                visible = legalExpanded,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 220),
                    expandFrom = Alignment.Top,
                ) + fadeIn(animationSpec = tween(durationMillis = 160, delayMillis = 40)),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 180),
                    shrinkTowards = Alignment.Top,
                ) + fadeOut(animationSpec = tween(durationMillis = 120)),
            ) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        SettingsLinkRow(
                            icon = Icons.Rounded.PrivacyTip,
                            title = stringResource(R.string.privacy_policy),
                            onClick = { uriHandler.openUri(PRIVACY_POLICY_URL) },
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 62.dp),
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                        SettingsLinkRow(
                            icon = Icons.Rounded.Description,
                            title = stringResource(R.string.terms_of_service),
                            onClick = { uriHandler.openUri(TERMS_OF_SERVICE_URL) },
                        )
                    }
                }
            }
        }
    }

    if (timePickerVisible) {
        val timePickerState = rememberTimePickerState(
            initialHour = backupHour,
            initialMinute = backupMinute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { timePickerVisible = false },
            title = { Text(stringResource(R.string.automatic_backup_time)) },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        backupHour = timePickerState.hour
                        backupMinute = timePickerState.minute
                        timePickerVisible = false
                    },
                ) { Text(stringResource(R.string.set_time)) }
            },
            dismissButton = {
                TextButton(onClick = { timePickerVisible = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }

    if (backupDataVisible) {
        BackupDataDialog(
            selected = includedMetrics,
            onDismiss = { backupDataVisible = false },
            onConfirm = {
                includedMetrics = it
                backupDataVisible = false
            },
        )
    }
}

@Composable
private fun BackupDataDialog(
    selected: Set<BackupMetric>,
    onDismiss: () -> Unit,
    onConfirm: (Set<BackupMetric>) -> Unit,
) {
    var draft by remember(selected) { mutableStateOf(selected) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.backup_data)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    stringResource(R.string.choose_backup_data),
                    modifier = Modifier.padding(bottom = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                BackupMetric.entries.forEach { metric ->
                    val checked = metric in draft
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                draft = if (checked) draft - metric else draft + metric
                            }
                            .padding(horizontal = 4.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = {
                                draft = if (checked) draft - metric else draft + metric
                            },
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(metric.localizedTitle(), fontWeight = FontWeight.SemiBold)
                            Text(
                                metric.localizedDescription(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(draft) }) { Text(stringResource(R.string.done)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
private fun BackupMetric.localizedTitle(): String = stringResource(
    when (this) {
        BackupMetric.STEPS -> R.string.metric_steps
        BackupMetric.WEIGHT -> R.string.metric_weight
        BackupMetric.ACTIVITY -> R.string.metric_activity
        BackupMetric.HEART -> R.string.metric_heart
        BackupMetric.SLEEP -> R.string.metric_sleep
        BackupMetric.SPO2 -> R.string.metric_spo2
    },
)

@Composable
private fun BackupMetric.localizedDescription(): String = stringResource(
    when (this) {
        BackupMetric.STEPS -> R.string.metric_steps_description
        BackupMetric.WEIGHT -> R.string.metric_weight_description
        BackupMetric.ACTIVITY -> R.string.metric_activity_description
        BackupMetric.HEART -> R.string.metric_heart_description
        BackupMetric.SLEEP -> R.string.metric_sleep_description
        BackupMetric.SPO2 -> R.string.metric_spo2_description
    },
)

@Composable
private fun Set<BackupMetric>.summaryLabel(): String = when (size) {
    BackupMetric.entries.size -> stringResource(R.string.all_data)
    0 -> stringResource(R.string.no_data)
    else -> stringResource(R.string.selected_data_count, size, BackupMetric.entries.size)
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.1.sp,
    )
}

@Composable
private fun ExpandableSettingsSectionLabel(
    text: String,
    supportingText: String,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "legal-section-chevron",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(start = 10.dp, end = 14.dp, top = 9.dp, bottom = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsSectionLabel(text)
        Spacer(Modifier.weight(1f))
        Text(
            text = supportingText,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Rounded.ExpandMore,
            contentDescription = stringResource(
                if (expanded) R.string.collapse_legal_links else R.string.expand_legal_links,
            ),
            modifier = Modifier
                .size(20.dp)
                .rotate(chevronRotation),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LanguageSettingsCard(
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(R.string.app_language),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LanguageSettingOption(
                    text = stringResource(R.string.language_english_native),
                    selected = selectedLanguage != LANGUAGE_PERSIAN,
                    modifier = Modifier.weight(1f),
                    onClick = { onLanguageChange(LANGUAGE_ENGLISH) },
                )
                LanguageSettingOption(
                    text = stringResource(R.string.language_persian_native),
                    selected = selectedLanguage == LANGUAGE_PERSIAN,
                    modifier = Modifier.weight(1f),
                    onClick = { onLanguageChange(LANGUAGE_PERSIAN) },
                )
            }
        }
    }
}

@Composable
private fun LanguageSettingOption(
    text: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selected) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(17.dp),
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(text, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun SettingsActionCard(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun SettingsLinkRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(19.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
        Icon(
            Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DateSystemCard(
    title: String,
    example: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            },
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(19.dp),
                )
                Spacer(Modifier.weight(1f))
                RadioButton(selected = selected, onClick = onClick, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(7.dp))
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(example, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AppHeader(onSettingsClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Icon(
                Icons.Rounded.Sync,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(5.dp)
                    .size(14.dp)
                    .rotate(90f),
            )
        }
        Spacer(Modifier.width(15.dp))
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.4).sp,
            )
            Text(
                stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
            )
        }
        IconButton(onClick = onSettingsClick) {
            Icon(
                Icons.Rounded.Settings,
                contentDescription = stringResource(R.string.settings),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ConnectionsCard(
    healthState: ConnectionState,
    driveState: ConnectionState,
    driveFolderName: String,
    selectedDate: LocalDate,
    dateSelectionEnabled: Boolean,
    onHealthConnect: () -> Unit,
    onDriveConnect: () -> Unit,
    onOpenHealthConnect: () -> Unit,
    onOpenGoogleDrive: () -> Unit,
    onDateChange: (LocalDate) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(Modifier.padding(vertical = 8.dp)) {
            ConnectionRow(
                icon = Icons.Rounded.HealthAndSafety,
                title = stringResource(R.string.health_connect),
                subtitle = stringResource(R.string.read_only_summaries),
                state = healthState,
                onClick = if (healthState == ConnectionState.CONNECTED) {
                    onOpenHealthConnect
                } else {
                    onHealthConnect
                },
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 18.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
            )
            ConnectionRow(
                icon = Icons.Rounded.Cloud,
                title = stringResource(R.string.google_drive),
                subtitle = driveFolderName,
                state = driveState,
                onClick = if (driveState == ConnectionState.CONNECTED) {
                    onOpenGoogleDrive
                } else {
                    onDriveConnect
                },
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 18.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
            )
            ReportDateSelector(
                selectedDate = selectedDate,
                enabled = dateSelectionEnabled,
                onDateChange = onDateChange,
            )
        }
    }
}

@Composable
private fun ConnectionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    state: ConnectionState,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        ConnectionStatus(state = state)
    }
}

@Composable
private fun ConnectionStatus(state: ConnectionState) {
    when (state) {
        ConnectionState.CHECKING -> CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
        ConnectionState.CONNECTED -> Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF20A67A)),
            )
            Spacer(Modifier.width(7.dp))
            Text(
                stringResource(R.string.connected),
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF16805F),
            )
        }
        ConnectionState.ACTION_REQUIRED,
        ConnectionState.UNAVAILABLE,
        -> Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(
                    if (state == ConnectionState.UNAVAILABLE) R.string.install_action else R.string.connect_action,
                ),
            )
            Spacer(Modifier.width(3.dp))
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun ScheduleCard(state: MainUiState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(19.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.next_backup),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                )
                Text(
                    formatScheduled(state.nextBackupEpochMillis),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    stringResource(R.string.last_backup),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                )
                Text(
                    state.appState.lastSuccessfulBackupEpochMillis?.let { formatLastBackup(it) }
                        ?: stringResource(R.string.not_yet),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportDateSelector(
    selectedDate: LocalDate,
    enabled: Boolean,
    onDateChange: (LocalDate) -> Unit,
) {
    var datePickerVisible by remember { mutableStateOf(false) }
    val today = LocalDate.now(DateUtils.HEALTH_ZONE)
    val locale = LocalConfiguration.current.locales[0]
    val dateFormatter = remember(locale) { DateTimeFormatter.ofPattern("MMM d, yyyy", locale) }
    val formattedDate = selectedDate.format(dateFormatter)
    val selectedDateText = when (selectedDate) {
        today -> stringResource(R.string.date_with_relative_day, stringResource(R.string.today), formattedDate)
        today.minusDays(1) -> stringResource(
            R.string.date_with_relative_day,
            stringResource(R.string.yesterday),
            formattedDate,
        )
        else -> formattedDate
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { datePickerVisible = true }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.report_date),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                selectedDateText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            stringResource(R.string.change_action),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }

    if (datePickerVisible) {
        val todayUtcMillis = today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli(),
            selectableDates = remember(today) {
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                        utcTimeMillis <= todayUtcMillis

                    override fun isSelectableYear(year: Int): Boolean = year <= today.year
                }
            },
        )
        DatePickerDialog(
            onDismissRequest = { datePickerVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            onDateChange(
                                Instant.ofEpochMilli(millis)
                                    .atZone(ZoneOffset.UTC)
                                    .toLocalDate(),
                            )
                        }
                        datePickerVisible = false
                    },
                    enabled = pickerState.selectedDateMillis != null,
                ) { Text(stringResource(R.string.use_date)) }
            },
            dismissButton = {
                TextButton(onClick = { datePickerVisible = false }) { Text(stringResource(R.string.cancel)) }
            },
        ) {
            DatePicker(
                state = pickerState,
                title = {
                    Text(
                        stringResource(R.string.choose_report_date),
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                headline = null,
                showModeToggle = false,
            )
        }
    }
}

@Composable
private fun BackupAction(
    isBackingUp: Boolean,
    status: UiText?,
    enabled: Boolean,
    onBackupNow: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            onClick = onBackupNow,
            enabled = enabled && !isBackingUp,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(17.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        ) {
            AnimatedContent(isBackingUp, label = "backup-button") { loading ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(21.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(if (loading) R.string.backing_up else R.string.back_up_now),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        AnimatedVisibility(status != null, enter = fadeIn(), exit = fadeOut()) {
            Text(
                status?.asString().orEmpty(),
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (!enabled && !isBackingUp) {
            Text(
                stringResource(R.string.connect_services_hint),
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RecentActivitySection(entries: List<ActivityEntry>) {
    val listState = rememberLazyListState()
    val newestEntryId = entries.firstOrNull()?.id

    LaunchedEffect(newestEntryId) {
        if (newestEntryId != null) listState.animateScrollToItem(0)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(21.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                stringResource(R.string.recent_activity),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(R.string.latest_count, entries.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = RoundedCornerShape(22.dp),
        ) {
            if (entries.isEmpty()) {
                EmptyActivity()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(entries, key = { it.id }) { entry ->
                        ActivityRow(entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyActivity() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Rounded.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.empty_activity), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ActivityRow(entry: ActivityEntry) {
    val (icon, tint) = when (entry.severity) {
        ActivitySeverity.SUCCESS -> Icons.Rounded.Check to Color(0xFF16805F)
        ActivitySeverity.WARNING -> Icons.Rounded.WarningAmber to Color(0xFFA15C00)
        ActivitySeverity.ERROR -> Icons.Rounded.ErrorOutline to MaterialTheme.colorScheme.error
        ActivitySeverity.INFO -> Icons.Rounded.Info to MaterialTheme.colorScheme.tertiary
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier
                    .size(18.dp)
                    .offset(
                        y = if (
                            entry.severity == ActivitySeverity.ERROR ||
                            entry.severity == ActivitySeverity.WARNING
                        ) {
                            (-1).dp
                        } else {
                            0.dp
                        },
                    ),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth()) {
                Text(
                    localizedActivityText(entry.title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    Instant.ofEpochMilli(entry.timestampEpochMillis)
                        .atZone(DateUtils.HEALTH_ZONE)
                        .format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            entry.detail?.let { detail ->
                Text(
                    localizedActivityText(detail),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun localizedActivityText(text: String): String {
    val directResource = when (text) {
        "Local state was repaired" -> R.string.activity_local_state_repaired
        "Backup history was unreadable; recent days will be checked again" ->
            R.string.activity_local_state_repaired_detail
        "Health Connect connected" -> R.string.activity_health_connected
        "Health permission missing" -> R.string.activity_health_permission_missing
        "Backups require all requested read permissions" -> R.string.activity_health_permission_missing_detail
        "Setup completed" -> R.string.activity_setup_completed
        "Automatic backups are ready" -> R.string.activity_backups_ready
        "Google Drive connected" -> R.string.activity_drive_connected
        "Manual backup started" -> R.string.activity_manual_started
        "Scheduled backup started" -> R.string.activity_scheduled_started
        "Backup completed" -> R.string.activity_backup_completed
        "Backup failed" -> R.string.activity_backup_failed
        "Backup attempt failed" -> R.string.activity_backup_attempt_failed
        "Missing backup found" -> R.string.activity_missing_found
        "Missing backup recovered" -> R.string.activity_missing_recovered
        "Reading Health Connect" -> R.string.activity_reading_health
        "Uploading to Google Drive" -> R.string.activity_uploading_drive
        "Backup needs attention" -> R.string.activity_backup_attention
        "Retrying backup" -> R.string.activity_retrying
        "Retry scheduled" -> R.string.activity_retry_scheduled
        "Google Drive upload failed after 5 retries" -> R.string.activity_failed_after_retries
        "Health Connect permission required" -> R.string.error_health_permission_required
        "Google Drive authorization required" -> R.string.error_drive_authorization_required
        "Required access was revoked" -> R.string.error_access_revoked
        "Could not create the daily JSON" -> R.string.error_json_creation
        "Network or Google Drive request failed" -> R.string.error_network_drive
        "Health backup could not be completed" -> R.string.error_backup_failed
        else -> null
    }
    if (directResource != null) return stringResource(directResource)

    ATTEMPT_PATTERN.matchEntire(text)?.let { match ->
        return stringResource(
            R.string.activity_attempt_count,
            match.groupValues[1].toInt(),
            match.groupValues[2].toInt(),
        )
    }
    RETRY_PATTERN.matchEntire(text)?.let { match ->
        return stringResource(
            R.string.activity_retry_detail,
            match.groupValues[1].toInt(),
            match.groupValues[2].toInt(),
        )
    }
    return text
}

private val ATTEMPT_PATTERN = Regex("Attempt (\\d+) of (\\d+)")
private val RETRY_PATTERN = Regex("Attempt (\\d+) of (\\d+) in about 3 minutes")

@Composable
private fun formatScheduled(epochMillis: Long): String {
    val scheduled = Instant.ofEpochMilli(epochMillis).atZone(DateUtils.HEALTH_ZONE)
    val today = LocalDate.now(DateUtils.HEALTH_ZONE)
    val locale = LocalConfiguration.current.locales[0]
    val day = when (scheduled.toLocalDate()) {
        today -> stringResource(R.string.today)
        today.plusDays(1) -> stringResource(R.string.tomorrow)
        else -> scheduled.format(DateTimeFormatter.ofPattern("MMM d", locale))
    }
    return stringResource(R.string.day_and_time, day, scheduled.format(DateTimeFormatter.ofPattern("HH:mm", locale)))
}

@Composable
private fun formatLastBackup(epochMillis: Long): String {
    val backup = Instant.ofEpochMilli(epochMillis).atZone(DateUtils.HEALTH_ZONE)
    val today = LocalDate.now(DateUtils.HEALTH_ZONE)
    val locale = LocalConfiguration.current.locales[0]
    val day = when (backup.toLocalDate()) {
        today -> stringResource(R.string.today)
        today.minusDays(1) -> stringResource(R.string.yesterday)
        else -> backup.format(DateTimeFormatter.ofPattern("MMM d", locale))
    }
    return stringResource(R.string.day_and_time, day, backup.format(DateTimeFormatter.ofPattern("HH:mm", locale)))
}
