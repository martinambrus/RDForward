# E2E Visual Regression Testing

## Architecture
- Two modules: `rd-e2e-agent` (Java 8, ByteBuddy agent) + `rd-e2e` (Java 21, JUnit 5 orchestrator)
- Agent injected via `-javaagent:` into real Minecraft client JVM
- Communication: agent writes `status.json` atomically, orchestrator polls every 200ms
- Run: `./gradlew :rd-e2e:test -Pe2e` (skipped during normal builds)
- Tests run in ~10 seconds (not counting first-run JAR downloads)
- **Timeouts**: 120-150 minutes for pre-1.19 (`test` task), 120-150 minutes for 1.19+ (`testModern` task). 30 minutes is NOT enough.
- **Parallelism**: Pre-1.19 uses `availableProcessors()/2` (6 on 12-core). 1.19+ uses `maxParallelForks = 3` (heavier JVMs, ~800MB-1GB each).
- **Beta inventory resetInventory on close**: Intentional. Resets all slots to 1x64 cobblestone on inventory close. Required for consistent column_build replenishment. Do NOT remove.

## Alpha 1.2.6 Obfuscation (Critical)
Official Alpha 1.2.6 JAR uses **single-letter ProGuard obfuscation**, NOT SRG names.
SRG names (field_xxxx_y, func_xxxx_y) only exist in RetroMCP-deobfuscated JARs.

### Class hierarchy
- `lw` = Entity base class
- `hf` extends `lw` = EntityLiving
- `eb` extends `hf` = EntityPlayer (has `aO = 1.62f` eye height)
- `bq` extends `eb` = EntityPlayerBase (has Minecraft reference)
- `mp` extends `bq` = EntityPlayerSP (has multiplayer sync code)

### Verified field names (from decompiled official JAR)
| Human name | Obfuscated | Class | Type |
|---|---|---|---|
| posX | `aw` | lw (Entity) | double |
| posY | `ax` | lw (Entity) | double |
| posZ | `ay` | lw (Entity) | double |
| prevPosX | `at` | lw (Entity) | double |
| prevPosY | `au` | lw (Entity) | double |
| prevPosZ | `av` | lw (Entity) | double |
| yaw | `aC` | lw (Entity) | float |
| pitch | `aD` | lw (Entity) | float |
| onGround | `aH` | lw (Entity) | boolean |
| boundingBox | `aG` | lw (Entity) | co |
| eyeHeight | `aO` | lw (Entity) | float |
| thePlayer | `g` | Minecraft | bq |
| theWorld | `e` | Minecraft | cy |
| serverHost | `V` | Minecraft | String |
| serverPort | `W` | Minecraft | int |
| displayWidth | `c` | Minecraft | int |
| displayHeight | `d` | Minecraft | int |
| runTick | `i()` | Minecraft | method |
| run | `run()` | Minecraft | method (Runnable) |
| setPosition | `b(d,d,d)` | lw (Entity) | method |

| mouseGrabbed | `L` | Minecraft | boolean |
| GameSettings | `y` | Minecraft | gq |
| movementInput | `a` | bq (EntityPlayerSP) | nn |
| pressedKeys[] | `f` | he (MovementInputFromOptions) | boolean[10] |
| inventory | `e` | eb (EntityPlayer) | fo |
| mainInventory | `a` | fo (InventoryPlayer) | fp[37] |
| currentItem | `d` | fo (InventoryPlayer) | int |
| itemID | `c` | fp (ItemStack) | int |
| stackSize | `a` | fp (ItemStack) | int |
| itemDamage | `b` | fp (ItemStack) | int |
| getBlockId | `a(int,int,int)` | cy (World) | method -> int |
| clickHandler | `a(int)` | Minecraft (private) | method -> void |

**ItemStack (fp) field order**: `a`=stackSize, `b`=itemDamage, `c`=itemID, `d`=animationsToGo.
Plan/javap mappings were wrong for `a`/`b`; verified at runtime.

