# Fork notes: jhot21/btw

This is a maintained fork of [bitwarden/android](https://github.com/bitwarden/android) that adds a
**Passgen** generator type: deterministic passwords from passgen v2/v3
([github.com/jhot21/passgen](https://github.com/jhot21/passgen)).

## Rules

- New code lives in new files (`data/tools/passgen/`, `ui/tools/feature/generator/*Passgen*`,
  `ui/tools/feature/generator/passgen/`, `res/values/strings_passgen.xml`).
- Every edit to an upstream file carries `// PASSGEN:` and is listed under **Hooks** below.
- Upstream-sync PRs **must be merged with a merge commit** (never squash or rebase), otherwise every
  later sync re-conflicts on the same hunks.
- Upstream's own workflows are disabled in this repo's Actions settings (they need Bitwarden secrets).
  Don't edit them.
- `fork-checks.yml`'s `passgen-tests` job needs `read:packages` to pull `bitwarden/sdk` from
  `maven.pkg.github.com` (see `settings.gradle.kts`); it uses the default `GITHUB_TOKEN`. If that
  token ever gets a 401 for bitwarden's package, store a classic PAT with the `read:packages` scope
  as a repo secret (e.g. `PACKAGES_TOKEN`) and reference it in that job's `GITHUB_TOKEN` env instead.
- The passgen salt is stored only on the device. Back it up (Generator → Passgen → copy salt):
  clearing app data without a backup makes passwords unrecoverable.

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
4. `./gradlew :app:testStandardDebugUnitTest --tests 'com.x8bit.bitwarden.data.tools.passgen.*' --tests 'com.x8bit.bitwarden.ui.tools.feature.generator.*'`
5. `./gradlew :app:assembleStandardDebug`
6. Push, mark ready for review, merge with a **merge commit**.

## Hooks

Format: `` - `<file>` :: `<text that must appear on a line with // PASSGEN:>` ``

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

Note: `GeneratorViewModel.kt`'s `loadOptions` hook (line 387 as of this writing) reads
`is PassgenMainType -> updateGeneratorMainType { it }` — it re-derives the main type on
process-death restore instead of being a no-op. The other `is PassgenMainType -> Unit` entry
(line 475, in `loadPasscodeOptions`) is unrelated and stays a no-op: that function only ever
handles the two passcode types, so `PassgenMainType` never reaches it via `loadOptions`'s
dispatch. The `PASSGEN(labelRes = ...)` enum entry's marker sits on its own comment
line (`        // PASSGEN:`) above the wrapped `PASSGEN(` statement, so its anchor is that comment
line's exact indentation rather than the `PASSGEN(` text itself.
