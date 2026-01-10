# Plan: Rust-Kotlin Logging Bridge

## Phase 1: Kotlin-side Log Receiver (Infrastructure)
- [ ] Task: Define `LogReceiver` interface and a Log4j-based implementation in Kotlin.
- [ ] Task: Create unit tests for `LogReceiver` level mapping.
- [ ] Task: Add the log callback function to `WasmBridge` (FFI Export).
- [ ] Task: Conductor - User Manual Verification 'Kotlin-side Log Receiver' (Protocol in workflow.md)

## Phase 2: Rust-side Logger Core (log::Log Implementation)
- [ ] Task: Define the FFI extern signatures for the Kotlin log callback in Rust.
- [ ] Task: Implement the `log::Log` trait for a custom `WasmLogger` struct.
- [ ] Task: Create unit tests (mocking FFI if possible) for `WasmLogger` metadata handling.
- [ ] Task: Implement string passing logic (template + metadata) using the existing allocator.
- [ ] Task: Conductor - User Manual Verification 'Rust-side Logger Core' (Protocol in workflow.md)

## Phase 3: Integration and Initialization
- [ ] Task: Implement a global registration function in Rust to set the `WasmLogger`.
- [ ] Task: Update Kotlin `Hyperstorage.kt` to link the Wasm instance with the Kotlin log receiver upon startup.
- [ ] Task: Create an integration test that triggers a Rust log and verifies its appearance in the Kotlin logs.
- [ ] Task: Conductor - User Manual Verification 'Integration and Initialization' (Protocol in workflow.md)

## Phase 4: Validation and Refinement
- [ ] Task: Verify all log levels (ERROR to TRACE) are correctly mapped and displayed.
- [ ] Task: Verify that file name and line numbers are correctly included in the logs.
- [ ] Task: Perform a "stress test" by logging 1000+ messages in a single tick to ensure no memory leaks or crashes.
- [ ] Task: Conductor - User Manual Verification 'Validation and Refinement' (Protocol in workflow.md)
