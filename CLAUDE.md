# RDForward Project Instructions

## Memory Sync Rule

Whenever a new memory is created or an existing memory is updated in the auto-memory directory (`~/.claude/projects/.../memory/`), it MUST also be written or updated in the corresponding `.claude/memory/` file in this repository. This includes both `MEMORY.md` and all topic sub-files.

This ensures memories are portable with the repository. When a developer pulls new changes, they can sync the `.claude/memory/` files into their local auto-memory directory to keep all machines up to date.

The full project memory lives in `.claude/memory/MEMORY.md` and its referenced sub-files — not duplicated here in CLAUDE.md to avoid doubling the context window (since auto-memory is loaded automatically by Claude Code).

## E2E Test Rules

- NEVER run two Gradle test suites in parallel. They share the Gradle daemon and will conflict/kill each other. Always run test tasks sequentially — wait for one to complete before starting the next.
- NEVER run a full test suite when only a single test needs to verify a bug fix. Always find a way to run just the specific test in isolation (e.g. create a temporary focused test class, use `--tests` filtering, or add a temporary include to the Gradle task). Delete any temporary test infrastructure after verification.
