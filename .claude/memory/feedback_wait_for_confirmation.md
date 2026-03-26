---
name: Wait for user confirmation before drawing conclusions
description: Do not draw conclusions from timed checks or notifications when user input is needed - wait for explicit user confirmation first
type: feedback
---

When waiting for the user to test something (e.g. connecting a client, checking a result), do NOT draw conclusions from timed log checks or notifications. Wait for the user to explicitly confirm they have tried before analyzing results.

**Why:** The user may not have attempted the action yet, and premature analysis wastes time and leads to incorrect conclusions.

**How to apply:** After asking the user to test something, wait for their explicit "I tried" / result message before checking logs or suggesting fixes. Do not schedule timed checks and assume lack of data means a problem.
