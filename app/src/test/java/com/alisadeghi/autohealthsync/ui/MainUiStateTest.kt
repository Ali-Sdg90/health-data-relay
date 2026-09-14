package com.alisadeghi.autohealthsync.ui

import com.alisadeghi.autohealthsync.model.AppState
import com.alisadeghi.autohealthsync.model.ConnectionState
import com.alisadeghi.autohealthsync.system.BackgroundAccessStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainUiStateTest {
    @Test
    fun `OEM auto start confirmation is required before setup can finish`() {
        val state = readyState(
            appState = AppState(onboardingCompleted = false, autoStartConfirmed = false),
        )

        assertFalse(state.requiredSetupComplete)
        assertTrue(state.showOnboarding)
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
    fun `completed onboarding stays dismissed when a required access changes`() {
        val state = readyState(
            appState = AppState(onboardingCompleted = true, autoStartConfirmed = true),
            background = BackgroundAccessStatus(
                batteryOptimizationDisabled = false,
                backgroundRestricted = false,
                autoStartSettingsAvailable = true,
            ),
        )

        assertFalse(state.requiredSetupComplete)
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
        driveState: ConnectionState = ConnectionState.CONNECTED,
        background: BackgroundAccessStatus = BackgroundAccessStatus(
            batteryOptimizationDisabled = true,
            backgroundRestricted = false,
            autoStartSettingsAvailable = true,
        ),
    ) = MainUiState(
        appState = appState,
        isAppStateLoaded = true,
        healthState = ConnectionState.CONNECTED,
        driveState = driveState,
        notificationGranted = true,
        backgroundAccess = background,
    )
}
