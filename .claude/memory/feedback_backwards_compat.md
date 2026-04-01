---
name: backwards-compatibility-highest-priority
description: Never break existing clients — this is the single highest priority constraint in the project, above clean code and new features
type: feedback
---

Not breaking existing clients is the HIGHEST PRIORITY constraint. Every code change — adding new protocol versions, refactoring shared code, or fixing bugs — MUST preserve full compatibility with ALL previously supported clients. This takes precedence over clean code, performance, and new features.

**Why:** The project supports many protocol versions simultaneously (RubyDung, Classic 0.0.15a, Classic v7, Alpha v1-v6, Beta, Netty 1.7-1.21+, MCPE, Bedrock). Changes to shared code paths have repeatedly caused regressions in other versions (e.g. wrong MCPE flags, broken block formats, world serialization order differences between Classic versions).

**How to apply:**
- If a shared method is used by multiple protocol versions, NEVER change its behavior globally. Add a version parameter and branch, or create a version-specific override.
- Before editing any shared code, trace ALL callers to verify which clients are affected.
- Guard new behavior behind version checks so only the targeted client type is affected.
- After any change to packet handling, test at least one client from each supported version range.
- When adding version thresholds (e.g. `if (version >= V27)`), verify the threshold is correct for ALL versions in the affected range.
- The user expects to be told about regressions proactively, not discover them during testing.
