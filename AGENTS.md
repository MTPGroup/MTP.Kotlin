MTP Kotlin – Agent Guide (authoritative; no Cursor/Copilot rules present)
Build all: `./gradlew build`
Desktop run: `./gradlew :composeApp:run`
Android debug APK: `./gradlew :composeApp:assembleDebug`
Tests (all common): `./gradlew :composeApp:commonTest`
Single test example: `./gradlew :composeApp:commonTest --tests "tech.hanasaki.momotalk_plus.ComposeAppCommonTest.example"`
Clean: `./gradlew clean`
No dedicated lint/ktlint task; use IDE formatter (Kotlin style, 4-space indent)
Imports: avoid wildcards; keep platform-specific imports minimal; prefer explicit androidx/compose/koin/ktor modules
Types: require explicit types on public APIs; favor `val`; use data/enum/sealed for domain models
Nullability: avoid `!!`; use safe calls/`let` or `requireNotNull` with message
Error handling: use `try/catch` or `runCatching` with meaningful messages; never swallow exceptions
Coroutines: structured concurrency only (scopes from ViewModel/DI); avoid `GlobalScope`; prefer `suspend` APIs
Compose: hoist state, `remember` for stability, side-effects in `LaunchedEffect`; keep composables small
Navigation: keep route constants centralized; avoid magic strings in composables
DI: Koin BOM in use; add bindings per feature module; inject via `get()`/`koinViewModel`
Networking: Ktor client configured via DI; set content-negotiation/logging there, not per-call
Serialization: kotlinx.serialization; keep DTOs with explicit fields and default values when safe
Persistence: Room schemas in `composeApp/schemas`; KSP processors generate them—do not hand-edit
Testing: prefer `kotlin.test` assertions; keep tests deterministic and platform-agnostic
