# Plan: Refactoring & Architecture Overhaul

## Phase 1: Rust Core Modularization [checkpoint: 29ff8a4]

- [x] Task: Create `src/main/rust/src/allocator.rs` and implement memory management functions (`alloc`, `dealloc`) with tests. [26df498]
- [x] Task: Create `src/main/rust/src/inventory.rs` and migrate SoA data structures and logic (`add`, `remove`, `count`) with unit tests. [f021eea]
- [x] Task: Create `src/main/rust/src/api.rs` and implement `extern "C"` functions delegating to `inventory` and `allocator`. [4ae772f]
- [x] Task: Update `src/main/rust/src/lib.rs` to expose modules and verify Wasm compilation. [4dacd84]
- [x] Task: Conductor - User Manual Verification 'Rust Core Modularization' (Protocol in workflow.md)

## Phase 2: Kotlin Infrastructure & Domain Layer [checkpoint: 0c4b605]

- [x] Task: Create `ChicoryWasmClient` in `infrastructure/wasm` implementing low-level Wasm interaction (replacing `WasmBridge` logic) with unit tests. [a9b1dff]
- [x] Task: Define `InventoryRepository` interface in `domain/repository` and implement it using `ChicoryWasmClient`. [e094596]
- [x] Task: Create `InventoryService` in `domain/service` to handle high-level logic (e.g., sync logic) with unit tests. [f3e026f]
- [x] Task: Conductor - User Manual Verification 'Kotlin Infrastructure & Domain Layer' (Protocol in workflow.md)

## Phase 3: BlockEntity Integration & Cleanup

- [ ] Task: Refactor `HyperStorageBlockEntity` to use `InventoryService` instead of direct `WasmBridge` calls.
- [ ] Task: Remove obsolete `WasmBridge.kt` and cleanup unused code.
- [ ] Task: Verify full system integration (Start game, place block, check logs).
- [ ] Task: Conductor - User Manual Verification 'BlockEntity Integration & Cleanup' (Protocol in workflow.md)
