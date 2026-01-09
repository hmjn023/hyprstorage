# Plan: Git Hooks Integration (Husky)

## Phase 1: Environment Setup & Husky Installation
- [x] Task: Initialize `package.json` if not present. [a081cf6]
- [x] Task: Install `husky` and `lint-staged` as devDependencies. [1498c35]
- [ ] Task: Initialize Husky (`npx husky init`) and configure `npm install` hook.
- [ ] Task: Conductor - User Manual Verification 'Environment Setup' (Protocol in workflow.md)

## Phase 2: Script Definition (Kotlin & Rust)
- [ ] Task: Add `ktlint` configuration to `build.gradle` (if missing) to support formatting tasks.
- [ ] Task: Define scripts in `package.json` for Kotlin operations (`lint:kotlin`, `format:kotlin`, `test:kotlin`, `build:kotlin`).
- [ ] Task: Define scripts in `package.json` for Rust operations (`lint:rust`, `format:rust`, `test:rust`, `build:rust`).
- [ ] Task: Define composite scripts (`lint:all`, `format:all`, `test:all`, `build:all`).
- [ ] Task: Conductor - User Manual Verification 'Script Definition' (Protocol in workflow.md)

## Phase 3: Hook Implementation
- [ ] Task: Create `.husky/pre-commit` hook to run `format:all` and `lint:all`.
    - *Note:* Implement logic to re-add formatted files to staging if `lint-staged` is used, or ensure format runs effectively.
- [ ] Task: Create `.husky/pre-push` hook to run `test:all` and `build:all`.
- [ ] Task: Conductor - User Manual Verification 'Hook Implementation' (Protocol in workflow.md)

## Phase 4: Validation
- [ ] Task: Verify `pre-commit` by attempting to commit unformatted code.
- [ ] Task: Verify `pre-push` by attempting to push (dry-run or actual) and ensuring tests run.
- [ ] Task: Conductor - User Manual Verification 'Validation' (Protocol in workflow.md)
