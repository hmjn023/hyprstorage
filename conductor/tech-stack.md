# Technology Stack

このプロジェクトは、パフォーマンスとメンテナンス性を両立させるため、Minecraft NeoForgeとRust WebAssemblyを組み合わせたハイブリッドアーキテクチャを採用しています。

## 1. Core Languages & Runtimes
- **Kotlin (2.0.0):** NeoForge APIとの対話、物理的なインタラクション、レンダリング、I/Oを管理。
- **Rust:** 高度な計算ロジック（輸送パス計算、在庫管理）をWasmとして実行。GCによるスタッターを排除。
- **WebAssembly (Wasm):** RustロジックをJava仮想マシン上で実行するためのバイナリ形式。
- **Java 21:** NeoForge 1.21.1の標準ランタイム。

## 2. Frameworks & APIs
- **NeoForge (21.1.217):** Minecraft 1.21.1向けの最新Modローダー。
- **Chicory:** Pure Javaで実装されたWasmインタプリタ。ネイティブ依存なしにWasmを実行。
- **Kotlin for Forge:** NeoForge環境でのKotlinサポート。

## 3. Build & Development Tools
- **Gradle:** プロジェクト全体のビルド管理。Rustのコンパイル（Cargo）とリソース同期を自動化。
- **ktlint:** Kotlinのコードスタイルチェックと自動整形。
- **Cargo:** Rustコードのコンパイル、依存関係管理。
- **Husky & lint-staged:** Git Hooks管理。コミット時のLint/Format、プッシュ時のTest/Buildを強制。
- **Node.js (npm):** 開発ツールのオーケストレーション（Husky, lint-staged）。
- **GitHub Flavored Markdown:** ドキュメント管理。

## 4. Architectural Patterns
- **Hybrid Wasm Integration:** 計算重負荷な処理をWasm（Rust）へ委譲し、物理的な処理をJava/Kotlinで実行。
- **Clean Architecture (Kotlin):** Infrastructure, Domain, Presentation層に分離し、Wasm実装の詳細をInfrastructure層に隠蔽。
- **Modular Rust Core:** API, Inventory, Allocatorモジュールに分割し、責務を明確化。
- **Structure-of-Arrays (SoA):** Rust側でのメモリ効率とキャッシュヒット率を最大化するためのデータ構造。
- **ID Translation Layer:** String (RegistryName) と u32 (WasmID) の高速変換。
