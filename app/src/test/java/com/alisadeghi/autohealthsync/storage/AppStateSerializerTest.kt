package com.alisadeghi.autohealthsync.storage

import com.alisadeghi.autohealthsync.model.FileDateSystem
import java.io.ByteArrayInputStream
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppStateSerializerTest {
    @Test
    fun `legacy implicit Jalali becomes language based without reusing old file ids`() = runBlocking {
        val oldState = """
            {
              "backupSettings": {"fileDateSystem": "JALALI"},
              "successfulDates": ["2026-09-14"],
              "driveFileIds": {"2026-09-14": "old-file"}
            }
        """.trimIndent()

        val migrated = AppStateSerializer.readFrom(ByteArrayInputStream(oldState.toByteArray()))

        assertEquals(null, migrated.backupSettings.fileDateSystem)
        assertTrue(migrated.successfulDates.isEmpty())
        assertTrue(migrated.driveFileIds.isEmpty())
    }

    @Test
    fun `legacy Gregorian selection remains explicit`() = runBlocking {
        val oldState = """{"backupSettings":{"fileDateSystem":"GREGORIAN"}}"""

        val migrated = AppStateSerializer.readFrom(ByteArrayInputStream(oldState.toByteArray()))

        assertEquals(FileDateSystem.GREGORIAN, migrated.backupSettings.fileDateSystem)
    }
}
