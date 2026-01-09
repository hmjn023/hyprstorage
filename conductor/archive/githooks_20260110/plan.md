# Plan: Git Hooks Integration (Husky)

## Phase 1: Environment Setup & Husky Installation [checkpoint: 6223c61]
- [x] Task: Initialize `package.json` if not present. [a081cf6]
- [x] Task: Install `husky` and `lint-staged` as devDependencies. [1498c35]
- [x] Task: Initialize Husky (`npx husky init`) and configure `npm install` hook. [fc15b2b]
- [x] Task: Conductor - User Manual Verification 'Environment Setup' (Protocol in workflow.md)

## Phase 2: Script Definition (Kotlin & Rust) [checkpoint: c72d1d5]
- [x] Task: Add `ktlint` configuration to `build.gradle` (if missing) to support formatting tasks. [6c7a05e]
- [x] Task: Define scripts in `package.json` for Kotlin operations (`lint:kotlin`, `format:kotlin`, `test:kotlin`, `build:kotlin`). [8dc1c2b]
- [x] Task: Define scripts in `package.json` for Rust operations (`lint:rust`, `format:rust`, `test:rust`, `build:rust`). [0e8d45b]
- [x] Task: Define composite scripts (`lint:all`, `format:all`, `test:all`, `build:all`). [1e2748c]
- [x] Task: Conductor - User Manual Verification 'Script Definition' (Protocol in workflow.md)

## Phase 3: Hook Implementation [checkpoint: 92d62b3]
- [x] Task: Create `.husky/pre-commit` hook to run `format:all` and `lint:all`. [5008488]
    - *Note:* Implement logic to re-add formatted files to staging if `lint-staged` is used, or ensure format runs effectively.
- [x] Task: Create `.husky/pre-push` hook to run `test:all` and `build:all`. [f032a73]
- [x] Task: Conductor - User Manual Verification 'Hook Implementation' (Protocol in workflow.md)

## Phase 4: Validation [checkpoint: f4714db]
- [x] Task: Verify `pre-commit` by attempting to commit unformatted code.
- [x] Task: Verify `pre-push` by attempting to push (dry-run or actual) and ensuring tests run.
- [x] Task: Conductor - User Manual Verification 'Validation' (Protocol in workflow.md)
