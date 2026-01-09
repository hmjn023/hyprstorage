# Initial Concept
Build "Hyper-Visor Storage," a high-performance Minecraft logistics/storage Mod using Rust (Wasm) for core logic (SoA, GC-less) and Java for I/O/Rendering.

# Product Guide: HyperStorage

## Target Audience
*   **Technical Players:** Fans of massive automation and complex factory building (e.g., Factorio, AE2, Mekanism).
*   **Server Administrators/Residents:** Those suffering from server lag caused by traditional storage/pipe mods.
*   **Logistics Enthusiasts:** Players who enjoy optimizing complex routing and logic.

## Project Goals
*   **Ultimate Performance:** Provide an alternative to existing storage mods (AE2, RS) with overwhelming performance capabilities.
*   **Logical Logistics:** Create a new logistics experience based on logical connections and instantaneous transfer, eliminating physical item movement.
*   **Wasm Demonstration:** Prove the potential of Wasm in modding (scripting, virtual generation, etc.).
*   **Overcoming Java Constraints:** Drastically reduce server load by bypassing Java's limitations (GC, object overhead) via Rust/Wasm.

## Core Features
*   **Wasm-Based Core Storage:** Ultra-high capacity storage capable of handling billions of items without performance degradation.
*   **Logical Transport System:** Instantaneous item transport using Channel IDs, eliminating the need for physical pipe rendering and entity processing.
*   **Scripting & Web-UI:** Advanced logistics control using JavaScript (Boa) and a modern, Web-like GUI for configuration.
*   **External Integration (Future):** Planned compatibility with external mods (like AE2) and Java-side inventory caching, prioritized for later phases.

## Development Guidelines
*   **Hybrid Architecture:** Clear separation of concerns—complex computation in Rust/Wasm, interaction/rendering in Kotlin/Java.
*   **Data-Oriented Design (SoA):** Strict adherence to Structure of Arrays (SoA) to maximize CPU cache efficiency and eliminate object overhead.
*   **Balanced Performance:** Prioritize server-side TPS and memory efficiency while allowing for acceptable client-side FPS trade-offs.
