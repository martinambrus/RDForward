---
name: Lazy Loading and Code Decoupling Principle
description: Protocol adapters, registries, and version-specific code must be lazy-loaded and decoupled per version
type: feedback
---

Lazy-load all protocol infrastructure and decouple version-specific code branches. No protocol adapter should be loaded until a client of that type actually connects. Version-specific if-else chains in shared handlers should be extracted into codec/strategy classes selected at connection time.

**Why:** Performance is a top priority alongside full client compatibility. If only Alpha clients are connected, MCPE/Bedrock code should never execute. If multiple MCPE versions share a handler method via cascading if-else, only the connecting version's code path should run. Eagerly loading all protocol versions wastes memory and startup time.

**How to apply:**
- Use JVM inner-class holders for static registries (thread-safe without synchronization)
- Use synchronized lazy getters (volatile + double-checked locking) for instance-level deferred init, following `BedrockProtocolConstants` as the reference pattern
- Extract version-specific wire format logic from shared methods into codec classes selected per-session at connection time
- Broadcast methods dispatching to mixed-version recipients must use the RECIPIENT's codec, not the sender's
- Applies to ALL protocol types: TCP, MCPE, Bedrock, and any future protocols
