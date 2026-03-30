# RDForward Project Instructions

## Memory Sync Rule

Whenever a new memory is created or an existing memory is updated in the auto-memory directory (`~/.claude/projects/.../memory/`), it MUST also be written or updated in the corresponding `.claude/memory/` file in this repository. This includes both `MEMORY.md` and all topic sub-files.

This ensures memories are portable with the repository. When a developer pulls new changes, they can sync the `.claude/memory/` files into their local auto-memory directory to keep all machines up to date.

The full project memory lives in `.claude/memory/MEMORY.md` and its referenced sub-files — not duplicated here in CLAUDE.md to avoid doubling the context window (since auto-memory is loaded automatically by Claude Code).

## Data File Protection

- NEVER delete or reset world save files (server-world.dat, server-players.dat, or similar) without explicit user permission. These contain accumulated test data built up over many sessions.
- Before ANY destructive file operation (rm, overwrite, reset) on non-code data files in the project root, ALWAYS ask the user first.

## Backwards Compatibility

When adding support for new protocol versions (MCPE, Netty, Alpha, or otherwise):
- ALWAYS test ALL previously supported versions after making changes. New version support must not break older versions.
- Version-specific code paths (packet formats, field sizes, flag meanings) can differ between ANY two versions — never assume adjacent versions share the same format without verifying.
- When adding version thresholds (e.g. `if (version >= V27)`), verify that ALL versions in the affected range behave the same way. A threshold that's correct for v27 may be wrong for v17.
- When touching shared code paths (packet dispatch, chunk sending, login sequence), re-test at least one client from each supported version range.

## Lazy Loading and Code Decoupling

Performance is a top priority. Protocol adapters, registries, and version-specific code must be loaded and executed only when actually needed:

- **Lazy-load infrastructure**: Bedrock block palettes, MCPE servers, packet registry overlays, block state mappers, and translation tables should NOT be loaded at startup. Defer initialization to the first client connection of that protocol type. Use JVM inner-class holders (thread-safe without synchronization) or synchronized lazy getters (double-checked locking with volatile) as appropriate.
- **Decouple version branches**: When multiple protocol versions share a handler method via cascading if-else chains on protocol version numbers, extract version-specific wire format logic into separate codec/strategy classes selected at connection time. Each session should only execute its own version's code path, never code for other versions.
- **No unnecessary processing**: If only Alpha clients are connected, zero MCPE or Bedrock code should execute. If only 1.8 clients are connected, no 1.21-specific packet registry overlays should be loaded. Broadcast methods that write to sessions of mixed versions must dispatch on the RECIPIENT's codec, not the sender's.
- **Existing pattern to follow**: `BedrockProtocolConstants` uses synchronized lazy getters as the reference pattern. `ProtocolDetectionHandler` dynamically reconfigures the Netty pipeline per-connection as the reference pattern for per-client protocol selection.

## Running the Server

To build and start the server for manual testing:

```bash
./gradlew buildAll
sleep infinity | java -jar rd-server/build/libs/rd-server-0.2.0-SNAPSHOT-all.jar 2>&1 | tee /tmp/rdforward-server.log &
```

- `sleep infinity |` keeps stdin open so the server's console reader doesn't EOF and exit.
- `tee /tmp/rdforward-server.log` lets you read logs later with `cat /tmp/rdforward-server.log`.
- To check if it's running: `ps aux | grep rd-server | grep -v grep`
- To stop it: `pkill -f "rd-server-0.2.0"`
- To read live output: `cat /tmp/rdforward-server.log`
- Ports: TCP 25565 (Java), UDP 19132 (MCPE/Bedrock)
- If you get `BindException: Address already in use`, kill the old process first and wait 2-3 seconds.

## E2E Test Rules

- NEVER run two Gradle test suites in parallel. They share the Gradle daemon and will conflict/kill each other. Always run test tasks sequentially — wait for one to complete before starting the next.
- BEFORE launching any Gradle test, ALWAYS check `ps aux | grep -E "testCrossVersion|testLWJGL3|testModern|e2e-xv-|GradleWorker" | grep -v grep` to verify no other test is already running. If one is, stop and ask the user before proceeding.
- NEVER run a full test suite when only a single test needs to verify a bug fix. Always find a way to run just the specific test in isolation (e.g. create a temporary focused test class, use `--tests` filtering, or add a temporary include to the Gradle task). Delete any temporary test infrastructure after verification.
- Do NOT start tests proactively. Only run tests when the user explicitly asks.
