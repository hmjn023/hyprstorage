# Hyper-Visor Storage Architecture

## 1. Project Overview & Philosophy

### 1.1. The "Hyper-Visor" Concept
本プロジェクトは、単なるMinecraft Modではなく、**Minecraftの物流・ストレージ処理を「仮想化（Hypervisor）」し、下層のハードウェア効率を極限まで引き出すためのシステム**である。
従来のModが抱えるパフォーマンス問題（TPS低下、ラグ）を、**Data-Oriented Design (データ指向設計)** と **WebAssembly (Wasm)** によって物理的に解決する。

*   **Brain (Guest/Wasm):** 思考・管理・計算を担当。Rustで実装。GCレス、リニアメモリ、SoAによるキャッシュ最適化。
*   **Body (Host/Java):** 物理世界との接点。Minecraft API (Block/Item/GUI) との連携。I/Oとレンダリングに専念する。

### 1.2. Why Wasm? (Technical Justification)
現代のMod環境におけるボトルネックは「メモリ容量」ではなく**「CPUのメモリ帯域・レイテンシ」**にある。

| 課題 | 従来 (Java / OOP) | 解決策 (Wasm / DoD) |
| :--- | :--- | :--- |
| **Pointer Chasing** | オブジェクトがヒープに散乱し、CPUキャッシュミスが多発。Stall（待ち時間）が処理の大半を占める。 | **Linear Memory & SoA:** データが連続配列に配置され、L1/L2キャッシュヒット率が劇的に向上する。 |
| **GC Pressure** | 計算のたびに短命オブジェクト（Iterator, Wrapper）が生成され、GCラグ（Stop-the-world）を誘発。 | **Zero Allocation:** 事前に確保されたメモリ領域を使い回すため、GCが一切発生しない。 |
| **Memory Footprint** | オブジェクトヘッダにより、int 1個でも数十バイト消費。 | **Raw Bytes:** `u32` は4バイト。メモリ効率が数倍〜数十倍。 |

---

## 2. Detailed Specifications (詳細仕様)

各機能の具体的な実装仕様については、以下のドキュメントを参照してください。

- **[Requirements (要件定義)](./01_requirements.md)**: システム全体の機能・非機能要件。
- **[Use Cases (ユースケース)](./02_use_case.md)**: プレイヤーおよびシステム間のインタラクション。
- **[Data Model (データモデル)](./03_data_model.md)**: Wasmリニアメモリ上のSoA構造およびJava側のマッピング。
- **[API Specification (API仕様)](./04_api_spec.md)**: Java ↔ Wasm 間のFFIおよびスクリプトAPI。
- **[UI/UX Design (UIデザイン)](./05_ui_design.md)**: HTML/CSSベースのGUIシステム仕様。

---

## 3. Core Architecture: Rust & Wasm

### 3.1. Memory Layout: Structure of Arrays (SoA)
Wasm内では「アイテムオブジェクト」を持たず、プロパティごとの巨大な配列（Vector）として管理する。

### 3.2. Communication Strategy: Batching & Shared Memory
Java ↔ Wasm 間の呼び出し（Boundary Crossing）はコストが高いため、回数を最小限にする。

*   **Batch Processing:** 変更があったデータはJava側でバッファリングし、Tickの終わりにまとめてWasmへ転送する。
*   **Direct Memory Access:** `DirectByteBuffer` を使用し、JavaとWasmでメモリ領域を共有してコピーコストを削減する。

---

## 4. Transport Logic: "The Router"

物理的なパイプ移動を廃止し、**論理的な瞬間移動**を採用する。

*   **Routing Algorithm:** 空間的な距離計算を行わず、チャンネルベースのハッシュテーブル参照により **O(1)** で宛先を特定する。
*   **Pub/Sub Model:** 送信側(Publisher)と受信側(Subscriber)を仲介役(Broker/Wasm)が結びつける疎結合な物流。

---

## 5. Crafting & Integration

### 5.1. Hierarchical Crafting
「単純作業」と「複雑作業」を分離し、AE2のエコシステムを利用しつつ計算負荷を取り除く。
- **Tier 1 (Internal):** Wasm内での高速な数値計算による仮想クラフト。
- **Tier 2 (Orchestration):** AE2を「下請け工場」として利用するマイクロ発注システム。

### 5.2. AE2 Integration
本ModをAE2の「外部ストレージ」として認識させ、既存のAE2ネットワークの在庫管理負荷を肩代わりする。

---

## 6. Roadmap

プロジェクトの進捗状況および開発計画については、**[Roadmap.md](./roadmap.md)** を参照してください。

---

## 7. Similar Technologies & References
*   **Factorio:** アイテムの一元管理・座標計算ロジック。
*   **Sodium:** レンダリングデータのバッファ化とゼロアロケーション。
*   **XNet / Modular Routers:** 輸送ロジックの概念的モデル。
