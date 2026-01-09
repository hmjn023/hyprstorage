# Specification: Refactoring & Architecture Overhaul

## 1. Overview
Current implementation concentrates logic in `lib.rs` and `HyperStorageBlockEntity.kt`. To ensure maintainability and scalability for future features (transport logic, GUI, scripting), we will refactor the codebase to follow Clean Architecture principles.

## 2. Rust (Wasm Core) Refactoring
**Goal:** Modularize the monolithic `lib.rs` to separate concerns between memory management, inventory logic, and API exports.

### Modules
- **`allocator`**: Handles `alloc` and `dealloc` for FFI memory management.
- **`inventory`**: Encapsulates the SoA (Structure of Arrays) data structures (`ITEM_IDS`, `QUANTITIES`, etc.) and core inventory operations (`add`, `remove`, `count`).
- **`api`**: Defines the `extern "C"` functions exposed to Wasm, acting as the controller layer.
- **`lib.rs`**: Entry point that exposes `api` functions.

## 3. Kotlin (Application Layer) Refactoring
**Goal:** Decouple `BlockEntity` from direct Wasm calls and introduce proper abstraction layers.

### Layers
- **Infrastructure Layer (`net.hmjn.hyperstorage.infrastructure.wasm`)**:
    - `WasmClient`: Wraps `Chicory` instance, handles low-level FFI types (Int/Long), and manages memory. Replaces current `WasmBridge`.
- **Domain Layer (`net.hmjn.hyperstorage.domain`)**:
    - `InventoryRepository`: Interface for inventory operations.
    - `InventoryService`: Business logic for inventory management and synchronization.
- **Presentation/Block Layer (`net.hmjn.hyperstorage.blockentity`)**:
    - `HyperStorageBlockEntity`: Uses `InventoryService` to perform actions. Responsible only for NeoForge lifecycle events and physical I/O.

## 4. Testing Strategy
- **Rust:** Unit tests for `inventory` module using standard `#[test]`.
- **Kotlin:** Unit tests for `InventoryService` mocking the `WasmClient`/Repository.

## 5. Directory Structure Changes
```
src/main/rust/src/
    ├── lib.rs
    ├── allocator.rs
    ├── inventory.rs
    └── api.rs

src/main/kotlin/net/hmjn/hyperstorage/
    ├── core/ (remove/migrate)
    ├── infrastructure/
    │   └── wasm/
    │       └── ChicoryWasmClient.kt
    ├── domain/
    │   ├── model/
    │   └── repository/
    └── blockentity/
        └── HyperStorageBlockEntity.kt (refactored)
```
