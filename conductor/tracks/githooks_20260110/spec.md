# Specification: Git Hooks Integration (Husky)

## 1. Overview
このトラックでは、Huskyを使用したGit Hooksを導入し、開発プロセスにおける品質管理を自動化します。開発の軽快さを損なわないよう、フックの実行タイミングを適切に分離し、Kotlin（NeoForge）とRust（Wasm）のマルチ言語環境に対応します。

## 2. Functional Requirements
### 2.1 Git Hook Management
- **Husky** を導入し、プロジェクトの `.git/hooks` を自動的に管理します。
- `npm` を使用して依存関係を管理します（`package.json` の作成）。

### 2.2 Hook Strategy
処理の重さに応じて実行タイミングを分離します。

#### A. pre-commit Hook (Lightweight & Formatting)
コミット直前に実行。コードスタイルの統一と基本的な静的解析を行います。
- **Kotlin:** `ktlintFormat` を実行し、修正されたファイルをステージングします。
- **Rust:** `cargo fmt` および `cargo clippy`（Lint）を実行します。

#### B. pre-push Hook (Verification & Stability)
リモートリポジトリへ送信する直前に実行。壊れたコードの共有を防ぎます。
- **Kotlin:** ユニットテスト (`./gradlew test`) およびビルド (`./gradlew build`) を実行します。
- **Rust:** ユニットテスト (`cargo test`) および Wasm ビルド (`cargo build --target wasm32-unknown-unknown`) を実行します。

### 2.3 Failure Handling
- **pre-commit:** フォーマットやLintに失敗した場合、コミットを中止します。
- **pre-push:** テストやビルドに失敗した場合、プッシュを中止します。

## 3. Technical Implementation
- **Manager:** Husky (v9+)
- **Lint-staged:** `pre-commit` 時に変更されたファイルのみを対象にフォーマットを行うため、`lint-staged` も導入を検討します（ただし、Gradle/Cargoとの兼ね合いで全チェックになる可能性も許容）。
- **Scripts:** `package.json` に `lint`, `format`, `test`, `build` 等の統合コマンドを定義します。

## 4. Acceptance Criteria
- [ ] `git commit` 時に自動的にフォーマットとLintが走ること。
- [ ] `git push` 時に自動的にテストとビルドが走ること。
- [ ] エラーがある場合、それぞれの操作がブロックされること。
- [ ] `npm install` 実行時に Husky が自動的にインストールされること。

## 5. Out of Scope
- CI/CD パイプライン（GitHub Actions 等）の詳細設定。
