# Integration & Capabilities (外部連携・機能拡張)

## 概要 (Overview)
Common Capabilities (CC) や Jade 等、開発効率とパフォーマンス、ユーザー体験を向上させる外部ライブラリとの低レイヤー接続仕様を定義する。

---

## Common Capabilities (CC) 統合

**本 Mod のインベントリ同期の主要な足回り。**

### IInventoryState による変更検知
- 隣接インベントリが **`IInventoryState`** をサポートしている場合、スロットを全走査（ポーリング）せず、状態IDの変化のみを監視する。
- 状態IDが変化した時のみ、Wasm 側へ変更内容を Push する。

### Universal Handlers
- `IItemHandler`, `IFluidHandler`, `IEnergyStorage`, `IGasHandler` を CC の提供する共通インターフェース経由で扱う。
- 輸送計算ロジックをリソースタイプに関わらず単一のプロトコルに統合可能にする。

---

## Jade / The One Probe (TOP) 連携

### インワールド・デバッグ表示
- ブロックを視認した際、以下の情報を表示する。
    - **Channel ID:** 現在のチャンネル。
    - **Node Status:** Active / Sleeping (Backoff Level).
    - **Transfer Rate:** 直近 1 秒間の転送量。

---

## 高性能描画 (Flywheel)

### 描画パーツ構成 (Model Parts)
大量設置されるノードの描画負荷を Flywheel で最小化する。
- **Base:** ノードの土台部分（静的モデル）。
- **Indicator:** 稼働状態を示すライト。Flywheel インスタンスの `int` データ（色）で制御。
- **Channel Overlay:** チャンネル番号。Flywheel のインスタンスデータとして 32bit 整数を渡し、シェーダー側でテクスチャアトラスから数字をサンプリング。
- **Cables:** 隣接接続に応じた 6 方向のパーツ（Up, Down, North, South, East, West）をビットマスクでインスタンス化。

---

## 次のステップ (Next Steps)
- [ ] `build.gradle` への Common Capabilities の依存関係追加。
- [ ] Jade 用の `IBlockComponentProvider` 実装。