### Type-scan fallback pitfall
GameState's type-scan fallback finds fields by Java type. For Entity position fields,
it incorrectly matched `mp.bz` (prevX local to EntityPlayerSP) instead of `lw.aw` (actual posX)
because the scan hits the subclass first. Solution: use exact field names, not type fallback.

## ByteBuddy Agent Constraints
- **All shared fields MUST be `public static volatile`**: Advice is inlined into the target class (different package). Package-private causes IllegalAccessError.
- **Use `Advice`, not `MethodDelegation`**: Avoids classloader visibility issues with old Minecraft clients.
- **Type matcher**: `hasSuperType(named("net.minecraft.client.Minecraft"))` catches both Minecraft and its inner classes.

## RubyDung Modded Client (not yet working)
- Modded JAR uses `FabricNativeLauncher` -> `KnotClient.main()` (Fabric custom classloader)
- ByteBuddy agent + Fabric Knot classloader interaction not verified
- `RubyDungLoginScreenshotTest` is `@Disabled` until this is resolved

## Input Injection
- Movement: write directly to `he.f[]` boolean array via reflection (no KeyBinding.pressed in Alpha)
- Mouse clicks: call `Minecraft.a(int)` via reflection (0=left, 1=right)
- Look direction: set Entity.yaw/pitch directly
- **Click cooldown field `S`**: `Minecraft.S` (int). When >0, left-clicks silently return. Set to 10 when objectMouseOver is null. Reset to 0 before left-clicks.
- **Direct dig packets**: Use `PlayerController.b.a(int,int,int,int)` to bypass Minecraft.a(0) cooldown/objectMouseOver checks.
- **Block breaking gotcha**: Game loop calls `a(0, false)` every tick which aborts ongoing breaks when Mouse.isButtonDown(0) is false. Use direct dig packets instead of click(0) for reliable breaking.

## Scenarios (Phase 2)
- `world_loaded`: Single screenshot capture (default scenario)
- `environment_check`: Verifies spawn position, surroundings, inventory
- `block_place_break`: Break grass, place cobblestone, verify conversion, verify replenishment
- `column_build`: Build to near max height (Y≈128), attempt above limit, break back down
  - Uses GROUND/AIRBORNE state machine (not continuous jump+click)
  - 8-tick ground stabilization between jumps to prevent drift
  - First click at phaseTick=4 (not 2) for body overlap margin
  - BreakDownStep uses settle ticks after detecting grass to prevent over-breaking

## Server World Height
- `E2ETestServer` creates server with height=128 (Alpha world height), not default 64 (Classic)
- `ServerWorld.inBounds()` rejects Y >= height — was blocking placement above Y=63 with default height
- FlatWorldGenerator surfaceY = height * 2/3 = 85 (with height=128)

## MC 1.17+ Core Profile / Mesa llvmpipe

MC 1.17+ uses OpenGL Core Profile with a shader-based render pipeline. Mesa's llvmpipe
software renderer (used under Xvfb) cannot execute this pipeline — the JVM dies silently
(no crash dump, no shutdown hook, no Java exception) on the first frame that renders 3D
world geometry.

### Fixes applied (in McTestAgent):
1. **`-Dorg.lwjgl.system.allocator=system`**: LWJGL 3.2.1's bundled jemalloc (Dec 2018)
   crashes with SIGSEGV at `pc=0xc00000` on Java 17+. Bypass jemalloc via system malloc.
2. **`MESA_GL_VERSION_OVERRIDE=4.5COMPAT`**: Force Mesa to report GL 4.5 Compatibility Profile.
3. **GlfwWindowHintAdvice**: Intercepts `glfwWindowHint(GLFW_OPENGL_PROFILE, CORE)` and
   changes to `ANY_PROFILE`. Without this, Mesa refuses Core Profile context creation.
