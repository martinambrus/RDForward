---
name: Server start method
description: Use plain Bash & to start the RDForward server, never run_in_background
type: feedback
---

Always start the server using a plain Bash `&` background, exactly as documented in CLAUDE.md. Never use `run_in_background: true` for the server process.

**Why:** `run_in_background` wraps the command in a task runner that closes pipes when it finishes tracking, which kills the server process. The server needs `sleep infinity |` piped to stdin to keep the console reader alive, and `&` keeps it running in the shell session.

**How to apply:** When starting the server, use a single Bash tool call:
```bash
sleep infinity | java -jar rd-server/build/libs/rd-server-0.1.0-SNAPSHOT-all.jar 2>&1 | tee /tmp/rdforward-server.log &
```
Then wait a few seconds and check logs with `cat /tmp/rdforward-server.log`.
