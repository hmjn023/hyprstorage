# Product Guidelines: HyperStorage

## Code Quality & Design Philosophy
*   **Modularity & Scalability:** Design modules (Core, Production, Transport) to be loosely coupled and independently scalable, anticipating future features like AE2 integration.
*   **Strict Type Safety:** Leverage Rust's and Kotlin's type systems to the fullest to catch errors at compile time, ensuring robustness.

## Branching & Commit Policy
*   **Feature Branch Workflow:** Develop all new features and fixes in dedicated branches. The `main` branch must always remain in a deployable state.
*   **Conventional Commits:** Adhere strictly to Conventional Commits (feat, fix, docs, chore, etc.) to facilitate automated changelog generation and clear history.

## Testing & Quality Assurance
*   **Test-Driven Development (TDD):** Write tests before implementation, especially for the critical Rust core logic, to ensure correctness from the start.
*   **CI/CD Integration:** Automated build and test pipelines on every push to prevent regression.
*   **Pre-commit Hooks:** Enforce quality gates locally. Commits are blocked unless Linting, Testing, and Building pass successfully.

## Documentation
*   **Documentation as Code:** Maintain all project documentation within the repository, treating it with the same care as source code.
*   **Inline Documentation:** Mandate Javadoc/Rustdoc style comments for all public APIs and complex logic to ensure the codebase remains maintainable and understandable.
