# Development Roadmap

## Phase 0: Architecture & Specification (Done)
- [x] Analyze requirements and bottlenecks (GC, Latency).
- [x] Define hybrid architecture (Rust/Wasm + Java).
- [x] Create detailed specifications (Requirements, Use Cases, Data Model, API).
- [x] Establish documentation archives.

## Phase 1: Rust Core Foundation (Current Focus)
**Goal:** Implement the "Brain" of the system with SoA data structures and efficient memory management.
- [ ] **Data Structure Implementation (`src/main/rust/src/lib.rs`)**
    - [ ] Implement `NetworkState` with SoA vectors.
    - [ ] Implement `Channel` logic (subscribers, filters).
- [ ] **Memory Management**
    - [ ] Implement Double Buffering mechanism for I/O.
    - [ ] Implement `process_batch` API.
- [ ] **Serialization**
    - [ ] Implement `bincode` serialization for persistence.
    - [ ] Implement `NetworkState` merging logic.
- [ ] **Testing**
    - [ ] Write Rust unit tests for inventory operations.

## Phase 2: Java Integration & ID Management
**Goal:** Connect the Minecraft world to the Wasm brain.
- [ ] **Global ID Manager (`WasmIdManager.kt`)**
    - [ ] Implement global RegistryName <-> ID mapping.
    - [ ] Implement NBT <-> ID mapping with collision handling.
    - [ ] Implement persistence for ID maps.
- [ ] **Wasm Bridge V2 (`WasmBridge.kt`)**
    - [ ] Update to use `Batch I/O` (Double Buffering).
    - [ ] Handle Wasm instantiation and memory limit.
- [ ] **Basic Blocks**
    - [ ] `HyperCore` block (Controller).
    - [ ] `Importer` / `Exporter` blocks (Basic implementation).
    - [ ] Verify item transfer (Chest -> Wasm -> Chest).

## Phase 3: Transport Logic & Routing
**Goal:** Enable complex logistics and "Smart" behaviors.
- [ ] **Routing Logic (Rust)**
    - [ ] Implement Pub/Sub delivery in `tick_transport`.
    - [ ] Implement Round-Robin / Priority load balancing.
- [ ] **Node Management (Java)**
    - [ ] Implement `Smart Sleep` logic for Exporters.
    - [ ] Handle Chunk Load/Unload events (`set_node_active`).
- [ ] **GUI Prototype**
    - [ ] Create basic configuration screens for Importer/Exporter.

## Phase 4: Scripting & Advanced UI
**Goal:** User programmability and Web-like UI.
- [ ] **Scripting Engine**
    - [ ] Integrate `Boa` (JS Engine) into Wasm.
    - [ ] Implement `BigInt` support for item counts.
    - [ ] Expose `Network` and `Item` APIs to JS.
- [ ] **UI Engine**
    - [ ] Implement HTML Parser & Taffy Layout (Rust).
    - [ ] Implement Java-side Renderer.

## Phase 5: Virtual Production & Ecosystem
**Goal:** High-performance resource generation and mod compatibility.
- [ ] **Virtual Production**
    - [ ] Implement LootTable simulation & cost calculation.
    - [ ] Implement "Mob Data" item.
- [ ] **Integrations**
    - [ ] Implement AE2 `IMEInventory` capability.
    - [ ] Add EMI / JEI support.
