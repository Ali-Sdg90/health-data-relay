# Contributing

Contributions are welcome when they keep Health Data Relay small, reliable, and privacy-focused.

1. Search existing issues before starting substantial work.
2. Create a focused branch and keep changes scoped.
3. Add or update tests for behavioral changes.
4. Run:

   ```shell
   ./gradlew testDebugUnitTest lintDebug assembleDebug
   ```

5. Open a pull request with a Conventional Commit title, such as `fix(backup): handle expired authorization` or `feat(settings): select backup fields`.

Test Preview is disabled by default. When UI work needs access-free setup previewing, enable it for that debug build only:

```shell
./gradlew assembleDebug -PSHOW_TEST_PREVIEW=true
```

The preview must remain unavailable in Release builds and must not grant access, persist onboarding completion, schedule work, or run backups.

Do not include credentials, signing material, access tokens, or personal health data. Product behavior should remain consistent with [vision.md](vision.md).
