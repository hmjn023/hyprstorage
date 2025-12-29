# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

HyperStorage (hyperVisor Storage) is a high-performance Minecraft logistics/storage mod for NeoForge 1.21.1 that uses a hybrid Rust (WebAssembly) + Kotlin/Java architecture to eliminate traditional bottlenecks (GC lag, pathfinding overhead, NBT operations).

**Core Design Philosophy:**
- **Brain (Wasm):** Computation, management, and logic in Rust (GC-less, linear memory, SoA data structures)
- **Body (Kotlin/Java):** Physical interaction, I/O, and rendering via Minecraft APIs
- **No Physics:** Logical connections (EnderIO-style) and wireless transfer (Modular Routers-style) instead of item entity rendering

## Build Commands

### Standard Gradle Tasks
```bash
# Build the entire project (compiles Rust → Wasm, then Kotlin/Java)
./gradlew build

# Run the Minecraft client
./gradlew runClient

# Run the dedicated server
./gradlew runServer

# Run game tests
./gradlew gameTestServer

# Generate data (recipes, tags, etc.)
./gradlew runData

# Clean build artifacts
./gradlew clean
```

### Rust WebAssembly Build
```bash
# Build Rust to Wasm manually (usually automatic via processResources)
cd src/main/rust
cargo build --release --target wasm32-unknown-unknown

# The output will be at:
# src/main/rust/target/wasm32-unknown-unknown/release/hyper_visor_storage_wasm.wasm
```

### Development Workflow
- Rust changes: Run `./gradlew processResources` to rebuild and copy the .wasm file to resources
- Kotlin/Java changes: Standard `./gradlew build` or IDE compilation
- The build automatically triggers Rust compilation via the `buildRust` task

## Architecture

### Hybrid Wasm Integration

**WasmBridge** (`src/main/kotlin/net/hmjn/hyperstorage/core/WasmBridge.kt`)
- Singleton that loads and manages the Chicory Wasm runtime instance
- Provides Java/Kotlin → Wasm FFI interface
- Currently implements basic `add()` test function
- Future: Will handle NBT-free item management using `ItemKey { id: u32, nbt_hash: u64 }`

**Rust Wasm Module** (`src/main/rust/src/lib.rs`)
- Compiled to `wasm32-unknown-unknown` target
- Exports C-ABI functions (`#[no_mangle] pub extern "C"`)
- Manual memory management via `alloc()` and `dealloc()` functions
- Future: Structure-of-Arrays (SoA) inventory state, JavaScript engine (Boa) for user scripting

### Data Flow Pattern

```
Minecraft World (Java/Kotlin)
    ↓
WasmBridge (FFI Layer)
    ↓
Wasm Linear Memory (Rust)
    - SoA data structures: item_ids, nbt_hashes, quantities, location_ids
    - No Java objects in Wasm
    ↑
Results returned via memory/exports
    ↓
Java NBT Dictionary (Map<Long, CompoundTag>)
```

### Key Design Patterns

**ID Translation Layer:**
- `WasmIdManager` (planned) maintains `String (RegistryName) ↔ Integer (WasmID)` bidirectional mapping
- Synchronized on startup and world load
- Eliminates string operations in hot paths

**Multi-Instance Sharding:**
- Independent Wasm instances per transport network ID
- Each instance has isolated memory space
- Java `ExecutorService` thread pool for parallel computation
- `ReadWriteLock` for concurrent access control

**Buffered I/O:**
- Ring buffers in Java for item accumulation
- Batch `memcpy` to Wasm at tick end
- "Speculative Insert" for outputs: attempt insert, return failures to Wasm
- Smart Sleep: disabled nodes when outputs are full

## Project Structure

```
src/main/
├── kotlin/net/hmjn/hyperstorage/
│   ├── Hyperstorage.kt          # Main mod class, Wasm initialization
│   ├── core/
│   │   └── WasmBridge.kt         # Chicory runtime wrapper
│   └── block/
│       └── ModBlocks.kt          # Block registry
├── rust/
│   ├── Cargo.toml                # Rust dependencies (serde, bincode)
│   └── src/
│       └── lib.rs                # Wasm exports (add, alloc, dealloc)
├── resources/
│   └── wasm/                     # Auto-generated .wasm files (via buildRust)
└── templates/META-INF/
    └── neoforge.mods.toml        # Mod metadata template
```

## Dependencies & Bundling

**Chicory Wasm Runtime:**
- Pure Java implementation (no native dependencies)
- Bundled via `shadow` configuration + `jarJar` for distribution
- Classes copied to build output via `copyShadedClasses` task
- Alternative: GraalVM Polyglot (optional, server-only, auto-detected)

**Kotlin for Forge:**
- Using `thedarkcolour:kotlinforforge-neoforge:5.3.0`
- Provides Kotlin stdlib and NeoForge integration

## Important Implementation Details

### Wasm Resource Loading
The Wasm module is loaded from `/wasm/hyper_visor_storage_wasm.wasm` at common setup:
- See `Hyperstorage.kt:69-79` for loading logic
- Must use `javaClass.getResourceAsStream()` (not file path)
- Error handling logs to `hyperstorage` logger

