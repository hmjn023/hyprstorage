# Plan: ID Translation Layer (WasmIdManager)

## Phase 1: Core Mapping Logic [checkpoint: bc4a4ff]
- [x] Task: Create `WasmIdManagerTest.kt` with failing tests for basic item ID mapping (on-demand assignment). [d447ec2]
- [x] Task: Implement basic item ID mapping in `WasmIdManager.kt` using `fastutil` maps. [d447ec2]
- [x] Task: Create failing tests for NBT mapping (handling null tags and identical tags with same/different hashes). [3888842]
- [x] Task: Implement NBT ID mapping logic in `WasmIdManager.kt` utilizing `ItemHashUtil`. [3888842]
- [x] Task: Conductor - User Manual Verification 'Core Mapping Logic' (Protocol in workflow.md) [4f081d3]

## Phase 2: Persistence (NBT-based Storage) [checkpoint: 7651519]
- [x] Task: Create unit tests for `save` and `load` functionality verifying data integrity after a cycle. [783278b]
- [x] Task: Implement `save` method to serialize maps into a Minecraft `CompoundTag` and write to disk. [8a81c5b]
- [x] Task: Implement `load` method to read the NBT file and populate the internal maps. [3126b01]
- [x] Task: Conductor - User Manual Verification 'Persistence' (Protocol in workflow.md) [67a3323]

## Phase 3: World Lifecycle Integration [checkpoint: 3af9b5d]
- [x] Task: Create integration tests simulating world load/save events to ensure `WasmIdManager` state is preserved. [d447ec2]
- [x] Task: Register NeoForge event listeners (`LevelEvent.Load`, `LevelEvent.Save`) to trigger manager's persistence methods. [3ed2e39]
- [x] Task: Implement directory resolution to ensure `id_map.dat` is stored in the correct world sub-directory. [2d2d61d]
- [x] Task: Conductor - User Manual Verification 'World Lifecycle Integration' (Protocol in workflow.md) [5b7547e]

## Phase 4: Concurrency and Refinement
- [x] Task: Add `ReadWriteLock` to `WasmIdManager` to ensure thread-safe access from multiple Wasm instances. [89e27a2]
- [x] Task: Verify thread safety with a concurrent stress test. [82da4f5]
- [x] Task: Conductor - User Manual Verification 'Concurrency and Refinement' (Protocol in workflow.md) [6c9126e]
