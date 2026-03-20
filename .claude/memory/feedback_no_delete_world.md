---
name: Never delete world/save files without permission
description: NEVER delete server-world.dat, server-players.dat, or any save/test data files without explicit user permission
type: feedback
---

NEVER delete or reset world save files (server-world.dat, server-players.dat, or similar) without explicit user permission.

**Why:** Deleting these files destroys accumulated test data that the user has built up over many sessions. This happened when world files were deleted to "start clean" during debugging — the user lost significant test data.

**How to apply:** Before ANY destructive file operation (rm, overwrite, reset) on data files in the project root, ALWAYS ask the user first. This applies to world saves, player data, configuration, and any non-code files that may contain user-generated state. If you need a clean world for testing, ask the user to confirm before deleting.
