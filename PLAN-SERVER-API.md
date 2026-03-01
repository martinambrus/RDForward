# RDForward Unified Modding API - Architecture Plan

## Context

RDForward is a Minecraft-compatible server + client supporting every protocol version from RubyDung (2009) through 1.21.9+ and Bedrock Edition. The server already has basic extension points (events, commands, scheduler, config) but lacks a formal mod loading system, API abstraction layer, or compatibility with existing modding ecosystems.

**Goal**: Create a custom "RDForward API" that provides a unified modding experience, then build bridge layers so mods written for Fabric, Bukkit/Spigot, Forge, and eventually PocketMine can run on our server/client with minimal changes. A mod written for any version must work across all versions (features gracefully no-op when unavailable).

**Key design decisions**:
- Manifest format: JSON primary (`rdmod.json`), with YAML and TOML also accepted (internally converted)
- Package namespace: `com.github.martinambrus.rdforward.api`
- First bridges: Bukkit/Spigot + Fabric (Forge and PocketMine planned later)
- Classloading: Parent-child hierarchy (API in parent, each mod in child)
- Hot-reloading: First-class support with zero memory leaks (addressing Spigot's critical weakness)

---

## Table of Contents

1. [Phase 1: Core RDForward API (`rd-api`)](#phase-1-core-rdforward-api-rd-api-module)
2. [Phase 2: Mod Loader (`rd-mod-loader`)](#phase-2-mod-loader-rd-mod-loader-module)
3. [Phase 3: Hot-Reload Architecture](#phase-3-hot-reload-architecture)
4. [Phase 4: Event Bubbling, Cancellation, and Admin Event Manager](#phase-4-event-bubbling-cancellation-and-admin-event-manager)
5. [Phase 5: Command Conflict Resolution](#phase-5-command-conflict-resolution)
6. [Phase 6: Version Abstraction Layer](#phase-6-version-abstraction-layer)
7. [Phase 7: Bukkit/Spigot Bridge](#phase-7-bukkitspigot-bridge-rd-bridge-bukkit-module)
8. [Phase 8: Fabric Bridge](#phase-8-fabric-bridge-rd-bridge-fabric-module)
9. [Phase 9: Future Bridges](#phase-9-future-bridges)
10. [Module Dependency Graph](#module-dependency-graph)
11. [Implementation Order](#implementation-order)
12. [Critical Files](#critical-files-to-modify)
13. [Verification Plan](#verification-plan)

---

## Phase 1: Core RDForward API (`rd-api` module)

A new Gradle module containing **only interfaces, abstract classes, and value types** — no implementation. This is what modders compile against. It has ZERO dependencies on `rd-server`, `rd-protocol`, or any server internals.

### 1.1 Module structure

```
rd-api/
  build.gradle          # java-library, no internal deps
  src/main/java/com/github/martinambrus/rdforward/api/
    RDForward.java                    # Static entry point (like Bukkit.getServer())
    mod/
      Mod.java                        # Interface: mod lifecycle
      ModDescriptor.java              # Parsed mod manifest (id, name, version, entrypoints, deps)
      ModDescriptorParser.java        # Parses JSON/YAML/TOML -> ModDescriptor
      ServerMod.java                  # Server-side mod entrypoint interface
      ClientMod.java                  # Client-side mod entrypoint interface
      UniversalMod.java               # Both server + client
      Reloadable.java                 # Optional interface: mod supports hot-reload
    event/
      Event.java                      # Keep current Event<T> pattern (moved from rd-protocol)
      EventResult.java                # PASS / SUCCESS / FAIL (moved from rd-protocol)
      Cancellable.java                # Marker interface for cancellable events
      EventPriority.java              # LOWEST, LOW, NORMAL, HIGH, HIGHEST, MONITOR
      PrioritizedEvent.java           # Event<T> with priority, stop-on-cancel, MONITOR pass-through
      EventOwnership.java             # Tracks which mod registered which listener
      ListenerInfo.java               # Record: modId, priority, listenerClass (for admin tooling)
    server/
      Server.java                     # Interface: worlds, players, scheduler, etc.
      ServerEvents.java               # All server event definitions
    world/
      World.java                      # Interface: getBlock(), setBlock(), dimensions
      Block.java                      # Interface: type, position, world
      BlockType.java                  # Enum/registry entry for block types
      Location.java                   # Value type: world + x,y,z + yaw,pitch
      WorldEvents.java                # Block break/place, world save
    player/
      Player.java                     # Interface: name, location, messages, version
      PlayerEvents.java               # Join, leave, move, chat
    command/
      Command.java                    # Interface: execute(CommandContext)
      CommandContext.java              # Sender, args, reply
      TabCompleter.java               # Interface for tab completion suggestions
      CommandRegistry.java            # Interface for command registration
    scheduler/
      Scheduler.java                  # Interface: runLater(), runRepeating(), cancelAll()
      ScheduledTask.java              # Handle for cancellation
    config/
      ModConfig.java                  # Interface: getString(), getInt(), setDefault(), load(), save()
    permission/
      PermissionManager.java          # Interface: hasPermission(), isOp()
    network/
      PluginChannel.java              # Custom payload channels between server/client
      PluginMessage.java              # Raw byte payload
    version/
      ProtocolVersion.java            # Enum exposing version info to mods
      VersionCapability.java          # Enum of features (WEATHER, TAB_LIST, etc.)
      VersionSupport.java             # Static: isSupported(capability, version)
    client/
      ClientEvents.java               # Key press, render frame, connect/disconnect
      KeyBinding.java                  # Key binding definition
      GameOverlay.java                 # HUD overlay interface
    registry/
      Registry.java                   # Generic typed registry (name -> value)
      RegistryKey.java                # Namespaced key ("modid:name")
```

### 1.2 Key API interfaces

#### Mod lifecycle

```java
// Server-side mod entrypoint
public interface ServerMod {
    void onEnable(Server server);   // Called after mod loaded and ready
    void onDisable();               // Called on shutdown or mod unload
}

// Client-side mod entrypoint
public interface ClientMod {
    void onClientReady();           // Called when client render loop is ready
    void onClientStop();
}

// Universal mod (both sides)
public interface UniversalMod extends ServerMod, ClientMod {}

// Optional: mod explicitly supports hot-reload
public interface Reloadable {
    /**
     * Called during hot-reload BEFORE onDisable().
     * Return state that should persist across reload.
     * Return null if no state needs preserving.
     */
    Object onSaveState();

    /**
     * Called during hot-reload AFTER onEnable().
     * Receives the state returned by onSaveState(), or null on first load.
     */
    void onRestoreState(Object savedState);
}
```

#### Server facade

```java
public interface Server {
    World getWorld();
    Collection<? extends Player> getOnlinePlayers();
    Player getPlayer(String name);
    Scheduler getScheduler();
    CommandRegistry getCommandRegistry();
    PermissionManager getPermissionManager();
    ProtocolVersion[] getSupportedVersions();
    void broadcastMessage(String message);
    ModManager getModManager();  // For querying loaded mods
}
```

#### Player interface (version-aware)

```java
public interface Player {
    String getName();
    Location getLocation();
    void teleport(Location location);
    void sendMessage(String message);
    ProtocolVersion getProtocolVersion();
    boolean isOp();
    void kick(String reason);
    boolean supportsCapability(VersionCapability capability);

    // Version-safe methods that no-op on unsupported clients:
    default void sendActionBar(String message) {}
    default void setTabListHeader(String header) {}
    default void sendTitle(String title, String subtitle) {}
}
```

#### World interface

```java
public interface World {
    String getName();
    int getWidth();
    int getHeight();
    int getDepth();
    Block getBlockAt(int x, int y, int z);
    boolean setBlock(int x, int y, int z, BlockType type);
    boolean isInBounds(int x, int y, int z);
    long getTime();
    void setTime(long time);
}
```

#### Version capabilities

```java
public enum VersionCapability {
    BLOCK_PLACEMENT,    // All versions
    CHAT,               // All versions
    WEATHER,            // Beta 1.5+
    TIME_OF_DAY,        // Alpha 1.2.0+
    TAB_LIST,           // Beta 1.8+
    CUSTOM_CHANNELS,    // 1.3.1+
    CREATIVE_MODE,      // Classic + Alpha 1.0.x (partial) + Beta 1.8+
    ACTION_BAR,         // 1.8+
    TITLE_SCREEN,       // 1.8+
    BOSS_BAR,           // 1.9+
    // ... extensible as server gains features
}

public final class VersionSupport {
    /** Check if a version supports a capability. */
    public static boolean isSupported(VersionCapability cap, ProtocolVersion version) { ... }
}
```

#### Event ownership tracking

```java
/**
 * Tracks which mod registered which event listener.
 * Critical for clean hot-reload: when a mod is unloaded,
 * all its listeners are automatically deregistered.
 */
public final class EventOwnership {
    // Maps (Event<?>, listener) -> modId
    // When a mod is disabled/reloaded, all its registrations are removed
}
```

### 1.3 Mod descriptor format: `rdmod.json`

Primary format is JSON. The mod loader also accepts `rdmod.yml` and `rdmod.toml`, converting them internally.

```json
{
  "id": "my-cool-mod",
  "name": "My Cool Mod",
  "version": "1.0.0",
  "description": "A sample mod for RDForward",
  "authors": ["AuthorName"],
  "api_version": "1.0",
  "entrypoints": {
    "server": "com.example.MyServerMod",
    "client": "com.example.MyClientMod"
  },
  "dependencies": {
    "rdforward": ">=1.0"
  },
  "soft_dependencies": {
    "some-optional-mod": ">=2.0"
  },
  "permissions": ["my-cool-mod.admin", "my-cool-mod.use"],
  "reloadable": true,
  "min_protocol": "RUBYDUNG",
  "max_protocol": "LATEST"
}
```

---

## Phase 2: Mod Loader (`rd-mod-loader` module)

Implementation module that discovers, loads, and manages mods at runtime.

### 2.1 Module structure

```
rd-mod-loader/
  build.gradle
  src/main/java/com/github/martinambrus/rdforward/modloader/
    ModLoader.java              # Scans mods/ directory, parses descriptors, loads JARs
    ModClassLoader.java         # Parent-child classloader per mod
    ModContainer.java           # Runtime wrapper: descriptor + classloader + mod instance + state
    ModManager.java             # Lifecycle: enable/disable/reload, dependency resolution
    ModState.java               # Enum: DISCOVERED, LOADING, ENABLED, DISABLING, DISABLED, ERROR
    DescriptorParser.java       # JSON + YAML + TOML -> ModDescriptor
    DependencyResolver.java     # Topological sort for load order
    admin/
      EventManager.java         # Admin event inspection + priority/position overrides
      CommandConflictResolver.java  # Detects command conflicts, manages assignments
      EventOverrideConfig.java  # Persists event-overrides.json
      CommandOverrideConfig.java # Persists command-overrides.json
    impl/
      RDServer.java             # Server implements com.github.martinambrus.rdforward.api.server.Server
      RDPlayer.java             # Wraps ConnectedPlayer as api.player.Player
      RDWorld.java              # Wraps ServerWorld as api.world.World
      RDBlock.java              # Wraps block state lookup as api.world.Block
      RDScheduler.java          # Wraps existing Scheduler as api.scheduler.Scheduler
      RDCommandRegistry.java    # Wraps existing CommandRegistry + namespaced commands + conflict resolution
      RDPermissionManager.java  # Wraps existing PermissionManager
```

### 2.2 Classloading architecture

```
Bootstrap ClassLoader
  +-- System ClassLoader (JDK + Gradle runtime)
        +-- Server ClassLoader (rd-server, rd-protocol, rd-world internals)
        |     +-- API ClassLoader (rd-api interfaces only)
        |           +-- Mod A ClassLoader (mod-a.jar)
        |           +-- Mod B ClassLoader (mod-b.jar, can see Mod A if dependency declared)
        |           +-- Mod C ClassLoader (mod-c.jar)
        +-- Bridge ClassLoaders (rd-bridge-bukkit stubs, rd-bridge-fabric stubs)
```

- **API ClassLoader**: Contains `rd-api` interfaces. All mods see these.
- **Mod ClassLoader**: Each mod gets its own `URLClassLoader` with parent = API ClassLoader.
- **Inter-mod visibility**: If mod B declares `"dependencies": {"mod-a": ">=1.0"}`, mod B's classloader can delegate to mod A's classloader for shared classes.
- **Implementation hiding**: `rd-server`, `rd-protocol`, `rd-world` are NOT on any mod classloader. Mods interact with the server only through `rd-api` interfaces.

### 2.3 Mod loading sequence

```
1. Server starts, initializes ServerWorld, PlayerManager, etc. (existing code)
2. ModLoader.discoverMods("mods/")
   a. Scan for *.jar files
   b. For each JAR: look for rdmod.json, rdmod.yml, rdmod.toml, plugin.yml, fabric.mod.json
   c. Parse descriptor -> ModDescriptor
   d. Create ModContainer(descriptor, jarPath, state=DISCOVERED)
3. DependencyResolver.resolve(containers)
   a. Build dependency graph
   b. Topological sort (deterministic: alphabetical within same depth)
   c. Fail with clear error on circular dependencies
4. For each ModContainer in dependency order:
   a. state = LOADING
   b. Create ModClassLoader(jarPath, parent=apiClassLoader)
   c. Load entrypoint class via classloader
   d. Instantiate mod (no-arg constructor)
   e. Call onEnable(serverFacade)
   f. state = ENABLED
   g. Log: "[ModLoader] Enabled my-cool-mod v1.0.0"
5. Server begins accepting connections
6. On shutdown (or /reload):
   a. For each mod in REVERSE dependency order:
      i.  state = DISABLING
      ii. Call onDisable()
      iii. Deregister all events owned by this mod (EventOwnership)
      iv. Deregister all commands owned by this mod
      v.  Cancel all scheduled tasks owned by this mod
      vi. Close ModClassLoader (releases JAR file handles)
      vii. Null out all references to mod instance
      viii. state = DISABLED
   b. System.gc() hint (not relied upon, but helps)
```

### 2.4 ModContainer state machine

```
DISCOVERED --> LOADING --> ENABLED --> DISABLING --> DISABLED
                  |                                     |
                  +---------> ERROR <-------------------+
                                |
                           (stays in ERROR, logged)
```

---

## Phase 3: Hot-Reload Architecture

This is a first-class feature designed to completely avoid the memory leaks and orphaned state that plague Bukkit/Spigot plugin reloading.

### 3.1 The Spigot problem

Spigot's `/reload` is notorious for:
1. **ClassLoader leaks**: Old plugin classloaders stay in memory because something still references a class they loaded (static fields, thread locals, cached Method/Constructor objects, lambda captures).
2. **Orphaned listeners**: Event handlers from the old plugin instance continue firing alongside new ones.
3. **Orphaned schedulers**: Old repeating tasks keep running.
4. **Orphaned commands**: Old command handlers remain registered.
5. **Thread leaks**: Plugins that start threads don't always stop them.
6. **Static state accumulation**: Singletons and static caches from old loads persist.

### 3.2 Our solution: Ownership tracking + forced cleanup

Every resource a mod creates is tracked by owner (mod ID). On unload, ALL resources owned by that mod are forcibly cleaned up — the mod doesn't need to do it manually (though it can via `onDisable()`).

#### 3.2.1 Event ownership

```java
// When a mod registers an event listener, the mod loader wraps the call:
public class OwnedEvent<T> extends Event<T> {
    // Maps listener -> owning modId
    private final Map<T, String> listenerOwners = new ConcurrentHashMap<>();

    @Override
    public void register(T listener) {
        String currentMod = ModLoader.getCurrentModId(); // thread-local during mod init
        listenerOwners.put(listener, currentMod);
        super.register(listener);
    }

    /**
     * Remove ALL listeners owned by the given mod.
     * Called during mod unload/reload.
     */
    public void removeListenersOwnedBy(String modId) {
        List<T> toRemove = listenerOwners.entrySet().stream()
            .filter(e -> e.getValue().equals(modId))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        toRemove.forEach(listener -> {
            listenerOwners.remove(listener);
            handlers.remove(listener);  // Remove from CopyOnWriteArrayList
        });
        rebuildInvoker();
    }
}
```

#### 3.2.2 Command ownership

```java
// CommandRegistry tracks which mod registered which command
public class OwnedCommandRegistry {
    private final Map<String, String> commandOwners = new ConcurrentHashMap<>();

    public void register(String name, String modId, Command handler) { ... }

    /** Remove all commands owned by a mod. */
    public void removeCommandsOwnedBy(String modId) {
        commandOwners.entrySet().removeIf(e -> {
            if (e.getValue().equals(modId)) {
                commands.remove(e.getKey());
                return true;
            }
            return false;
        });
    }
}
```

#### 3.2.3 Scheduler ownership

```java
// Scheduler tracks which mod created which task
public class OwnedScheduler {
    private final Map<ScheduledTask, String> taskOwners = new ConcurrentHashMap<>();

    public ScheduledTask runLater(String modId, int delay, Runnable task) {
        ScheduledTask st = super.runLater(delay, task);
        taskOwners.put(st, modId);
        return st;
    }

    /** Cancel all tasks owned by a mod. */
    public void cancelTasksOwnedBy(String modId) {
        taskOwners.entrySet().removeIf(e -> {
            if (e.getValue().equals(modId)) {
                e.getKey().cancel();
                return true;
            }
            return false;
        });
    }
}
```

#### 3.2.4 ClassLoader cleanup

```java
public class ModClassLoader extends URLClassLoader {
    private final String modId;
    private volatile boolean closed = false;

    @Override
    public void close() throws IOException {
        closed = true;
        // Clear any class cache
        // Release JAR file handles
        super.close();
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (closed) {
            throw new ClassNotFoundException("ModClassLoader for " + modId + " is closed");
        }
        return super.loadClass(name);
    }
}
```

#### 3.2.5 Thread tracking

```java
/**
 * Tracks threads spawned by mods. On unload, any threads still alive
 * are interrupted and given 5 seconds to terminate before being
 * forcibly stopped (Thread.stop() as last resort, with warning).
 */
public class ModThreadTracker {
    private final Map<Thread, String> threadOwners = new ConcurrentHashMap<>();

    /** Wrap Thread creation to track ownership. */
    public Thread createThread(String modId, Runnable task, String name) {
        Thread t = new Thread(task, modId + "/" + name);
        t.setDaemon(true);  // Don't prevent JVM shutdown
        threadOwners.put(t, modId);
        return t;
    }

    /** Interrupt and join all threads owned by a mod. */
    public void stopThreadsOwnedBy(String modId) {
        List<Thread> modThreads = threadOwners.entrySet().stream()
            .filter(e -> e.getValue().equals(modId))
            .map(Map.Entry::getKey)
            .filter(Thread::isAlive)
            .collect(Collectors.toList());

        // Phase 1: interrupt
        modThreads.forEach(Thread::interrupt);

        // Phase 2: wait up to 5 seconds
        for (Thread t : modThreads) {
            try {
                t.join(5000);
            } catch (InterruptedException ignored) {}
            if (t.isAlive()) {
                System.err.println("[ModLoader] WARNING: Thread '" + t.getName()
                    + "' from mod " + modId + " did not terminate within 5s");
            }
        }

        modThreads.forEach(t -> threadOwners.remove(t));
    }
}
```

### 3.3 Full reload sequence

When `/reload <modId>` (or `/reload all`) is executed:

```
1. PRE-RELOAD
   a. If mod implements Reloadable: savedState = mod.onSaveState()
   b. Log: "[ModLoader] Reloading my-cool-mod..."

2. DISABLE (forced cleanup)
   a. mod.onDisable()                              // Let mod clean up voluntarily
   b. eventSystem.removeListenersOwnedBy(modId)    // Force-remove any forgotten listeners
   c. commandRegistry.removeCommandsOwnedBy(modId) // Force-remove commands
   d. scheduler.cancelTasksOwnedBy(modId)           // Force-cancel scheduled tasks
   e. threadTracker.stopThreadsOwnedBy(modId)       // Force-stop threads
   f. pluginChannels.removeChannelsOwnedBy(modId)   // Force-remove network channels
   g. modClassLoader.close()                         // Release JAR handles + class cache
   h. modContainer.setInstance(null)                  // Drop reference to mod instance
   i. modContainer.setClassLoader(null)               // Drop reference to classloader
   j. Log resource cleanup stats: "Removed 3 listeners, 1 command, 2 tasks"

3. GC ASSIST
   a. System.gc()  // Hint only — NOT relied upon
   b. WeakReference probe: create a WeakReference to the old classloader.
      After gc(), check if it was collected. Log warning if not:
      "[ModLoader] WARNING: ClassLoader for my-cool-mod was not garbage collected.
       This may indicate a memory leak (static references, thread locals, etc.)"

4. RE-LOAD
   a. Re-read mod JAR (may have been replaced on disk)
   b. Re-parse descriptor
   c. Create fresh ModClassLoader
   d. Load and instantiate new mod class
   e. mod.onEnable(server)
   f. If mod implements Reloadable and savedState != null:
      mod.onRestoreState(savedState)
   g. Log: "[ModLoader] Reloaded my-cool-mod v1.0.1 (was v1.0.0)"

5. POST-RELOAD VERIFICATION
   a. Check that no listeners from the old instance remain
   b. Check that no commands from the old instance remain
   c. Check that no tasks from the old instance remain
   d. If any orphans found, log ERROR (this is a bug in our cleanup logic)
```

### 3.4 ModLoader thread-local context

During mod initialization, `ModLoader` sets a thread-local with the current mod ID. All API calls that create resources (register events, commands, tasks) read this thread-local to automatically tag ownership:

```java
public class ModLoader {
    private static final ThreadLocal<String> currentModId = new ThreadLocal<>();

    static void enableMod(ModContainer container) {
        currentModId.set(container.getDescriptor().getId());
        try {
            container.getInstance().onEnable(serverFacade);
        } finally {
            currentModId.remove();
        }
    }

    /** Used by Event, CommandRegistry, Scheduler to tag ownership. */
    public static String getCurrentModId() {
        String id = currentModId.get();
        if (id == null) {
            // Called outside mod init context — attribute to "server" (core)
            return "__server__";
        }
        return id;
    }
}
```

### 3.5 Preventing common leak patterns

| Leak Source | Prevention |
|-------------|------------|
| Static fields in mod classes | ClassLoader disposal makes them unreachable; GC collects |
| ThreadLocal values | ModClassLoader.close() + thread interrupt clears thread locals |
| Cached reflection (Method, Field) | Held by mod classloader; disposed with it |
| Lambda captures referencing mod classes | Listener removal drops lambda references |
| Timer/ScheduledExecutorService | Thread tracking forces shutdown |
| Registered shutdown hooks | Tracked and removed on mod unload |
| Open file handles | ModClassLoader tracks FileInputStream/OutputStream opens |
| JDBC connections | Out of scope for initial impl, but trackable via connection pool wrapper |

### 3.6 Reload command

```
/reload <modId>     - Reload a single mod
/reload all         - Reload all mods (in dependency order)
/mods               - List loaded mods with state
/mod info <modId>   - Show mod details (version, deps, resource counts)
/mod disable <modId> - Disable without re-enabling
/mod enable <modId>  - Enable a disabled mod
```

---

## Phase 4: Event Bubbling, Cancellation, and Admin Event Manager

### 4.1 The Spigot cancellation problem

Spigot's event cancellation has a fundamental design flaw: when a plugin cancels an event, other plugins further down the priority chain **still receive it** unless they explicitly opt-in to respecting cancellation via `@EventHandler(ignoreCancelled = true)`. This means:
- A protection plugin cancels `BlockBreakEvent` in a protected region
- A logging plugin at NORMAL priority still receives the event and logs "Player broke a block" — even though the block was never actually broken
- Plugin authors must remember `ignoreCancelled = true` on every handler, and many forget

This is an intentional incompatibility with the Spigot API. Our system is strictly better — plugins that want the old behavior can always check `event.isCancelled()` themselves.

### 4.2 Our cancellation model: Stop-on-cancel with MONITOR pass-through

```
Event dispatch order (by priority):
  LOWEST  -> LOW  -> NORMAL  -> HIGH  -> HIGHEST  -> MONITOR

Rules:
1. When ANY listener returns EventResult.FAIL (cancel), the event is IMMEDIATELY
   cancelled and NO FURTHER listeners at LOWEST-HIGHEST priorities are called.
2. MONITOR listeners ALWAYS receive the event, regardless of cancellation state.
3. MONITOR listeners CANNOT modify the event or change its cancellation state.
   They receive a read-only view.
4. The cancelled state is available to MONITOR listeners via event.isCancelled().
```

### 4.3 Implementation: PrioritizedEvent

```java
public class PrioritizedEvent<T> {

    // Listeners grouped by priority, each with mod ownership
    private final Map<EventPriority, List<OwnedListener<T>>> listenersByPriority
        = new EnumMap<>(EventPriority.class);

    /**
     * Register a listener at the given priority.
     * MONITOR listeners receive a read-only wrapper of the callback type.
     */
    public void register(EventPriority priority, T listener) {
        String modId = ModLoader.getCurrentModId();
        listenersByPriority
            .computeIfAbsent(priority, k -> new CopyOnWriteArrayList<>())
            .add(new OwnedListener<>(listener, modId));
        rebuildInvoker();
    }

    /** Shorthand: register at NORMAL priority. */
    public void register(T listener) {
        register(EventPriority.NORMAL, listener);
    }

    /**
     * Fire a cancellable event through the priority chain.
     *
     * @param fireCallback  given a listener, calls the appropriate method and returns EventResult
     * @param monitorCallback  given a listener + cancelled flag, calls for read-only observation
     * @return final EventResult (PASS if not cancelled, FAIL if cancelled)
     */
    public EventResult fire(
            Function<T, EventResult> fireCallback,
            BiConsumer<T, Boolean> monitorCallback) {

        boolean cancelled = false;

        // Phase 1: LOWEST through HIGHEST — stop on cancel
        for (EventPriority priority : EventPriority.values()) {
            if (priority == EventPriority.MONITOR) continue;

            List<OwnedListener<T>> listeners = listenersByPriority.get(priority);
            if (listeners == null) continue;

            for (OwnedListener<T> owned : listeners) {
                if (cancelled) break;  // Stop dispatching after cancellation

                EventResult result = fireCallback.apply(owned.listener);
                if (result == EventResult.FAIL) {
                    cancelled = true;
                    // Log which mod cancelled it (useful for debugging):
                    // "[Events] BlockBreak cancelled by mod 'grief-prevention' at HIGH priority"
                }
            }

            if (cancelled) break;  // Don't continue to next priority level
        }

        // Phase 2: MONITOR — always fires, read-only, cannot change state
        List<OwnedListener<T>> monitors = listenersByPriority.get(EventPriority.MONITOR);
        if (monitors != null) {
            for (OwnedListener<T> owned : monitors) {
                try {
                    monitorCallback.accept(owned.listener, cancelled);
                } catch (Exception e) {
                    System.err.println("[Events] MONITOR listener error in mod '"
                        + owned.modId + "': " + e.getMessage());
                }
            }
        }

        return cancelled ? EventResult.FAIL : EventResult.PASS;
    }

    /**
     * Remove all listeners owned by the given mod (for hot-reload cleanup).
     */
    public void removeListenersOwnedBy(String modId) {
        for (List<OwnedListener<T>> listeners : listenersByPriority.values()) {
            listeners.removeIf(owned -> owned.modId.equals(modId));
        }
        rebuildInvoker();
    }

    /**
     * Get a snapshot of all registered listeners with their priorities and owning mods.
     * Used by the admin Event Manager tooling.
     */
    public List<ListenerInfo> getListenerInfo() {
        List<ListenerInfo> result = new ArrayList<>();
        for (Map.Entry<EventPriority, List<OwnedListener<T>>> entry
                : listenersByPriority.entrySet()) {
            for (OwnedListener<T> owned : entry.getValue()) {
                result.add(new ListenerInfo(
                    owned.modId, entry.getKey(), owned.listener.getClass().getName()));
            }
        }
        return result;
    }

    static class OwnedListener<T> {
        final T listener;
        final String modId;
        OwnedListener(T listener, String modId) {
            this.listener = listener;
            this.modId = modId;
        }
    }

    public record ListenerInfo(String modId, EventPriority priority, String listenerClass) {}
}
```

### 4.4 Bukkit bridge compatibility note

When a Bukkit plugin is loaded through our bridge, its `@EventHandler` methods are adapted:
- `@EventHandler(ignoreCancelled = true)` — maps naturally (our system stops on cancel anyway)
- `@EventHandler(ignoreCancelled = false)` — this is the Spigot default where plugins receive cancelled events. **In our system, this is intentionally NOT honored.** The Bukkit bridge logs a one-time warning:

```
[BukkitBridge] Note: Plugin 'OldPlugin' registers event handlers without ignoreCancelled=true.
In RDForward, cancelled events are NOT delivered to non-MONITOR listeners (unlike Spigot).
If this plugin needs to see cancelled events, it should use MONITOR priority.
```

This is a documented, intentional incompatibility. The vast majority of Bukkit plugins work correctly (and often better) with our cancellation model.

### 4.5 Admin Event Manager

A server-side administration tool that provides full visibility and control over the event system at runtime. Accessible via commands and optionally through a future web panel.

#### 4.5.1 Event inspection commands

```
/events list                        - List all event types that have listeners registered
/events info <eventType>            - Show all listeners for an event, grouped by priority
/events mod <modId>                 - Show all events that a specific mod listens to
```

Example output of `/events info BLOCK_BREAK`:
```
=== BLOCK_BREAK Listeners ===
Priority: LOWEST
  (none)
Priority: LOW
  1. [grief-prevention] GriefPreventionMod$$Lambda/0x00001234  (position: 1)
Priority: NORMAL
  2. [block-logger] BlockLoggerMod$$Lambda/0x00005678  (position: 2)
  3. [custom-drops] CustomDropsMod$$Lambda/0x00009abc  (position: 3)
Priority: HIGH
  (none)
Priority: HIGHEST
  (none)
Priority: MONITOR
  4. [analytics] AnalyticsMod$$Lambda/0x0000def0  (position: 4, read-only)
```

#### 4.5.2 Admin priority override commands

```
/events setpriority <eventType> <modId> <newPriority>
    Change which priority a mod's listener runs at for a specific event.
    Example: /events setpriority BLOCK_BREAK block-logger HIGH
    -> Moves block-logger's BLOCK_BREAK listener from NORMAL to HIGH

/events setposition <eventType> <modId> <position>
    Within the same priority level, change the execution order.
    Example: /events setposition BLOCK_BREAK custom-drops 1
    -> Moves custom-drops before grief-prevention within their priority group

/events reset <eventType>
    Reset all admin overrides for this event type back to defaults.

/events reset all
    Reset ALL admin overrides.
```

#### 4.5.3 Persistence: `config/event-overrides.json`

All admin event priority/position changes are persisted to `config/event-overrides.json`:

```json
{
  "version": 1,
  "overrides": {
    "BLOCK_BREAK": {
      "block-logger": {
        "original_priority": "NORMAL",
        "override_priority": "HIGH",
        "position_in_priority": 0
      }
    },
    "CHAT": {
      "profanity-filter": {
        "original_priority": "NORMAL",
        "override_priority": "LOW",
        "position_in_priority": 0
      }
    }
  }
}
```

#### 4.5.4 Startup reconciliation

On server startup, after all mods are loaded:

1. Load `config/event-overrides.json`
2. For each override entry:
   a. Check if the mod is still loaded (present in `ModManager`)
   b. If the mod is NOT found (JAR removed):
      - Remove ALL overrides referencing this mod
      - Log: `[EventManager] Removed event overrides for missing mod 'block-logger'`
   c. If the mod IS found but no longer listens to that event:
      - Remove that specific override
      - Log: `[EventManager] Removed stale BLOCK_BREAK override for 'block-logger' (no longer listens)`
   d. If the mod IS found and still listens:
      - Apply the priority/position override
      - Log: `[EventManager] Applied override: block-logger BLOCK_BREAK -> HIGH priority`
3. Save cleaned config back to disk

#### 4.5.5 Integration with hot-reload

When a mod is reloaded via `/reload <modId>`:
1. After the new mod instance registers its events, check `event-overrides.json`
2. If overrides exist for this mod, apply them to the newly registered listeners
3. If the reloaded mod no longer registers a listener that had an override, remove that override
4. Save updated config

---

## Phase 5: Command Conflict Resolution

### 5.1 The problem

Multiple mods may register the same command name. Example: both `essentials-mod` and `ban-manager-mod` register `/ban`. Without resolution, one silently overwrites the other, and the admin has no visibility or control.

### 5.2 Our solution: Namespaced commands + admin-controlled defaults

#### 5.2.1 All commands are always namespaced

Every command registered by a mod is ALWAYS available under its fully-qualified name:
```
/<modId>:<commandName>
```

Examples:
- `/essentials:ban` — always works, unambiguous
- `/ban-manager:ban` — always works, unambiguous

The "bare" command (`/ban`) is an alias that points to one of the namespaced commands.

#### 5.2.2 Conflict detection on startup

During mod loading, after all mods have registered their commands:

```
1. Scan all registered commands
2. Group by bare command name
3. For commands with NO conflict (only 1 mod registered it):
   - Bare name (/ban) points to that mod's implementation
   - Namespaced name (/essentials:ban) also works
4. For commands WITH conflict (2+ mods registered same name):
   a. Check config/command-overrides.json for a saved resolution
   b. If saved resolution exists AND the target mod is still present:
      - Apply it: /ban -> /essentials:ban
      - Log: "[Commands] /ban -> essentials (from config)"
   c. If NO saved resolution (or target mod missing):
      - Log WARNING to console with interactive prompt:
        "[Commands] CONFLICT: /ban is registered by: essentials, ban-manager"
        "[Commands] Type '/commands assign ban <modId>' to choose, or"
        "[Commands] the first-loaded mod (essentials) will be used by default"
      - Temporarily assign to alphabetically-first mod
```

#### 5.2.3 Admin resolution commands

```
/commands list                          - List all commands and which mod handles each
/commands conflicts                     - List only commands with conflicts
/commands info <command>                - Show which mod(s) registered this command
/commands assign <command> <modId>      - Set which mod handles the bare command
/commands reset <command>               - Reset to default (first-loaded mod)
/commands reset all                     - Reset all command assignments
```

Example `/commands conflicts` output:
```
=== Command Conflicts ===
/ban
  -> [essentials] (ACTIVE - admin assigned)
     /essentials:ban
  -> [ban-manager]
     /ban-manager:ban

/home
  -> [essentials] (ACTIVE - first loaded)
     /essentials:home
  -> [homes-plus]
     /homes-plus:home
```

Example `/commands assign ban ban-manager` output:
```
[Commands] /ban now points to ban-manager's implementation.
           /essentials:ban and /ban-manager:ban are both still available.
           This preference has been saved.
```

#### 5.2.4 Persistence: `config/command-overrides.json`

```json
{
  "version": 1,
  "assignments": {
    "ban": {
      "assigned_mod": "ban-manager",
      "reason": "admin_assigned",
      "all_providers": ["essentials", "ban-manager"]
    },
    "home": {
      "assigned_mod": "essentials",
      "reason": "first_loaded",
      "all_providers": ["essentials", "homes-plus"]
    }
  }
}
```

#### 5.2.5 Startup reconciliation

On server startup, after all mods are loaded:

1. Load `config/command-overrides.json`
2. For each assignment:
   a. Check if the assigned mod is still loaded
   b. If the assigned mod is **NOT found** (JAR removed):
      - Check if there are still 2+ providers for this command
      - If yes: Remove the old assignment, log a WARNING prompting admin to re-assign:
        ```
        [Commands] WARNING: /ban was assigned to 'ban-manager' which is no longer loaded.
        [Commands] Remaining providers: essentials
        [Commands] /ban now points to 'essentials'. Use '/commands assign ban <modId>' to change.
        ```
      - If only 1 provider remains: Auto-assign to it, remove override (no longer a conflict)
      - If 0 providers remain: Remove the override entirely
   c. If the assigned mod IS found:
      - Update `all_providers` list (other mods may have been added/removed)
      - Apply the assignment
3. For NEW conflicts (commands that now conflict but didn't before):
   - Log warning prompting admin
   - Default to alphabetically-first mod
4. Save cleaned config back to disk

#### 5.2.6 Integration with hot-reload

When a mod is reloaded:
1. After the new mod registers its commands, re-run conflict detection for its commands
2. If the reloaded mod no longer provides a command it previously did:
   - If that command had an override pointing to this mod: remove override, re-evaluate
3. If the reloaded mod adds a new command that conflicts: log warning
4. Save updated config

#### 5.2.7 Tab completion

Tab completion for bare commands uses the assigned mod's tab completer. Tab completion also suggests namespaced variants:
- Typing `/ban` shows completions from the active handler
- Typing `/ess` tab-completes to `/essentials:` and then shows all essentials commands
- Typing `/ban-` tab-completes to `/ban-manager:` and shows ban-manager commands

#### 5.2.8 Server-core commands

Built-in server commands (help, list, stop, kick, tp, etc.) are registered under the `__server__` mod ID. They can be overridden by mods just like any other command — the admin can always restore the default via `/commands assign <cmd> __server__`.

---

## Phase 6: Version Abstraction Layer

> (Previously Phase 4 — renumbered after inserting event and command conflict phases)

### 6.1 How version no-oping works

Every API method that depends on a version-specific feature checks the player's protocol version and silently no-ops if unsupported:

```java
// In RDPlayer (implementation, not visible to mods):
@Override
public void sendActionBar(String message) {
    if (!VersionSupport.isSupported(VersionCapability.ACTION_BAR,
            connectedPlayer.getProtocolVersion())) {
        return; // Silent no-op for pre-1.8 clients
    }
    // Build and send version-appropriate packet
}
```

### 6.2 Mod-side version queries

Mods can explicitly check capabilities for conditional behavior:

```java
@Override
public void onEnable(Server server) {
    server.getEvents().playerJoin().register((player) -> {
        if (player.supportsCapability(VersionCapability.TITLE_SCREEN)) {
            player.sendTitle("Welcome!", "Enjoy your stay");
        } else {
            player.sendMessage("Welcome!");
        }
    });
}
```

### 6.3 Extensible capabilities

`VersionCapability` is a registry, not a closed enum. Mods can register custom capabilities for features they add:

```java
VersionCapability MY_FEATURE = VersionCapability.register("mymod:custom_feature",
    v -> v.isAtLeast(ProtocolVersion.RELEASE_1_8));
```

### 6.4 Protocol-specific behavior in API implementations

The API implementation layer (in `rd-mod-loader/impl/`) handles all version branching. Mods never see packet classes or protocol details:

```java
// RDPlayer.teleport() handles all protocol versions internally:
@Override
public void teleport(Location loc) {
    // Delegates to PlayerManager.teleportPlayer() which already handles
    // Classic fixed-point, Alpha double-precision, Netty 1.7-1.21+, Bedrock
    playerManager.teleportPlayer(connectedPlayer, loc.getX(), loc.getY(), loc.getZ(),
        loc.getYaw(), loc.getPitch(), chunkManager);
}
```

---

## Phase 7: Bukkit/Spigot Bridge (`rd-bridge-bukkit` module)

### 7.1 Architecture

```
rd-bridge-bukkit/
  build.gradle        # depends on rd-api, provides org.bukkit.* stubs
  src/main/java/
    org/bukkit/         # Bukkit API stub classes
      Bukkit.java
      Server.java
      World.java
      Location.java
      Material.java
      block/Block.java
      entity/Player.java
      event/
        Event.java
        EventHandler.java
        EventPriority.java
        Listener.java
        block/BlockBreakEvent.java
        block/BlockPlaceEvent.java
        player/PlayerJoinEvent.java
        player/PlayerQuitEvent.java
        player/AsyncPlayerChatEvent.java
        player/PlayerMoveEvent.java
      command/
        Command.java
        CommandExecutor.java
        CommandSender.java
        TabCompleter.java
      plugin/
        Plugin.java
        JavaPlugin.java
        PluginDescriptionFile.java
        PluginManager.java
      scheduler/
        BukkitScheduler.java
        BukkitTask.java
        BukkitRunnable.java
      configuration/
        Configuration.java
        file/FileConfiguration.java
        file/YamlConfiguration.java
    com/github/martinambrus/rdforward/bridge/bukkit/
      BukkitBridge.java            # Main bridge: Bukkit calls -> RDForward API
      BukkitPluginLoader.java      # Loads plugin.yml-based plugins
      BukkitEventAdapter.java      # RDForward events -> Bukkit events with priority dispatch
      BukkitPlayerAdapter.java     # Wraps RDForward Player as Bukkit Player
      BukkitWorldAdapter.java      # Wraps RDForward World as Bukkit World
      BukkitSchedulerAdapter.java  # Wraps RDForward Scheduler as BukkitScheduler
      MaterialMapper.java          # Maps Material enum -> RDForward BlockType
```

### 7.2 How the bridge works

1. Bukkit plugins ship with `plugin.yml` containing `main: com.example.MyPlugin` extending `JavaPlugin`.
2. `BukkitPluginLoader` reads `plugin.yml`, creates a `ModDescriptor` equivalent.
3. The plugin is loaded through a child classloader that has both the Bukkit stubs and `rd-api` on its parent.
4. When the plugin calls `Bukkit.getServer()`, it gets a `BukkitBridge.serverInstance` which delegates every call to our `Server` interface.
5. When the plugin registers `@EventHandler` methods via `getServer().getPluginManager().registerEvents()`, `BukkitEventAdapter` uses reflection to find annotated methods, maps them to our event system with priority support.
6. When our server fires `ServerEvents.BLOCK_BREAK.invoker()...`, the adapter creates a `BlockBreakEvent`, dispatches it through all registered Bukkit listeners (respecting priority order), and checks cancellation.

### 7.3 Event priority mapping

```
Bukkit EventPriority     -> RDForward EventPriority
LOWEST                   -> LOWEST
LOW                      -> LOW
NORMAL                   -> NORMAL
HIGH                     -> HIGH
HIGHEST                  -> HIGHEST
MONITOR                  -> MONITOR
```

Direct 1:1 mapping. Our priority system is designed to match Bukkit's for this reason.

### 7.4 Initial scope

Only the Bukkit API subset that maps to our current server capabilities:

| Bukkit Feature | RDForward Equivalent | Status |
|----------------|---------------------|--------|
| `JavaPlugin.onEnable()/onDisable()` | `ServerMod.onEnable()/onDisable()` | Direct map |
| `BlockBreakEvent` | `ServerEvents.BLOCK_BREAK` | Direct map |
| `BlockPlaceEvent` | `ServerEvents.BLOCK_PLACE` | Direct map |
| `PlayerJoinEvent` | `ServerEvents.PLAYER_JOIN` | Direct map |
| `PlayerQuitEvent` | `ServerEvents.PLAYER_LEAVE` | Direct map |
| `AsyncPlayerChatEvent` | `ServerEvents.CHAT` | Direct map |
| `PlayerMoveEvent` | `ServerEvents.PLAYER_MOVE` | Direct map |
| `CommandExecutor` | `Command` interface | Direct map |
| `BukkitScheduler.runTaskLater()` | `Scheduler.runLater()` | Direct map |
| `BukkitScheduler.runTaskTimer()` | `Scheduler.runRepeating()` | Direct map |
| `World.getBlockAt()` | `World.getBlockAt()` | Direct map |
| `Player.sendMessage()` | `Player.sendMessage()` | Direct map |
| `Player.teleport()` | `Player.teleport()` | Direct map |
| `Player.kickPlayer()` | `Player.kick()` | Direct map |
| `Player.getInventory()` | Returns empty stub | Future |
| `Player.getHealth()` | Returns 20.0 stub | Future |

Methods beyond current capabilities return sensible defaults or no-op.

---

## Phase 8: Fabric Bridge (`rd-bridge-fabric` module)

### 8.1 Architecture

```
rd-bridge-fabric/
  build.gradle        # depends on rd-api, provides net.fabricmc.* stubs
  src/main/java/
    net/fabricmc/
      api/
        ModInitializer.java
        ClientModInitializer.java
        DedicatedServerModInitializer.java
      fabric/api/
        event/
          Event.java           # Near-1:1 mapping to our Event<T>!
          EventResult.java
        lifecycle/v1/
          ServerLifecycleEvents.java
          ServerTickEvents.java
        networking/v1/
          ServerPlayNetworking.java
          PayloadTypeRegistry.java
        command/v2/
          CommandRegistrationCallback.java
      loader/api/
        FabricLoader.java
        ModContainer.java
        entrypoint/EntrypointContainer.java
    com/github/martinambrus/rdforward/bridge/fabric/
      FabricBridge.java
      FabricModLoader.java      # Loads fabric.mod.json mods
      FabricEventAdapter.java   # Our events <-> Fabric events (mostly 1:1)
      FabricPlayerAdapter.java
```

### 8.2 Key insight: Near-1:1 event mapping

Our existing `Event<T>` in `rd-protocol` is already modeled on Fabric's event pattern:
- Both use `Event.create(emptyInvoker, invokerFactory)`
- Both use `event.register(listener)` and `event.invoker().method()`
- The bridge is nearly trivial — we mostly just re-export under `net.fabricmc.fabric.api.event.Event`

### 8.3 Initial scope

| Fabric Feature | RDForward Equivalent | Status |
|----------------|---------------------|--------|
| `ModInitializer.onInitialize()` | `ServerMod.onEnable()` | Direct map |
| `ClientModInitializer` | `ClientMod.onClientReady()` | Direct map |
| `ServerLifecycleEvents.SERVER_STARTED` | Server ready event | Direct map |
| `ServerTickEvents.END_SERVER_TICK` | `ServerEvents.SERVER_TICK` | Direct map |
| `CommandRegistrationCallback` | `CommandRegistry.register()` | Adapter |
| `Event<T>` | `Event<T>` | Near-identical |
| `FabricLoader.getInstance()` | `ModManager` queries | Adapter |

---

## Phase 9: Future Bridges

These are NOT implemented in the initial version but the architecture supports them.

### 9.1 Forge bridge (`rd-bridge-forge`)
- `@Mod` annotation scanning for mod discovery
- `MinecraftForge.EVENT_BUS` adapter backed by our event system
- `@SubscribeEvent` method discovery via reflection
- `IForgeRegistries` stubs mapping to our registry system
- Capability system adapter (`IItemHandler`, `IFluidHandler`, `IEnergyStorage`)
- `FMLCommonSetupEvent` / `FMLClientSetupEvent` lifecycle mapping

### 9.2 NeoForge bridge (`rd-bridge-neoforge`)
- NeoForge is a Forge fork with API changes
- Would extend the Forge bridge with NeoForge-specific differences
- Different event bus API, different registration patterns

### 9.3 Paper bridge (extends Bukkit bridge)
- Paper extends Bukkit with additional events and APIs
- Our Bukkit bridge would be extended with Paper-specific stubs
- `PaperConfig`, `AsyncChatEvent`, etc.

### 9.4 PocketMine bridge (`rd-bridge-pocketmine`)
- PocketMine is PHP-based, so direct code compatibility is impossible
- Options: a) Java API that mirrors PocketMine's patterns, b) GraalVM polyglot for PHP interop
- Most practical: Provide Java interfaces matching PocketMine event/plugin patterns
- Community can port popular PocketMine plugins to Java using familiar patterns

---

## Module Dependency Graph

```
rd-api  (interfaces only, ZERO deps on server internals)
  |
  +-- rd-mod-loader       (depends on rd-api + rd-server + rd-protocol + rd-world)
  |     |
  |     +-- rd-mod-loader/impl/   (API implementation: RDServer, RDPlayer, RDWorld, etc.)
  |
  +-- rd-bridge-bukkit    (depends on rd-api, provides org.bukkit.* stubs)
  |
  +-- rd-bridge-fabric    (depends on rd-api, provides net.fabricmc.* stubs)
  |
  +-- [future] rd-bridge-forge     (depends on rd-api)
  +-- [future] rd-bridge-neoforge  (depends on rd-bridge-forge)
  +-- [future] rd-bridge-pocketmine (depends on rd-api)

rd-server  (existing, depends on rd-protocol, rd-world)
  +-- Modified to call ModLoader during startup
```

### settings.gradle additions:
```gradle
include 'rd-api'
include 'rd-mod-loader'
include 'rd-bridge-bukkit'
include 'rd-bridge-fabric'
```

### rd-api/build.gradle:
```gradle
plugins { id 'java-library' }
description = 'RDForward Modding API - compile-time interfaces for mod development'
// ZERO dependencies on internal modules
```

### rd-mod-loader/build.gradle:
```gradle
plugins { id 'java-library' }
description = 'RDForward Mod Loader - runtime mod discovery, loading, and lifecycle'
dependencies {
    implementation project(':rd-api')
    implementation project(':rd-server')
    implementation project(':rd-protocol')
    implementation project(':rd-world')
    // YAML/TOML parsing for alternate descriptor formats
    implementation 'org.snakeyaml:snakeyaml-engine:2.7'
    implementation 'com.moandjiezana.toml:toml4j:0.7.2'
}
```

---

## Implementation Order

### Step 1: Create `rd-api` module
- Move `Event<T>` and `EventResult` from `rd-protocol` to `rd-api` (keep re-exports in rd-protocol for backward compat)
- Define all API interfaces: `Server`, `Player`, `World`, `Block`, `Location`, `BlockType`
- Define `ServerMod`, `ClientMod`, `Reloadable` lifecycle interfaces
- Define `ModDescriptor` and JSON parser
- Define `VersionCapability` and `VersionSupport`
- Define `EventPriority`, `PrioritizedEvent` (with stop-on-cancel + MONITOR pass-through), `EventOwnership`, `ListenerInfo`
- Define `OwnedEvent`, `OwnedCommandRegistry`, `OwnedScheduler` wrappers

### Step 2: Create `rd-mod-loader` module (core)
- Implement `ModClassLoader` (parent-child URLClassLoader)
- Implement `ModLoader` (JAR scanning, descriptor parsing)
- Implement `DependencyResolver` (topological sort)
- Implement `ModManager` (lifecycle state machine)
- Implement API bindings (`RDServer`, `RDPlayer`, `RDWorld`, etc.) wrapping existing classes
- Implement ownership tracking (events, commands, scheduler, threads)
- Implement hot-reload logic (Phase 3)
- Wire into `RDServer.java` startup: after world init, before connections
- Add `/reload`, `/mods`, `/mod` commands

### Step 3: Implement event bubbling and cancellation (Phase 4)
- Implement `PrioritizedEvent` with stop-on-cancel semantics
- LOWEST through HIGHEST: event stops propagating the moment any listener cancels
- MONITOR: always receives event (including cancelled ones), read-only, cannot modify
- Intentional Spigot incompatibility: cancelled events do NOT reach non-MONITOR listeners
- Wire all `ServerEvents` to use `PrioritizedEvent` instead of plain `Event`

### Step 4: Implement Admin Event Manager (Phase 4.5)
- Implement `EventManager`: introspection of all registered events and listeners
- Implement `/events list`, `/events info <type>`, `/events mod <modId>` commands
- Implement `/events setpriority`, `/events setposition`, `/events reset` commands
- Implement `EventOverrideConfig`: persist overrides to `config/event-overrides.json`
- Implement startup reconciliation: remove overrides for missing mods, apply for present mods
- Integrate with hot-reload: reapply overrides after mod reload

### Step 5: Implement Command Conflict Resolution (Phase 5)
- All mod commands registered as `<modId>:<command>` (namespaced) + bare alias
- Implement `CommandConflictResolver`: detect conflicts when 2+ mods register same command name
- Implement `/commands list`, `/commands conflicts`, `/commands info`, `/commands assign`, `/commands reset`
- Implement `CommandOverrideConfig`: persist assignments to `config/command-overrides.json`
- Implement startup reconciliation: handle removed mods, re-prompt for unresolved conflicts
- Server-core commands registered under `__server__` pseudo-mod, overridable by mods
- Tab completion: bare commands use assigned mod's completer; typing `/modid:` shows that mod's commands

### Step 6: Create a test mod
- Simple mod that listens for player join + block place events at different priorities
- Registers a `/hello` command (test conflict resolution with a second test mod that also has `/hello`)
- Uses scheduler for a repeating broadcast
- Implements `Reloadable` for hot-reload testing
- Verifies: event cancellation stops propagation, MONITOR still receives, priorities respected

### Step 7: Test hot-reload + admin tooling
- Load test mod -> verify events/commands work
- Use `/events info BLOCK_BREAK` to verify listener visibility
- Use `/events setpriority` to reorder listeners, verify change takes effect
- Replace JAR on disk -> `/reload test-mod` -> verify new version loads
- Verify old listeners/commands/tasks are cleaned up
- Verify event overrides reapplied after reload
- Verify no classloader leak (WeakReference probe)

### Step 8: Create `rd-bridge-bukkit` module
- Bukkit API stubs (minimal subset matching current capabilities)
- `BukkitBridge` adapter classes
- `BukkitPluginLoader` for `plugin.yml` discovery
- `BukkitEventAdapter` with priority dispatch and `@EventHandler` reflection
- Map Bukkit's `ignoreCancelled` to our stop-on-cancel model (with one-time warning log)
- Test with a simple Bukkit-style plugin

### Step 9: Create `rd-bridge-fabric` module
- Fabric API stubs (minimal subset)
- `FabricBridge` adapter classes
- `FabricModLoader` for `fabric.mod.json` discovery
- Verify near-1:1 event mapping
- Test with a simple Fabric-style mod

### Step 10: API-only distribution
- Gradle task in `rd-api`: produce `rdforward-api-<version>.jar` (interfaces only)
- Gradle task in `rd-bridge-bukkit`: produce `rdforward-bukkit-api-<version>.jar`
- Gradle task in `rd-bridge-fabric`: produce `rdforward-fabric-api-<version>.jar`
- These are `compileOnly` dependencies for mod developers

---

## Critical Files to Modify

| File | Change |
|------|--------|
| `settings.gradle` | Add `rd-api`, `rd-mod-loader`, `rd-bridge-bukkit`, `rd-bridge-fabric` |
| `rd-protocol/.../event/Event.java` | Keep as-is, add re-export from rd-api |
| `rd-protocol/.../event/EventResult.java` | Keep as-is, add re-export from rd-api |
| `rd-server/.../server/RDServer.java` | Add `ModLoader.init()` call after world setup |
| `rd-server/.../server/event/ServerEvents.java` | Evolve to use `PrioritizedEvent` with stop-on-cancel + MONITOR |
| `rd-server/.../server/api/CommandRegistry.java` | Implement rd-api `CommandRegistry`; add namespacing + conflict resolution |
| `rd-server/.../server/api/Scheduler.java` | Implement rd-api `Scheduler` interface; add ownership |
| `rd-server/.../server/api/ModConfig.java` | Implement rd-api `ModConfig` interface |
| `rd-server/.../server/api/PermissionManager.java` | Implement rd-api `PermissionManager` interface |

### New config files (auto-generated at runtime)

| File | Purpose |
|------|---------|
| `config/event-overrides.json` | Admin event priority/position overrides, reconciled on startup |
| `config/command-overrides.json` | Admin command conflict assignments, reconciled on startup |

---

## Verification Plan

### 1. Unit tests for `rd-api`
- `ModDescriptorParser` correctly parses JSON, YAML, TOML formats
- `DependencyResolver` handles correct ordering, detects circular dependencies
- `VersionSupport.isSupported()` returns correct results for all capabilities
- `EventOwnership` correctly tracks and removes listeners

### 2. Unit tests for event cancellation
- Cancellable event: listener at LOW cancels -> NORMAL/HIGH/HIGHEST listeners NOT called
- Cancellable event: MONITOR listeners still called after cancellation, receive `cancelled=true`
- MONITOR listeners cannot change event state (read-only enforcement)
- Non-cancellable event: all listeners called regardless of return value
- Multiple listeners at same priority: cancellation stops mid-priority (remaining at that level skipped)
- Priority ordering: LOWEST < LOW < NORMAL < HIGH < HIGHEST < MONITOR strictly enforced

### 3. Unit tests for admin event manager
- `EventManager.getListenerInfo()` returns correct mod/priority/class for all registered listeners
- Priority override: changing a listener's priority moves it in dispatch order
- Position override: reordering within same priority group works
- Override persistence: overrides survive save/load cycle
- Startup reconciliation: overrides for missing mods are removed
- Startup reconciliation: overrides for mods that no longer listen to an event are removed

### 4. Unit tests for command conflict resolution
- Single-provider command: bare name and namespaced name both work
- Multi-provider command: bare name points to first-loaded mod by default
- Admin assignment: `/commands assign ban ban-manager` changes bare command target
- Namespaced commands: `/essentials:ban` and `/ban-manager:ban` always work regardless of assignment
- Assignment persistence: survives save/load cycle
- Startup reconciliation: assigned mod removed -> re-evaluate, warn admin
- Startup reconciliation: only 1 provider left -> auto-assign, remove override
- Tab completion: bare command uses active provider's completer
- Server-core commands under `__server__` can be overridden

### 5. Unit tests for hot-reload
- Load mod -> disable -> verify all resources cleaned up
- Load mod -> reload -> verify new instance, no old references
- WeakReference classloader probe passes (classloader is GC'd)
- Orphan detection finds zero orphans after clean reload
- Event overrides reapplied after reload
- Command assignments preserved after reload

### 6. Integration tests for `rd-mod-loader`
- Create test mod JAR in `src/test/resources/mods/`
- Verify `ModLoader` discovers, resolves deps, loads class, calls `onEnable()`
- Verify events fire to mod listeners respecting priorities and cancellation
- Verify commands registered by mod are dispatchable (both bare and namespaced)
- Verify `/reload` cleanly replaces the mod
- Verify admin event/command overrides persist and reapply

### 7. Bridge tests
- **Bukkit**: Load a `JavaPlugin` with `plugin.yml`, verify `onEnable()`, events, commands
- **Bukkit**: Verify `ignoreCancelled=false` handlers do NOT receive cancelled events (intentional incompatibility)
- **Bukkit**: Verify one-time warning logged for `ignoreCancelled=false` handlers
- **Fabric**: Load a `ModInitializer` with `fabric.mod.json`, verify `onInitialize()`, events

### 8. E2E test
- Place test mod in `mods/`
- Start server
- Connect client
- Verify mod's player-join listener fires
- Verify mod's `/hello` command works (including conflict resolution with second test mod)
- Verify mod's block-place listener fires
- Verify event cancellation: protection mod blocks break -> logging mod does NOT see it (only MONITOR does)
- `/events info BLOCK_BREAK` shows correct listener registry
- `/reload test-mod` -> verify clean reload
- All existing e2e tests still pass (no regressions)

### 9. Build verification
- `./gradlew buildAll` succeeds
- API-only JARs are produced and contain only interfaces
- Mod JAR compiled against API-only JAR loads successfully at runtime

### 10. Memory leak verification
- Load/unload a mod 100 times in a loop
- Monitor heap size — should stay flat (no cumulative growth)
- All WeakReference probes report classloader collected

### 11. Admin config persistence verification
- Set event overrides + command assignments
- Restart server
- Verify all overrides and assignments reapplied from config files
- Remove a mod JAR, restart
- Verify stale overrides cleaned up, remaining overrides intact
- Verify command conflict re-prompted for newly unresolved conflicts
