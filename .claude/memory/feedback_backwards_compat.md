---
name: backwards-compatibility-testing
description: Always test all supported protocol versions after changes — new version support must not break older versions
type: feedback
---

When adding new protocol version support, ALWAYS verify backwards compatibility with ALL previously supported versions.

**Why:** Adding v34/v38 MCPE support introduced regressions in v14 (0.8.0) and v17 (0.9.0) — wrong UseItem face threshold, wrong AdventureSettings flags, broken block breaking. Version-specific wire formats, flag meanings, and packet fields can differ between ANY two adjacent versions.

**How to apply:**
- After any change to MCPE packet handling, test at least one client from each supported version range (v11-v13, v14, v17-v20, v27, v34, v38).
- When adding version thresholds (e.g. `if (version >= V27)`), verify the threshold is correct for ALL versions in the affected range — don't assume adjacent versions share formats.
- When touching shared code (packet dispatch, chunk sending, login sequence), re-test broadly.
- The user expects to be told about regressions proactively, not discover them during testing.
