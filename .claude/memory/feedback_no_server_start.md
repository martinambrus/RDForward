---
name: Do not start the server without permission
description: Never start/restart the RDForward server unless the user explicitly or implicitly permits it
type: feedback
---

Do not start or restart the RDForward server process unless the user gives implicit or explicit permission.

**Why:** The user wants full control over when the server is running. Starting it proactively can interfere with their workflow or other processes.

**How to apply:** After building, do NOT automatically start the server. Only start it when the user says something like "start the server", "let's test it", "fire it up", or otherwise indicates they want it running. The same applies to restarts after rebuilds.
