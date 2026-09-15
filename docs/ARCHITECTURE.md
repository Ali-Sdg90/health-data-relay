# Architecture

Health Data Relay is an Android app that creates one health summary per day and stores it in Google Drive.

## Data flow

```text
Health Connect → HealthConnectManager → DailyHealthSummary
               → BackupCoordinator → DriveBackupManager → Google Drive
```

## Main components

| Component | Responsibility |
| --- | --- |
| `MainScreen` / `MainViewModel` | Gated onboarding, UI state, settings, and manual backups |
| `BackgroundAccessManager` | Battery-optimization status and supported OEM Auto Start settings |
| `HealthConnectManager` | Permissions, record reads, and daily aggregation |
| `BackupCoordinator` | Validation, selected-metric serialization, recovery, and backup orchestration |
| `DriveBackupManager` | Folder discovery and daily file upload/update |
| `AppStateStore` | Persistent settings, backup state, and recent activity |
| `BackupScheduler` / `BackupWorker` | Daily WorkManager execution |

The app has no backend, analytics, or user database. It requests read-only Health Connect access and the narrow Google Drive `drive.file` scope. Health Connect and Google Drive are the only required setup items. Notifications, battery access, and supported OEM Auto Start controls are optional. Automatic work is scheduled only after the required connections are ready. Scheduling and daily boundaries use `Asia/Tehran`.

## Setup lifecycle

The user chooses English or Persian during guided setup. Settings can reopen the setup flow without clearing saved backup preferences or the persisted completion state; completing the required connection checks returns to the main screen.

The non-destructive Test Preview is compiled into debug builds but hidden by default through `SHOW_TEST_PREVIEW=false`. A developer can enable it with the Gradle property `-PSHOW_TEST_PREVIEW=true`. The additional `BuildConfig.DEBUG` guard prevents it from appearing in Release builds, and preview mode never grants access, persists setup completion, schedules work, or runs a real backup.
