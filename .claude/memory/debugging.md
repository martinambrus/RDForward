# Debugging & Test Workflow

## Run Only Failing Tests When Debugging
When a test fails during a full suite run, do NOT re-run the entire test suite to debug it. Instead, run only the specific failing test class or method:
```bash
./gradlew :rd-bot:test --tests "com.github.martinambrus.rdforward.bot.scenarios.ClassName.methodName"
```
Running the full suite for one failing test wastes minutes of time and provides no additional debugging value. Only run the full suite for final verification after all fixes are applied.

## Bot Forward Entry Limitations
- V735 (1.16) bot decoder does NOT handle SpawnPlayer packets — spawn detection tests won't work for v735. Chat and block placement tests work fine.
- V573 (1.15) has a KeepAlive forward entry bug — bot gets kicked before tests complete.
- V765 (1.20.3) has no PLAY state S2C forward entries — bot can't decode chat/blocks/entities.
