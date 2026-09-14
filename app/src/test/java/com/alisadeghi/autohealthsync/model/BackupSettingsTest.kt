package com.alisadeghi.autohealthsync.model

import org.junit.Assert.assertEquals
import org.junit.Test

class BackupSettingsTest {
    @Test
    fun `defaults preserve the existing backup behavior`() {
        val settings = BackupSettings()

        assertEquals(23, settings.backupHour)
        assertEquals(0, settings.backupMinute)
        assertEquals("Auto: Health Data", settings.driveFolderName)
        assertEquals(null, settings.fileDateSystem)
        assertEquals(BackupMetric.entries.toSet(), settings.includedMetrics)
    }

    @Test
    fun `file date follows app language until explicitly chosen`() {
        val automatic = AppState()
        assertEquals(FileDateSystem.GREGORIAN, automatic.resolvedFileDateSystem("en"))
        assertEquals(FileDateSystem.JALALI, automatic.resolvedFileDateSystem("fa"))
        assertEquals(
            FileDateSystem.JALALI,
            automatic.copy(languageTag = "fa").resolvedFileDateSystem("en"),
        )

        val explicit = automatic.copy(
            languageTag = "en",
            backupSettings = BackupSettings(fileDateSystem = FileDateSystem.JALALI),
        )
        assertEquals(FileDateSystem.JALALI, explicit.resolvedFileDateSystem("en"))
    }

    @Test
    fun `normalization trims folder and protects time ranges`() {
        val settings = BackupSettings(
            backupHour = 25,
            backupMinute = -2,
            driveFolderName = "  My Health  ",
        ).normalized()

        assertEquals(23, settings.backupHour)
        assertEquals(0, settings.backupMinute)
        assertEquals("My Health", settings.driveFolderName)
    }

    @Test
    fun `normalization preserves selected backup metrics`() {
        val selected = setOf(BackupMetric.STEPS, BackupMetric.SLEEP)

        assertEquals(
            selected,
            BackupSettings(includedMetrics = selected).normalized().includedMetrics,
        )
    }
}
