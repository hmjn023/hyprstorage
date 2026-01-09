# 5. UI/UX Design & Wireframes (UIデザイン仕様書)

## 5.1. デザイン方針 (Design Philosophy)

*   **Web-Like:** Minecraft標準のGUI（インベントリグリッド）にとらわれず、モダンなWebアプリケーションのようなUIを提供する。
*   **Responsiveness:** 画面サイズに応じてレイアウトが自動調整される (Flexbox)。
*   **Dark Mode:** デフォルトでダークテーマを採用し、長時間作業でも目に優しくする。

---

## 5.2. メイン画面: Hyper Terminal

ネットワーク全体の状態を監視・操作する中央端末。

### 5.2.1. レイアウト構成
```
+---------------------------------------------------------------+
| [Header] HyperStorage OS v1.0                 [Energy: 98%]   |
+----------------------+----------------------------------------+
| [Sidebar]            | [Main Content]                         |
| - Dashboard          |                                        |
| - Inventory          |  +----------------------------------+  |
| - Channels           |  | Search: [ iron ing             ] |  |
| - Scripting          |  +----------------------------------+  |
| - Settings           |                                        |
|                      |  [Item Grid]                           |
|                      |  +---+ +---+ +---+ +---+ +---+ +---+   |
|                      |  |Fe | |Au | |Di | |St | |Re | |...|   |
|                      |  |12k| |500| |20 | |1M | |3k | |   |   |
|                      |  +---+ +---+ +---+ +---+ +---+ +---+   |
|                      |                                        |
|                      |  [Details Panel (Right, Collapsible)]  |
|                      |  Selected: Iron Ingot                  |
|                      |  Total: 12,450                         |
|                      |  Incoming: +50/tick                    |
|                      |  Outgoing: -12/tick                    |
+----------------------+----------------------------------------+
```

### 5.2.2. 機能詳細
*   **Inventory Tab:**
    *   アイテム一覧をアイコン表示。
    *   数値は "1k", "1M", "1G" と短縮表記。
    *   クリックすると右パネルに詳細統計を表示。
*   **Channels Tab:**
    *   稼働中のチャンネル一覧と流量を表示。
    *   ドラッグ＆ドロップで優先順位を変更可能。

---

## 5.3. 設定画面: Node Configurator

Importer, Exporter, Interface などの各ブロックを開いた時の設定画面。

### 5.3.1. レイアウト構成
```
+---------------------------------------+
| [Title] Iron Importer (Node #104)     |
+---------------------------------------+
| Channel Configuration:                |
|  Channel ID: [ 1 ] (Ores)             |
|                                       |
| Filter Settings:                      |
|  Mode: (o) Whitelist  ( ) Blacklist   |
|  +---------------------------------+  |
|  | [Raw Iron] [Raw Copper] [Gold ] |  |
|  | [ + Add Item                  ] |  |
|  +---------------------------------+  |
|                                       |
| Upgrades:                             |
|  [Speed] [Speed] [Stack] [ ]          |
|                                       |
| [Status: Active] [Buffer: 0/64]       |
+---------------------------------------+
```

---

## 5.4. スクリプトエディタ: Code Studio

`Script Controller` ブロックのGUI。

### 5.4.1. レイアウト構成
```
+---------------------------------------------------------------+
| File: main.js                                     [Run] [Stop]|
+---------------------------------------------------------------+
| 1 | // Main Logic for Ore Processing                          |
| 2 | const TARGET_LEVEL = 5000;                                |
| 3 |                                                           |
| 4 | function onTick() {                                       |
| 5 |   let current = Network.getItemCount('iron_ingot');       |
| 6 |   if (current < TARGET_LEVEL) {                           |
| 7 |     Network.setChannelActive(1, true);                    |
| 8 |   } else {                                                |
| 9 |     Network.setChannelActive(1, false);                   |
|10 |   }                                                       |
|11 | }                                                         |
|   |                                                           |
+---+-----------------------------------------------------------+
| [Console Output]                                              |
| > Script compiled successfully.                               |
| > [INFO] Channel 1 activated.                                 |
+---------------------------------------------------------------+
```

*   **Syntax Highlighting:** キーワードや変数を色分け表示。
*   **Auto Completion:** `Network.` と打つとメソッド候補を表示。
*   **Error Reporting:** コンパイルエラーを行番号付きで表示。

---

## 5.5. HTML/CSS 構造例
GUI描画エンジンに渡される実際のマークアップ例。

```html
<div class="container">
  <div class="header">
    <span class="title">Hyper Terminal</span>
    <span class="energy-bar" style="width: 80%"></span>
  </div>
  <div class="grid">
    <div class="item-slot" onclick="showDetails('iron_ingot')">
      <img src="texture:minecraft:item/iron_ingot" />
      <span class="count">12k</span>
    </div>
    <!-- more items... -->
  </div>
</div>

<style>
  .container {
    display: flex;
    flex-direction: column;
    width: 100%;
    height: 100%;
    background-color: #2b2b2b;
  }
  .grid {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    padding: 8px;
  }
</style>
```
