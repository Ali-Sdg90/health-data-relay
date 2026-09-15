package com.alisadeghi.autohealthsync.ui

import com.alisadeghi.autohealthsync.model.AppState
import com.alisadeghi.autohealthsync.model.ConnectionState
import com.alisadeghi.autohealthsync.system.BackgroundAccessStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainUiStateTest {
    @Test
    fun `battery and OEM auto start are optional during setup`() {
        val state = readyState(
            appState = AppState(onboardingCompleted = false, autoStartConfirmed = false),
            background = BackgroundAccessStatus(
                batteryOptimizationDisabled = false,
                backgroundRestricted = true,
                autoStartSettingsAvailable = true,
            ),
        )

        assertTrue(state.requiredSetupComplete)
        assertTrue(state.showOnboarding)
    }

    @Test
    fun `setup still requires Health Connect and Drive`() {
        val missingHealth = readyState(
            appState = AppState(onboardingCompleted = false),
            healthState = ConnectionState.ACTION_REQUIRED,
        )
        val missingDrive = readyState(
            appState = AppState(onboardingCompleted = false),
            driveState = ConnectionState.ACTION_REQUIRED,
        )

        assertFalse(missingHealth.requiredSetupComplete)
        assertFalse(missingDrive.requiredSetupComplete)
    }

    @Test
    fun `completed setup opens the main screen`() {
        val state = readyState(
            appState = AppState(onboardingCompleted = true, autoStartConfirmed = true),
        )

        assertTrue(state.requiredSetupComplete)
        assertFalse(state.showOnboarding)
    }

    @Test
    fun `optional background access changes do not affect completed setup`() {
        val state = readyState(
            appState = AppState(onboardingCompleted = true, autoStartConfirmed = true),
            background = BackgroundAccessStatus(
                batteryOptimizationDisabled = false,
                backgroundRestricted = false,
                autoStartSettingsAvailable = true,
            ),
        )

        assertTrue(state.requiredSetupComplete)
        assertFalse(state.showOnboarding)
    }

    @Test
    fun `completed onboarding stays dismissed while Drive is temporarily unavailable`() {
        val state = readyState(
            appState = AppState(onboardingCompleted = true, autoStartConfirmed = true),
            driveState = ConnectionState.ACTION_REQUIRED,
        )

        assertFalse(state.requiredSetupComplete)
        assertFalse(state.showOnboarding)
    }

    @Test
    fun `loading persisted state does not flash onboarding`() {
        val state = MainUiState(isAppStateLoaded = false, notificationGranted = true)

        assertFalse(state.showOnboarding)
    }

    @Test
    fun `setup review reopens onboarding without clearing completion`() {
        val state = readyState(
            appState = AppState(onboardingCompleted = true),
            setupReviewActive = true,
        )

        assertTrue(state.appState.onboardingCompleted)
        assertTrue(state.showOnboarding)
    }

    @Test
    fun `debug preview can pass setup without marking onboarding complete`() {
        val preview = MainUiState(
            isAppStateLoaded = true,
            healthState = ConnectionState.ACTION_REQUIRED,
            driveState = ConnectionState.ACTION_REQUIRED,
            testModeEnabled = true,
            testPreviewActive = true,
        )

        assertTrue(preview.requiredSetupComplete)
        assertFalse(preview.showOnboarding)
        assertFalse(preview.appState.onboardingCompleted)
    }

    private fun readyState(
        appState: AppState,
        healthState: ConnectionState = ConnectionState.CONNECTED,
        driveState: ConnectionState = ConnectionState.CONNECTED,
        setupReviewActive: Boolean = false,
        background: BackgroundAccessStatus = BackgroundAccessStatus(
            batteryOptimizationDisabled = true,
            backgroundRestricted = false,
            autoStartSettingsAvailable = true,
        ),
    ) = MainUiState(
        appState = appState,
        isAppStateLoaded = true,
        healthState = healthState,
        driveState = driveState,
        notificationGranted = true,
        backgroundAccess = background,
        setupReviewActive = setupReviewActive,
    )
}
