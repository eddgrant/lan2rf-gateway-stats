# TASKS

## Priority 1 (High)

### 1. Fix InfluxDB write error resilience
- **Status:** DONE
- **File:** `StatusDataPublisher.kt:48-50`
- **Problem:** When an InfluxDB write fails, the error propagates via `sink.error(e)`, terminating the entire reactive subscription. The application continues running but stops collecting and publishing data -- effectively a zombie process requiring a restart.
- **Fix:** Add error recovery (e.g. `onErrorResume`) so that a transient InfluxDB failure is logged and skipped, matching the existing HTTP error handling pattern in `LAN2RFRepository`.

### 2. Use configured room names instead of hardcoded strings
- **Status:** TODO
- **File:** `StatusDataPublisher.kt:113,143`
- **Problem:** `val room1 = "room1"` and `val room2 = "room2"` are hardcoded despite `LAN2RFConfiguration` already exposing `room1Name` and `room2Name` properties. The configured values are logged at startup but never used as InfluxDB tag values.
- **Fix:** Replace the hardcoded strings with `lan2RFConfiguration.room1Name` and `lan2RFConfiguration.room2Name`.

### 3. Clean up Reactor-coroutine bridge
- **Status:** TODO
- **File:** `StatusDataPublisher.kt:34-53`
- **Problem:** The hand-rolled `Mono.create` + `CoroutineScope(Dispatchers.IO).launch` bridge creates a new unstructured coroutine scope per emission with no supervision. This is fragile and hard to reason about.
- **Fix:** Use `kotlinx-coroutines-reactor` which provides `mono {}` and `flux {}` coroutine builders that integrate properly with Reactor's lifecycle, cancellation, and error propagation.

## Priority 2 (Medium)

### 4. Add health check endpoint
- **Status:** TODO
- **Problem:** The application has no HTTP endpoints. If the reactive subscription silently dies, no orchestrator (Docker, Kubernetes) can detect it. For a long-running IoT polling service, this is an operational gap.
- **Fix:** Add Micronaut's `management` dependency for a `/health` endpoint. Consider a custom health indicator that checks whether the `Disposable` is still active and whether the last successful write was within a reasonable window.

### 5. Introduce a sealed interface for measurement types
- **Status:** TODO
- **File:** `StatusDataPublisher.kt:60`
- **Problem:** `asMeasurements()` returns `Set<Any>` because the four measurement types (Temperature, Pressure, OperationalStatus, TextStatus) share no common type. The compiler can't prevent non-measurement objects from being added.
- **Fix:** Create a `sealed interface Measurement` implemented by all four types. Return `Set<Measurement>` from `asMeasurements()`.

## Priority 3 (Low)

### 6. Fix inconsistent camelCase in StatusData
- **Status:** TODO
- **File:** `StatusData.kt:24`
- **Problem:** `room2Temperaturelsb` (lowercase 'l') vs `room2TemperatureLsb` used everywhere else. Minor naming inconsistency.
- **Fix:** Rename to `room2TemperatureLsb`. The `@param:JsonProperty` annotation means the JSON mapping is unaffected.

### 7. Standardise test assertions on Kotest matchers
- **Status:** DONE
- **Problem:** Tests mix JUnit assertions (`assertEquals`, `assertTrue`) with Kotest matchers (`shouldBe`, `shouldContainExactlyInAnyOrder`). The project uses Kotest as its test runtime.
- **Fix:** Migrate remaining JUnit assertions (primarily in `StatusDataTest`) to Kotest matchers.

### 8. Remove or populate empty InfluxDBServiceTest
- **Status:** TODO
- **File:** `InfluxDBServiceTest.kt`
- **Problem:** The test file exists but contains no tests. Gives a false impression of coverage.
- **Fix:** Either add meaningful tests or delete the file.

### 9. Remove unnecessary @Inject annotations from IntergasService
- **Status:** TODO
- **File:** `IntergasService.kt:10-11`
- **Problem:** Both constructor parameters use `@Inject` despite AGENTS.md stating: "Don't use `@Inject` on constructors if it's the only one." Micronaut handles single-constructor injection automatically.
- **Fix:** Remove the `@Inject` annotations.

### 10. Remove unused log4j2.xml
- **Status:** TODO
- **File:** `src/main/resources/log4j2.xml`
- **Problem:** The project uses Logback. The log4j2.xml file is dead configuration that could confuse contributors.
- **Fix:** Delete the file.

### 11. Reclassify LAN2RFClientIntegrationTest
- **Status:** TODO
- **File:** `LAN2RFClientIntegrationTest.kt`
- **Problem:** This integration test targets a physical device at a hardcoded IP (`192.168.2.58`). It can only pass on the developer's home network, meaning CI can never run it.
- **Fix:** Move to the `endToEndTest` source set (where device-dependent tests belong), or introduce WireMock to simulate the LAN2RF device for a true integration test.
