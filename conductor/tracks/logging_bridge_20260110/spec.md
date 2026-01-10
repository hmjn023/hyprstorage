# Specification: Rust-Kotlin Logging Bridge

## 1. Overview
このトラックでは、Rust (Wasm) 内で発生したログを Minecraft (NeoForge/Log4j) のコンソールおよびログファイルに出力するためのブリッジを構築します。これにより、Wasm内部の状態推移やエラーをインゲームまたはデバッグコンソールで直接確認できるようになります。

## 2. Functional Requirements
### 2.1 Logging Mechanism
- **FFI Integration:** Rust側からKotlinのエクスポートされた関数を直接呼び出す「Direct FFI Call」方式を採用します。
- **Structured Passing:** パフォーマンス最適化のため、Rust側では最小限の構造（テンプレートと引数）のみを作成し、最終的なメッセージの組み立てはKotlin側で行います。
- **Source Context:** ログ出力時に、Rust側のファイル名および行番号を付随情報として送信します。

### 2.2 Log Level Mapping
Rustの標準ログレベルを Minecraft (Log4j) のレベルに1対1でマッピングします。
- Rust `error` -> Minecraft `ERROR`
- Rust `warn` -> Minecraft `WARN`
- Rust `info` -> Minecraft `INFO`
- Rust `debug` -> Minecraft `DEBUG`
- Rust `trace` -> Minecraft `TRACE`

### 2.3 Rust Side API
- Rust標準の `log` クレートのバックエンドとして動作するカスタムロガーを実装します。これにより、既存の `info!()`, `error!()` マクロがそのまま利用可能になります。

## 3. Technical Implementation
- **Kotlin:** `WasmBridge` にログ受け取り用のエクスポート関数を定義。
- **Rust:** `log::Log` トレイトを実装した構造体を作成し、`log::set_logger` で登録。
- **Data Transfer:** 文字列の受け渡しには、既存の `alloc`/`dealloc` メカニズムを利用して、ポインタ経由でデータを渡します。

## 4. Acceptance Criteria
- [ ] Rust側で `info!("Hello from Wasm")` を実行した際、Minecraftのコンソールに `[INFO] [HyperStorage/Wasm]: Hello from Wasm` のように出力されること。
- [ ] ログレベル（WARN, ERROR等）が正しく反映されること。
- [ ] ログ出力に含まれるファイル名・行番号が正確であること。
- [ ] 大量のログ出力が発生しても、極端なパフォーマンス低下やクラッシュが発生しないこと。

## 5. Out of Scope
- Wasm内部のパフォーマンスプロファイリング（トレースログ以上の詳細な解析）。
- ユーザー向けのインゲームGUIでのログ表示（コンソール出力のみ）。
