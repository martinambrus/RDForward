---
name: Restart server after changes
description: User wants Claude to rebuild and restart the server proactively after code changes
type: feedback
---

When making code changes to the server, rebuild and restart the server without being asked.

**Why:** User explicitly asked "take charge of restarting and rebuilding the server after any changes please" — they don't want to have to ask each time.

**How to apply:** After any code change that affects rd-server, run `./gradlew buildAll`, kill the running server process, and restart it. Check startup logs to confirm it's running.
