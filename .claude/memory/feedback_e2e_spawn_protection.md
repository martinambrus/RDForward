---
name: E2E spawn protection fix
description: E2ETestServer disables spawn protection via server.properties - all E2E tests need this to break/place blocks at spawn
type: feedback
---

E2ETestServer writes `spawn-protection=0` to server.properties before starting. Without this, E2E agents can't break/place blocks within 16 blocks of spawn.

**Why:** Spawn protection was added to the server, defaulting to 16 blocks. E2E tests spawn players at world center which is within the protected area.

**How to apply:** If new E2E tests fail with "[SpawnProtection] Blocked" in logs, verify E2ETestServer is writing server.properties before start. No per-test changes needed — the fix is in E2ETestServer.start().