4. **GameRendererSkipAdvice**: Skips `GameRenderer.render(float, long, boolean)` entirely
   when `mctestagent.coreprofile` is set. This prevents 3D rendering while allowing
   `f(boolean)` to run normally for network processing and ticks.
   - Key insight: inside `f(boolean)`, the order is: network packets → ticks → render.
     The world is created by JoinGame network packet, and the render portion crashes on
     the SAME frame. Tick-based detection is insufficient because 0 ticks may run on that
     frame. Intercepting at GameRenderer level is the correct granularity.
5. **RenderLoopSafetyAdvice**: Still handles 1.14-1.16.x render exceptions (GL matrix
   stack cleanup). Core Profile skip logic was REMOVED — GameRendererSkipAdvice handles it.

### GameRenderer class names (per version):
- V755 (1.17): `ena`, V756 (1.17.1): `enb`, V757 (1.18): `epe`, V758 (1.18.2): `eql`

### Screenshots are black for 1.17+
Since all rendering is skipped, screenshots are solid black. Tests verify connectivity
and state machine progression, not visual output.

## Critical Rules
- **NEVER run 2 concurrent Gradle test runs.** They always conflict with each other (XML result files, daemon locks, etc.). Run one test suite at a time, wait for it to complete, then run the next.
- **ALWAYS use `fatJar` task** (not `jar`) when rebuilding the e2e agent. Tests use the `-all.jar` fat JAR.

## Screenshot Baselines
- ALWAYS read the scenario source code before assuming what a screenshot should look like. Do NOT guess from the screenshot file name alone.
- Example: void_fall_complete.png shows the player BACK at spawn (grass visible) after being teleported from the void, NOT the void itself.
- The `fatJar` task (not `jar`) must be used to rebuild the agent; tests use the `-all.jar` fat JAR.
- Screenshot baselines that differ may indicate rendering timing issues (flaky), not incorrect baselines.

## Netty Sub-Version Obfuscation
- Each Minecraft sub-version (1.7.3, 1.7.4, 1.7.5 etc.) has DIFFERENT ProGuard obfuscation. Class names AND field names can differ between sub-versions of the same protocol version.
- The auto-detection system (DelegatingFieldMappings) only handles 3 fields: minecraftClassName, runMethodName, tickMethodName. All other fields (posX, inventoryFieldName, creativeInventoryClassName, etc.) must come from the correct mapping file.
- Mapping groupings found by decompilation:
  - 1.7.2 (v4): NettyReleaseV4Mappings
  - 1.7.3/1.7.4 (v4): NettyReleaseV4_173Mappings (posX/Y/Z shifted, many fields differ)
  - 1.7.5 (v4): NettyReleaseV4_175Mappings (extends 1.7.3, class names differ)
  - 1.7.6/1.7.7 (v5): NettyReleaseV5Mappings
  - 1.7.8/1.7.9 (v5): NettyReleaseV5_178Mappings (creativeInventoryClassName=bfi)
  - 1.7.10 (v5): NettyReleaseV5_1710Mappings (creativeInventoryClassName=bfl)
  - 1.8.0 (v47): NettyReleaseV47Mappings
  - 1.8.1 (v47): NettyReleaseV47_181Mappings (15 fields differ)
  - 1.8.2/1.8.3 (v47): NettyReleaseV47_182Mappings (tickMethod=s, many fields differ)
  - 1.8.4-1.8.9 (v47): NettyReleaseV47_184Mappings (tickMethod=s, clickCooldown=ag)

## CFR Decompiler
- Download: `curl -fSL -o /tmp/cfr.jar "https://github.com/leibnitz27/cfr/releases/download/0.152/cfr-0.152.jar"`
- Usage: `jar xf client.jar ClassName.class && java -jar /tmp/cfr.jar ClassName.class`
- Must extract individual .class files first (CFR decompiles entire JAR otherwise)
