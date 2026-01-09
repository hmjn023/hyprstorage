# 6. End Content & Technical Stack

## 6.1. エンドコンテンツ: "Beyond Logistics"

Wasmの計算能力を活かした、従来のModでは不可能な規模のシミュレーションと表現。

### 1. Pocket Civilization (仮想文明)
*   **概要:** 1ブロックの中で文明を育成する。
*   **仕組み:**
    *   Entityを使わず、人口・資源・技術レベルを純粋な数値シミュレーション（セル・オートマトン等）で処理する。
    *   時間を加速させ、数千年の興亡を観察する。
*   **報酬:** 文明が発展すると「ロストテクノロジー（中間素材）」を排出する。

### 2. The Swarm (群知能兵器)
*   **概要:** 数百機の自律ドローンによる絶対防御。
*   **Wasm:** Boidsアルゴリズム（分離・整列・結合）を毎Tick計算し、ドローンの座標を更新。
*   **Rendering:** Wasmから座標配列を受け取り、Instanced Renderingで一括描画。CPU負荷ほぼゼロ。
*   **機能:** 敵の攻撃に対する身代わり防御、一斉射撃。

### 3. Vector Field Projector (ベクトル干渉)
*   **概要:** 拠点周辺の物理法則を書き換える。
*   **Wasm:** 流体力学シミュレーションを行い、空間内の全Entityに対して「流速（ベクトル）」を加算する。
*   **効果:** 飛来する矢を逸らす、敵を軌道上に拘束する、ドロップ品を一箇所に吸い込む。

### 4. Holographic Display (空間ホログラム)
*   **概要:** 空中に浮かぶ3D情報ディスプレイ。
*   **Wasm:** ベクターグラフィクス計算を行い、複雑なグラフや地形マップをリアルタイム生成。
*   **用途:** 工場の稼働状況監視、SF的な拠点演出。

---

## 6.2. プレイヤー装備 (Curios)

「脳のリソースをWasmに外注する」ハイテク装備。

*   **Quantum Link Belt:** 倉庫直結。拾ったアイテム即転送、使ったアイテム即補充。通信量は「見た目」分だけ。
*   **Chrono-Anchor:** Wasmリングバッファで過去数秒のステータスを常時録画。死んだ瞬間に巻き戻す。
*   **Entropy Trowel:** 周囲の建築パターンを解析（WFCアルゴリズム）し、次に置くべきブロックを予測して設置。
*   **Laplacian Visor:** 敵の攻撃範囲や安全ルートを物理演算して視界にオーバーレイ表示。

---

## 6.3. アドオン構想

### Critical Mass (核物理学)
*   **概要:** マルチブロックではなく「シミュレーション」としての原子炉。
*   **処理:** 巨大なグリッド上での中性子拡散・熱伝導シミュレーションをWasmで実行。

### Algorithmic Trade (経済)
*   **概要:** 架空の株式市場シミュレーション。
*   **処理:** 資源を売却して市場に影響を与え、変動相場で利益を出す。

### Wasm Synth (音響)
*   **概要:** 波形合成（DSP）によるプロシージャル音楽。
*   **処理:** WasmでPCMデータをリアルタイム生成。

---

## 6.4. 技術スタック選定まとめ

### Host Side (Java/Kotlin)
*   **Loader:** Architectury API (Forge/Fabric両対応)
*   **Wasm Runtime:** **Chicory** (推奨) / GraalWasm
*   **Rendering:** Sodium / Embeddium API (Instanced Rendering)
*   **Git:** Eclipse JGit
*   **Network:** Netty (Vanilla)

### Guest Side (Rust)
*   **Scripting:** **Boa** (JS) / **Rhai**
*   **GUI:** **Taffy** (Layout) / **html5gum** (Parser)
*   **Serialization:** **Bincode** (Binary) / Serde
*   **ECS:** **Hecs** (軽量Entity管理)
*   **Memory:** Slab (固定長アロケータ)

### Development Tools
*   **Build:** Gradle (Java) + Cargo (Rust)
*   **Bundler:** **Vite** (User Script)
*   **Wasm:** wasm-pack / wit-bindgen

---

## 6.5. 最終的なアーキテクチャ図

```text
[User's PC (Dev Env)]           [Minecraft Server (Runtime)]
+-------------------+           +----------------------------+
| VS Code           |           | Java (Minecraft / Forge)   |
|  - TypeScript     |  Build    |  +----------------------+  |
|  - NPM Packages   | ------->  |  | HyperVisor Mod       |  |
|  - Vite Plugin    | (js file) |  |  - Wasm Runtime      |  |
|                   |           |  |  - Git Puller/Loader |  |
+-------------------+           |  +----------+-----------+  |
                                |             | (Interop)    |
                                |  +----------v-----------+  |
                                |  | Wasm Sandbox (Rust)  |  |
                                |  |  - Core Logic        |  |
                                |  |  - Inventory DB      |  |
                                |  |  - Boa (JS Engine)   |  |
                                |  +----------------------+  |
                                +----------------------------+
```

この構成により、**「パフォーマンス」「拡張性」「開発体験」**の全てにおいて既存Modを凌駕するプラットフォームを構築する。
