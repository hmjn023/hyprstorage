# 5. Scripting & GUI System

## 5.1. スクリプト環境: "Minecraft内 統合開発環境"

ユーザーが高度な物流制御を行うために、2つのスクリプト言語をサポートする。

### Boa (JavaScript)
*   **用途:** GUI制御、イベントハンドリング、複雑な条件分岐。
*   **特徴:** Rust製JSエンジン。Wasmに埋め込み可能。
*   **メリット:** Web開発の知識（ES6）がそのまま使える。
*   **API:** `document.getElementById` や `Network.getItemCount` などを提供。

### Rhai (Rust Script)
*   **用途:** パイプラインの高速ルーティング、フィルタリング。
*   **特徴:** Rust専用の軽量スクリプト。
*   **メリット:** 実行速度が非常に速い。型システムがRustと親和性が高い。
*   **安全性:** 無限ループ対策（Instruction Limit）が容易。

---

## 5.2. GUIシステム: HTML/CSS

既存Modの「お絵かきGUI」を廃止し、WebライクなUI構築を実現する。

### アーキテクチャ
1.  **Parser (Wasm):** `html5gum` 等でHTML文字列をパースし、DOMツリーを構築。
2.  **Layout (Wasm):** **Taffy** (Rust製Flexboxエンジン) を使用し、要素の座標とサイズを計算する。
3.  **Renderer (Java):** Wasmから描画命令（矩形、テキスト、テクスチャID）を受け取り、Minecraftのレンダラーで描画する。

### ユーザー体験 (DX)
*   **Flexbox:** `display: flex;` で複雑なレイアウトも自動整列。
*   **Data Binding:** `{{ energy }}` のように書くだけで、Wasm内の変数をリアルタイム表示。
*   **Event:** `onclick="stop_machine()"` でスクリプトを実行。

---

## 5.3. 開発環境とエコシステム

### 外部エディタ連携 (VS Code)
ゲーム内エディタの貧弱さを解消するため、外部ツールを推奨する。

*   **SDK (@hypervisor/sdk):**
    *   npmパッケージとして提供。
    *   TypeScript型定義 (`.d.ts`) とモックランタイムを含む。
    *   `node test.js` でロジックの単体テストが可能。

### Vite Plugin
*   **機能:**
    *   TypeScriptのトランスパイル。
    *   外部ライブラリ (lodash, dayjs等) のバンドル。
    *   **Hot Reload:** ファイル保存時、Minecraftのワールドフォルダに `.js` を出力し、Mod側で自動リロードする。

### Git連携
*   **In-Game Git Client:**
    *   **JGit** ライブラリを使用。
    *   ゲーム内から `git pull` を実行し、GitHub上のスクリプトをデプロイ可能にする。
    *   サーバー管理者のロールプレイ（DevOps）を実現。

### セキュリティ
*   **Wasm Sandbox:** スクリプトはWasm内で完結するため、ホストOSへのアクセス（ファイル削除、プロセス起動）は物理的に不可能。
*   **Network Restriction:** 外部通信は「Java側が管理するGit Pull」のみ。スクリプトからの任意HTTP通信は禁止。
*   **Metering:** CPU時間を食いつぶす無限ループは、Wasmの燃料切れで強制停止させる。

---

## 5.4. 実装ライブラリ選定

| 分野 | ライブラリ (Rust/Java) | 役割 |
| :--- | :--- | :--- |
| **Script** | **Boa** (Rust) | JavaScriptエンジン。Wasm埋め込み用。 |
| **Script** | **Rhai** (Rust) | 軽量スクリプト。高速ロジック用。 |
| **Layout** | **Taffy** (Rust) | GUIレイアウト計算 (Flexbox)。 |
| **Parser** | **html5gum** / **Lightning CSS** | HTML/CSS解析。 |
| **Git** | **JGit** (Java) | ゲーム内Gitクライアント。 |
| **Build** | **Vite** (Node.js) | ユーザースクリプトのバンドル・開発環境。 |
