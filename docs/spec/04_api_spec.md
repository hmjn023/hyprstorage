# 4. API Specification (API仕様書) v1.1

## 4.1. Wasm Exports (Java -> Rust)
Wasmモジュールが公開し、Javaから呼び出される関数群。`#[no_mangle] pub extern "C"` で定義される。

### 4.1.1. System & Memory
*   `init() -> u32`
    *   Wasmインスタンスの初期化を行う。内部的にI/Oバッファを確保する。
*   `get_io_buffer_ptr() -> *mut u8`
    *   **New:** Java側が書き込み/読み込みを行うための固定長バッファ（Double Buffer）のポインタを取得する。
    *   毎回 `alloc` するオーバーヘッドとメモリ断片化を防ぐ。

### 4.1.2. Batch Operations (Double Buffering)
個別の `insert/extract` 関数は廃止し、バッファ経由の一括処理に統一する。

*   `process_batch(command_size: u32) -> u32`
    *   **Input:** Javaが `get_io_buffer_ptr()` の領域にコマンドパケットを書き込んだ後、この関数を呼ぶ。
    *   **Processing:** Rustはバッファ内のコマンドを処理し、結果を**同じバッファ（またはOutput用バッファ）**に書き込む。
    *   **Output:** 書き込まれた結果データのサイズ(バイト数)を返す。Javaはこのサイズ分だけバッファを読み取る。

### 4.1.3. Transport & Routing
*   `tick_transport() -> void`
    *   1 Tick分の輸送ロジック（Pub/Sub配送）を実行する。
*   `update_node_config(node_id: u32, config_data_size: u32)`
    *   I/Oバッファに設定データを書き込んだ状態で呼び出し、ノード設定を更新する。
*   `set_node_active(node_id: u32, active: bool)`
    *   **New:** チャンクアンロード等により、特定のノードへのアクセスを一時的に禁止する。
    *   `active=false` の場合、そのノードへの配送（Output）や、そのノードからの集荷（Input）をスキップする。
*   `merge_network_data(other_ptr: *const u8, size: usize)`
    *   **New:** 他のネットワークのバイナリデータ（`NetworkState`）を読み込み、自身の在庫に加算統合する。
    *   IDがグローバル共有されているため、単純な加算で済む。

---

## 4.2. Java API (Internal)
Mod内部の他クラスから利用されるAPI。

### 4.2.1. WasmBridge
*   `fun getInstance(networkId: UUID): WasmInstance`
    *   指定したネットワークIDに対応するWasmインスタンスを取得（なければ作成）。
*   `fun sendBatch(networkId: UUID, batch: BatchPacket)`
    *   Tick終了時にバッファへの書き込み -> `process_batch` 呼び出し -> 結果読み取りを行う。

### 4.2.2. Scripting Hooks
*   `fun compileScript(source: String): Result<CompiledScript, String>`
    *   ユーザー入力されたスクリプトをコンパイル（検証）する。
*   `fun executeScript(scriptId: Int)`
    *   コンパイル済みスクリプトを実行する。

---

## 4.3. Scripting API (JS/Rhai -> Rust)
ユーザースクリプトから呼び出し可能な組み込み関数。
JS側では `BigInt` を使用する。

### 4.3.1. Network Control
*   `Network.get_total_items(item_id_str: &str) -> BigInt`
    *   指定アイテムの総在庫数を取得。
*   `Network.set_channel_state(channel_id: i32, state: bool)`
    *   チャンネルの有効/無効を切り替える。
*   `Network.trigger_alert(message: &str)`
    *   管理者にチャットで通知を送る。

### 4.3.2. Production Control
*   `Machine.set_efficiency(machine_id: i32, percent: i32)`
    *   仮想生産機の稼働率を変更する。
*   `Machine.get_status(machine_id: i32) -> String`
    *   現在のステータス（Running, No Energy, Full）を取得。

### 4.3.3. Events
スクリプト内で定義可能なイベントハンドラ。
*   `onTick()`: 毎Tick呼ばれる。
*   `onItemLow(item_id: string, threshold: bigint)`: 在庫が閾値を下回った時に呼ばれる。