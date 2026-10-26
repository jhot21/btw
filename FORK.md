# Fork notes: jhot21/btw

This is a maintained fork of [bitwarden/android](https://github.com/bitwarden/android) that adds a
**Passgen** generator type: deterministic passwords from passgen v2/v3
([github.com/jhot21/passgen](https://github.com/jhot21/passgen)).

## Rules

- New code lives in new files (`data/tools/passgen/`, `ui/tools/feature/generator/*Passgen*`,
  `ui/tools/feature/generator/passgen/`, `res/values/strings_passgen.xml`,
  `ui/platform/util/PassgenShortcutUtils.kt` and its test, and
  `data/platform/manager/model/PassgenShortcut.kt`). The latter must live in `SpecialCircumstance`'s
  package because sealed subclasses must share the package; conflict resolvers must not move it.
- Every edit to an upstream file carries a marker and is listed under **Hooks** below: `// PASSGEN:` in Kotlin, or a standalone `<!-- PASSGEN: ... -->` comment line in XML.
- Upstream-sync PRs **must be merged with a merge commit** (never squash or rebase), otherwise every
  later sync re-conflicts on the same hunks.
- Upstream's own workflows are disabled in this repo's Actions settings (they need Bitwarden secrets
  or the standard flavor's google-services.json). Don't edit them. Only these stay enabled:
  `fork-checks.yml` (hooks and passgen tests), `fork-tests.yml` (detekt, lint and the full unit
  suites on the F-Droid flavor, replacing upstream's `test.yml`), `upstream-sync.yml`, and the
  reusable `_version.yml`. After a sync adds a new upstream workflow, disable it with
  `gh workflow disable <file> -R jhot21/btw`.
- `fork-checks.yml`'s `passgen-tests` job needs `read:packages` to pull `bitwarden/sdk` from
  `maven.pkg.github.com` (see `settings.gradle.kts`); it uses the default `GITHUB_TOKEN`. If that
  token ever gets a 401 for bitwarden's package, store a classic PAT with the `read:packages` scope
  as a repo secret (e.g. `PACKAGES_TOKEN`) and reference it in that job's `GITHUB_TOKEN` env instead.
- The passgen salt is stored only on the device. Back it up (Generator → Passgen → copy salt):
  clearing app data without a backup makes passwords unrecoverable.
- On API < 30 MainActivity is singleTask and relies on onNewIntent; the navbar's init-time shortcut
  check doesn't re-fire for an already-open app — same limitation as the built-in generator shortcut.
- The Passgen launch request is an in-memory one-shot flag on PassgenRepository; if the vault locks or the account switches between the navbar consuming the shortcut and the Generator's first resume, the flag survives until the next Default-mode Generator resume, which then opens on Passgen. Harmless (only the tab choice), accepted.

## Syncing with upstream

`.github/workflows/upstream-sync.yml` runs every Monday 06:00 UTC (or manually). It opens a PR from
`upstream-sync/YYYY-MM-DD`. On conflicts, it commits the conflict markers and opens a **draft** PR
labelled `merge-conflicts`. `fork-checks.yml` fails on markers, so it can't be merged by accident.

The workflow authenticates as a fine-grained PAT in the `SYNC_TOKEN` secret, which needs these
scopes on `jhot21/btw`:
- **Contents: write** — checkout with push rights, push the sync branch.
- **Pull requests: write** — open/list the sync PR.
- **Issues: write** — create the `merge-conflicts` label (repository labels are governed by the
  Issues permission for fine-grained PATs, not Pull requests).
- **Workflows: write** — pushes that modify `.github/workflows/*` are rejected otherwise.

Resolving a sync PR (human or agent):
1. `git fetch origin && git checkout upstream-sync/<date>`
2. Resolve every conflict; keep upstream's change **and** re-apply each `// PASSGEN:` hook.
   Delete/modify and binary conflicts leave no `<<<<<<<` markers in the file — check the PR's
   "Conflicts" list and verify each such file by hand.
3. `scripts/check-fork-hooks.sh`
4. `./gradlew :app:testFdroidDebugUnitTest --tests 'com.x8bit.bitwarden.data.tools.passgen.*' --tests 'com.x8bit.bitwarden.ui.tools.feature.generator.*' --tests 'com.x8bit.bitwarden.ui.platform.util.PassgenShortcutUtilsTest' --tests 'com.x8bit.bitwarden.MainViewModelTest' --tests 'com.x8bit.bitwarden.ui.platform.feature.rootnav.RootNavViewModelTest' --tests 'com.x8bit.bitwarden.ui.platform.feature.vaultunlockednavbar.VaultUnlockedNavBarViewModelTest'`
5. `./gradlew :app:assembleFdroidDebug`
6. Push, mark ready for review, merge with a **merge commit**.

## Hooks

Format: `` - `<file>` :: `<text on the marker line>` `` where the marker is `// PASSGEN:` in Kotlin or a standalone `<!-- PASSGEN: ... -->` comment line in XML.

- `app/src/main/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorScreen.kt` :: `import com.x8bit.bitwarden.ui.tools.feature.generator.passgen.PassgenContent`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorScreen.kt` :: `onPassgenAction = { viewModel.trySendAction(it) }`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorScreen.kt` :: `@Suppress("LongMethod", "CyclomaticComplexMethod")`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorScreen.kt` :: `onPassgenAction: (PassgenAction) -> Unit`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorScreen.kt` :: `never shown in modal mode`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorScreen.kt` :: `is PassgenMainType -> item {`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorScreen.kt` :: `@Suppress("MaxLineLength", "LongMethod", "CyclomaticComplexMethod")`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorScreen.kt` :: `GeneratorState.MainTypeOption.PASSGEN -> passcodePolicyOverride == null`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorViewModel.kt` :: `fork-only import`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorViewModel.kt` :: `private val passgenRepository: PassgenRepository,`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorViewModel.kt` :: `is PassgenAction -> handlePassgenAction(action)`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorViewModel.kt` :: `is PassgenMainType,`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorViewModel.kt` :: `is PassgenMainType -> updateGeneratorMainType { it }`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorViewModel.kt` :: `is PassgenMainType -> Unit`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorViewModel.kt` :: `recordPassgenCopy() // PASSGEN:`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorViewModel.kt` :: `GeneratorState.MainTypeOption.PASSGEN -> loadPassgenOptions()`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorViewModel.kt` :: `GeneratorState.MainTypeOption.PASSGEN, // PASSGEN: create-login`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorViewModel.kt` :: `is PassgenMainType -> // PASSGEN:`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorViewModel.kt` :: `region — fork-only passgen handling`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorViewModel.kt` :: `PASSGEN: endregion`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorViewModel.kt` :: `GeneratorMode.Modal.Password -> MainTypeOption`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorViewModel.kt` :: `        // PASSGEN:`
- `app/src/test/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorScreenTest.kt` :: `fork-only tests`
- `app/src/test/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorViewModelTest.kt` :: `fork-only imports`
- `app/src/test/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorViewModelTest.kt` :: `private val passgenRepository: PassgenRepository = mockk(relaxed = true) {`
- `app/src/test/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorViewModelTest.kt` :: `fork-only tests`
- `app/src/test/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorViewModelTest.kt` :: `passgenRepository = passgenRepository,`
- `app/src/main/kotlin/com/x8bit/bitwarden/MainViewModel.kt` :: `isPassgenShortcut(intent) -> { // PASSGEN:`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/platform/feature/rootnav/RootNavViewModel.kt` :: `                    com.x8bit.bitwarden.data.platform.manager.model.PassgenShortcut, // PASSGEN:`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/platform/feature/rootnav/RootNavViewModel.kt` :: `is com.x8bit.bitwarden.data.platform.manager.model.PassgenShortcut, // PASSGEN:`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/platform/feature/vaultunlockednavbar/VaultUnlockedNavBarViewModel.kt` :: `passgenRepository: // PASSGEN:`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/platform/feature/vaultunlockednavbar/VaultUnlockedNavBarViewModel.kt` :: `model.PassgenShortcut -> { // PASSGEN:`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorViewModel.kt` :: `handlePassgenLaunchRequest()) return // PASSGEN:`
- `app/src/main/res/xml/shortcuts.xml` :: `PASSGEN: Passgen shortcut`
- `app/src/beta/res/xml/shortcuts.xml` :: `PASSGEN: Passgen shortcut`
- `app/src/release/res/xml/shortcuts.xml` :: `PASSGEN: Passgen shortcut`
- `app/build.gradle.kts` :: `applicationId = "me.jhot.btw"`
- `app/src/main/res/xml/shortcuts.xml` :: `android:targetPackage="me.jhot.btw.dev"`
- `app/src/beta/res/xml/shortcuts.xml` :: `android:targetPackage="me.jhot.btw.beta"`
- `app/src/release/res/xml/shortcuts.xml` :: `android:targetPackage="me.jhot.btw"`
- `app/src/main/kotlin/com/x8bit/bitwarden/data/autofill/accessibility/util/AccessibilityNodeInfoExtensions.kt` :: `startsWith(prefix = "me.jhot.btw")`
- `app/src/test/kotlin/com/x8bit/bitwarden/MainViewModelTest.kt` :: `PASSGEN: fork-only test`
- `app/src/test/kotlin/com/x8bit/bitwarden/MainViewModelTest.kt` :: `mockDataString: String? = null, // PASSGEN:`
- `app/src/test/kotlin/com/x8bit/bitwarden/MainViewModelTest.kt` :: `every { dataString } returns mockDataString // PASSGEN:`
- `app/src/test/kotlin/com/x8bit/bitwarden/ui/platform/feature/rootnav/RootNavViewModelTest.kt` :: `PASSGEN: fork-only test`
- `app/src/test/kotlin/com/x8bit/bitwarden/ui/platform/feature/vaultunlockednavbar/VaultUnlockedNavBarViewModelTest.kt` :: `passgenRepository: // PASSGEN:`
- `app/src/test/kotlin/com/x8bit/bitwarden/ui/platform/feature/vaultunlockednavbar/VaultUnlockedNavBarViewModelTest.kt` :: `PASSGEN: fork-only test`
- `app/src/test/kotlin/com/x8bit/bitwarden/ui/platform/feature/vaultunlockednavbar/VaultUnlockedNavBarViewModelTest.kt` :: `passgenRepository = passgenRepository, // PASSGEN:`
- `app/src/test/kotlin/com/x8bit/bitwarden/ui/tools/feature/generator/GeneratorViewModelTest.kt` :: `PASSGEN: fork-only launch-request tests`

Note: `GeneratorViewModel.kt`'s `loadOptions` hook (line 387 as of this writing) reads
`is PassgenMainType -> updateGeneratorMainType { it }` — it re-derives the main type on
process-death restore instead of being a no-op. The other `is PassgenMainType -> Unit` entry
(line 475, in `loadPasscodeOptions`) is unrelated and stays a no-op: that function only ever
handles the two passcode types, so `PassgenMainType` never reaches it via `loadOptions`'s
dispatch. The `PASSGEN(labelRes = ...)` enum entry's marker sits on its own comment
line (`        // PASSGEN:`) above the wrapped `PASSGEN(` statement, so its anchor is that comment
line's exact indentation rather than the `PASSGEN(` text itself.

## App identity

The applicationId is `me.jhot.btw` (set in `app/build.gradle.kts`); the debug and beta variants
derive `me.jhot.btw.dev` and `me.jhot.btw.beta` from their `applicationIdSuffix`. The Kotlin
`namespace`/package intentionally stays `com.x8bit.bitwarden` (no package rename, to keep upstream
merges clean). The accessibility service's `shouldSkipPackage` also skips `me.jhot.btw*` so autofill
ignores the app's own screens.

After an upstream sync, verify that every `android:targetPackage` in the three `shortcuts.xml` files
(`main`, `beta`, `release`) still equals its variant's ID (`me.jhot.btw.dev`, `me.jhot.btw.beta`,
`me.jhot.btw`); `scripts/check-fork-hooks.sh` verifies the marked lines are still present.

The standard flavor's debug build needs google-services.json for the app's package; this fork's tracked file only knows com.x8bit.bitwarden.dev, so the standard flavor is not built here — the fork builds and tests the fdroid flavor.

Because the ID differs from upstream, the official Bitwarden app can be installed alongside this one.

## Building a signed APK

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
  ./gradlew :app:assembleFdroidRelease
```

Use the F-Droid flavor: it needs no `google-services.json`. The standard flavor's release build does,
and this fork doesn't ship one. Output: `app/build/outputs/apk/fdroid/release/me.jhot.btw-fdroid.apk`.
Gradle does not sign release builds here, so that APK is unsigned. Sign it with the build-tools from
`$ANDROID_HOME` (or `~/Library/Android/sdk`):

```bash
BT="${ANDROID_HOME:-$HOME/Library/Android/sdk}/build-tools/<version>"
APK=app/build/outputs/apk/fdroid/release/me.jhot.btw-fdroid.apk
"$BT/zipalign" -p -f 4 "$APK" aligned.apk
"$BT/apksigner" sign --ks <keystore> --out btw-release.apk aligned.apk
"$BT/apksigner" verify --verbose btw-release.apk
```

`apksigner sign` prompts for the keystore password; never put the password on the command line.
Add `--ks-key-alias <alias>` only if the keystore holds several keys.

Keep the keystore outside the repository (e.g. `~/.android-signing/`) and never commit it. Back it
up somewhere safe: if it is lost, installed copies cannot be updated in place (a new key means
uninstalling first, losing local data).

## Releasing

Releases are published only as GitHub releases on this fork, built and signed by
`.github/workflows/fork-release.yml` when a tag is pushed. The upstream tags were removed from the
fork and are not mirrored: the sync workflow fetches `main` only, so none come back.

- Tag format: `btw-v<versionName>-fork.<N>`, e.g. `btw-v2026.9.0-fork.2`. `versionName` is the
  app's version (from upstream); `N` counts fork builds of that version, starting at 1. The `btw-`
  prefix keeps the names from ever colliding with an upstream tag.
- To release: make sure `main` is what you want to ship, then
  `git tag btw-v2026.9.0-fork.2 && git push origin btw-v2026.9.0-fork.2`.
- The tag's version must equal `appVersionName` in `gradle/libs.versions.toml`, and the tagged
  commit must be on `main`; the workflow fails otherwise.
- `versionCode` is derived from the tag as `YYYYMMPPNN` (2026.9.0 fork.2 → `2026090002`), so it
  always increases and never needs bumping by hand. The workflow also fails if it is not greater
  than the previous release's. (Fork build 1 shipped with `versionCode` 1; any derived code is
  higher.)
- Signing happens in CI through `scripts/ci/fork-release.init.gradle`, a Gradle init script that
  injects the version code and signing config, so no upstream build file carries a hook. The
  workflow refuses to publish unless the APK's signing certificate is the one in
  `EXPECTED_CERT_SHA256`; changing keys means installed copies can't update in place.
- Repo secrets: `RELEASE_KEYSTORE_BASE64`, `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS`,
  `RELEASE_KEY_PASSWORD`. Keep the keystore itself backed up outside the repo too.
- The release gets the APK, `SHA256SUMS`, and notes with the package ID, commit, certificate and
  APK SHA-256, plus GitHub's generated changelog.

Sideloaded installs need "Allow restricted settings" in the app's info page before the
accessibility service can be enabled. Never commit the APK or the keystore.

The manual steps under "Building a signed APK" still work as a fallback, but a hand-built APK has
`versionCode` 1, so don't publish one.
