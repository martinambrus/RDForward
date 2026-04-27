---
name: Build fat jar before asking user to test
description: Always run ./gradlew buildAll (not just per-module compileJava) before telling the user a server-side fix is ready to retest
type: feedback
---

Before signaling that a server-side change is ready for the user to test manually, ALWAYS run `./gradlew buildAll` so the shaded `rd-server-0.2.0-SNAPSHOT-all.jar` is up to date. Per-module tasks like `:rd-bridge-bukkit:compileJava` are not enough — the user launches the fat jar.

**Why:** Compiling a single module updates `build/classes/` but does not regenerate the shaded jar at `rd-server/build/libs/rd-server-0.2.0-SNAPSHOT-all.jar`, which is what the user launches with `java -jar ...`. Telling the user "retest now" after only compiling a module makes them run stale code and waste a round trip.

**How to apply:** After any code change in any module that ships inside the server fat jar (rd-api, rd-protocol, rd-world, rd-server, rd-bridge-*, rd-mod-loader), run `./gradlew buildAll` and confirm `rd-server-0.2.0-SNAPSHOT-all.jar` exists under `rd-server/build/libs/` before asking for a retest. Test-only changes (e.g. `src/test/java`) are exempt.