### Memory Safety
- Rust uses `#![no_std]` conventions (panic = "abort")
- Manual allocator via `alloc()`/`dealloc()` for Java ↔ Wasm data passing
- Future: Double-buffering (Current/Next memory) for crash recovery

### Gradle Task Dependencies
```
compileKotlin → copyShadedClasses → jar
buildRust → processResources → build
[runClient, runServer, etc.] all depend on copyShadedClasses
```

## Future Architecture (from require.md)

**Planned Features:**
1. **Transport Logic:** Pub/Sub model with O(1) channel lookup, dirty flag optimization
2. **Crafting System:** Tier 1 (Wasm-internal graph), Tier 2 (AE2 slave mode)
3. **JS Scripting:** Boa engine for user-defined filters (with instruction metering for safety)
4. **Persistence:** Raw binary dump of Wasm linear memory (no NBT overhead)
5. **Emergency Save:** Shutdown hooks for JVM kill scenarios

**Data Structures (SoA):**
```rust
struct NetworkState {
    item_ids: Vec<u32>,        // String-interned item IDs
    nbt_hashes: Vec<u64>,      // Deterministic NBT hashes
    quantities: Vec<u64>,      // Stack counts
    location_ids: Vec<u32>,    // Storage locations
}
```

## NeoForge Version

- Minecraft: 1.21.1
- NeoForge: 21.1.217
- Java Toolchain: 21
- Kotlin: 2.0.0

## Troubleshooting

### Build Error: "Could not get unknown property 'net'"

**症状:**
```
Build file 'build.gradle' line: 118
Could not get unknown property 'net' for root project 'hyperStorage'
```

**原因:**
- `build.gradle`で`tasks.withType(net.neoforged.moddevgradle.tasks.RunGameTask)`のように完全修飾名(FQCN)を直接参照していた
- Gradleビルドスクリプトのコンパイル時点では、プラグインがまだロードされておらず、クラスが解決できない
- インポート文を追加しても、ビルドスクリプト自体のコンパイル段階でクラスが利用できないため失敗する

**解決方法:**
型参照を使用せず、具体的なタスク名で依存関係を設定する:

```groovy
// ❌ 動作しない
tasks.withType(net.neoforged.moddevgradle.tasks.RunGameTask).configureEach {
    dependsOn('copyShadedClasses')
}

// ✅ 正しい方法
['runClient', 'runServer', 'gameTestServer', 'runData'].each { taskName ->
    def task = tasks.findByName(taskName)
    if (task != null) {
        task.dependsOn('copyShadedClasses')
    }
}
```

**ポイント:**
- `findByName()`を使用することで、存在しないタスクによるエラーを回避
- NeoForgeのバージョンによってタスク名が異なる場合にも対応可能

### Build Error: "Task uses output without declaring dependency"

**症状:**
```
Task ':jar' uses this output of task ':copyShadedClasses' without declaring 
an explicit or implicit dependency
```

**原因:**
- `jar`タスクが`copyShadedClasses`タスクの出力ディレクトリを使用しているが、明示的な依存関係が宣言されていない
- Gradleのタスク実行順序が保証されず、ビルドが失敗する可能性がある

**解決方法:**
`jar`タスクに明示的な依存関係を追加:

```groovy
// Ensure jar task waits for shaded classes
tasks.named('jar').configure {
    dependsOn('copyShadedClasses')
}
```

### Wasm Module Not Found

**症状:**
```
[ERROR] [hyperstorage/]: Wasm file not found in resources!
```

**原因:**
- Rustのビルドが実行されていない、または`.wasm`ファイルがリソースディレクトリにコピーされていない

**解決方法:**
1. Rustのビルドを手動で実行:
   ```bash
   ./gradlew buildRust
   ```

2. リソース処理を実行:
   ```bash
   ./gradlew processResources
   ```

3. 完全なクリーンビルド:
   ```bash
   ./gradlew clean build
   ```

4. `.wasm`ファイルの存在確認:
   ```bash
   ls -la build/resources/main/wasm/
   ```

### Chicory Runtime ClassNotFoundException

**症状:**
```
java.lang.ClassNotFoundException: com.dylibso.chicory.runtime.Instance
```

**原因:**
- `copyShadedClasses`タスクが実行されていない、またはChicoryライブラリがクラスパスに含まれていない

**解決方法:**
1. `copyShadedClasses`タスクが正しく設定されているか確認:
   ```groovy
   task copyShadedClasses(type: Copy) {
       dependsOn('compileKotlin')
       from { configurations.shadow.collect { it.isDirectory() ? it : zipTree(it) } }
       into sourceSets.main.output.classesDirs.first()
       duplicatesStrategy = DuplicatesStrategy.EXCLUDE
   }
   ```

2. 依存関係の確認:
   ```bash
   ./gradlew dependencies --configuration runtimeClasspath | grep chicory
   ```

3. クリーンビルドを実行:
   ```bash
   ./gradlew clean build
   ```
