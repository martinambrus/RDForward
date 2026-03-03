# rd-e2e Implementation Plan (Layer 2: Visual Regression with Real Clients)

## Overview

Automated visual regression testing that launches real Minecraft client JARs, operates them programmatically via a Java agent, captures screenshots, and compares against baselines. Catches rendering bugs, chunk display issues, and UI regressions that protocol-level bot tests miss.

## Architecture: Two-JVM, Two-Module Design

### Why Two JVMs

- Alpha/Beta clients require Java 8 + LWJGL 2
- The build system and test orchestrator use Java 21+
- A ByteBuddy Java agent (-javaagent) injects into the client JVM without modifying client JARs
- The test orchestrator (JUnit 5) spawns client subprocesses and coordinates via file-based JSON

### Two New Modules

**rd-e2e-agent** (Java 8 source/target)
- ByteBuddy fat JAR with Premain-Class manifest
- Hooks into the client's tick loop to drive test scenarios
- Captures screenshots via glReadPixels on the render thread
- Writes status/results to JSON files in a temp directory

**rd-e2e** (Java 21 source/target)
- JUnit 5 test orchestrator
- Launches RDForward server + client subprocesses
- Monitors agent status via JSON file polling
- Compares screenshots against baselines using image-comparison library

## JSON Status Protocol (Agent <-> Orchestrator)

```json
{
  "state": "WAITING_FOR_WORLD|WAITING_FOR_PLAYER|RUNNING_SCENARIO|CAPTURING|COMPLETE|ERROR",
  "tick": 1234,
  "screenshots": ["login_world_loaded.png"],
  "error": null,
  "playerPosition": [8.5, 67.62, 8.5]
}
```

Agent writes to {statusDir}/status.json; orchestrator reads via StatusMonitor (polls every 200ms).
