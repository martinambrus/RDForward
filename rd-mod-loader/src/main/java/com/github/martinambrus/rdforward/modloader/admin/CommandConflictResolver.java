package com.github.martinambrus.rdforward.modloader.admin;

import com.github.martinambrus.rdforward.server.api.Command;
import com.github.martinambrus.rdforward.server.api.CommandRegistry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Decides which mod owns the bare alias when two mods register a command
 * with the same name. Each mod's {@code modId:name} alias always lives,
 * but only one mod's handler binds to the bare {@code name} in rd-server's
 * static {@link CommandRegistry} at a time.
 *
 * <p>Resolution order:
 * <ol>
 *   <li>Explicit override from {@code command-overrides.json}.</li>
 *   <li>The {@value #SERVER_OWNER} pseudo-mod for built-in server commands.</li>
 *   <li>First registration wins — alphabetical mod id tie-breaks.</li>
 * </ol>
 *
 * <p>Ops can inspect and reassign via {@code /commands} (see
 * {@link AdminCommands}). The resolver is installed once in
 * {@link com.github.martinambrus.rdforward.modloader.ModSystem#boot(Object, Path)}
 * after rd-server finishes registering its built-in commands.
 */
public final class CommandConflictResolver {

    private static final Logger LOG = Logger.getLogger(CommandConflictResolver.class.getName());
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /** Pseudo mod id used for commands installed by rd-server itself. */
    public static final String SERVER_OWNER = "__server__";

    private static final Map<String, LinkedHashMap<String, Claim>> claimsByName = new ConcurrentHashMap<>();
    private static final Map<String, String> overrides = new ConcurrentHashMap<>();
    private static volatile Path overrideFile;
    private static volatile boolean installed;

    private CommandConflictResolver() {}

    /**
     * Snapshot the commands currently registered on rd-server's static
     * registry as {@value #SERVER_OWNER} claims, then load persisted
     * overrides. Must be called after built-in commands are registered
     * but before any mod's {@code onEnable} runs.
     */
    public static synchronized void install(Path overrideFile) {
        claimsByName.clear();
        CommandConflictResolver.overrideFile = overrideFile;
        for (Map.Entry<String, CommandRegistry.RegisteredCommand> e : CommandRegistry.getCommands().entrySet()) {
            String name = e.getKey();
            if (name.contains(":")) continue;
            CommandRegistry.RegisteredCommand rc = e.getValue();
            Claim c = new Claim(SERVER_OWNER, rc.name, rc.description, rc.requiredOpLevel, rc.command);
            claimsByName.computeIfAbsent(name, k -> new LinkedHashMap<>()).put(SERVER_OWNER, c);
        }
        loadOverrides();
        installed = true;
        LOG.info("[CommandResolver] installed with " + claimsByName.size()
                + " server command(s), " + overrides.size() + " override(s)");
    }

    /**
     * Record a mod's claim on the given command name. Reapplies the
     * winner so the bare alias points at whichever claim the resolution
     * rules pick. The mod's {@code modId:name} alias must be registered
     * separately by the caller.
     */
    public static void claim(String modId, String name, String description, int opLevel, Command handler) {
        if (!installed) {
            LOG.warning("[CommandResolver] claim(" + modId + ":" + name + ") before install — claim queued");
        }
        Claim c = new Claim(modId, name.toLowerCase(), description, opLevel, handler);
        claimsByName.computeIfAbsent(c.name(), k -> new LinkedHashMap<>()).put(modId, c);
        applyWinner(c.name());
    }

    /** Drop one mod's claim on one name. Picks a new winner (or clears the alias). */
    public static void unclaim(String modId, String name) {
        String key = name.toLowerCase();
        LinkedHashMap<String, Claim> claims = claimsByName.get(key);
        if (claims == null) return;
        if (claims.remove(modId) == null) return;
        if (claims.isEmpty()) {
            claimsByName.remove(key);
            CommandRegistry.unregister(key);
        } else {
            applyWinner(key);
        }
    }

    /** Drop every claim this mod owns. */
    public static int unclaimAll(String modId) {
        int count = 0;
        for (String name : new ArrayList<>(claimsByName.keySet())) {
            LinkedHashMap<String, Claim> claims = claimsByName.get(name);
            if (claims != null && claims.containsKey(modId)) {
                unclaim(modId, name);
                count++;
            }
        }
        return count;
    }

    /** @return the mod id whose handler is currently bound to {@code name}, or {@code null}. */
    public static String resolve(String name) {
        LinkedHashMap<String, Claim> claims = claimsByName.get(name.toLowerCase());
        if (claims == null || claims.isEmpty()) return null;
        String override = overrides.get(name.toLowerCase());
        if (override != null && claims.containsKey(override)) return override;
        if (claims.containsKey(SERVER_OWNER)) return SERVER_OWNER;
        return claims.keySet().iterator().next();
    }

    /** @return names with more than one claimant, in insertion order. */
    public static List<String> conflictedNames() {
        List<String> out = new ArrayList<>();
        for (Map.Entry<String, LinkedHashMap<String, Claim>> e : claimsByName.entrySet()) {
            if (e.getValue().size() > 1) out.add(e.getKey());
        }
        return out;
    }

    /** @return a deep snapshot: name → mod ids that have claimed it, in first-come order. */
    public static Map<String, List<String>> allClaims() {
        Map<String, List<String>> out = new LinkedHashMap<>();
        for (Map.Entry<String, LinkedHashMap<String, Claim>> e : claimsByName.entrySet()) {
            out.put(e.getKey(), new ArrayList<>(e.getValue().keySet()));
        }
        return Collections.unmodifiableMap(out);
    }

    /** Pin the bare alias to a specific mod. Persists. */
    public static void setOverride(String name, String modId) {
        String key = name.toLowerCase();
        overrides.put(key, modId);
        save();
        applyWinner(key);
    }

    /** Remove the override, letting the default rules pick the winner again. Persists. */
    public static void clearOverride(String name) {
        String key = name.toLowerCase();
        if (overrides.remove(key) == null) return;
        save();
        applyWinner(key);
    }

    /**
     * Startup reconciliation per plan §5.1 / §14. Drops overrides that pin
     * a bare alias to a mod that is no longer present on disk, logs a
     * warning for each removal, and re-runs winner selection so the alias
     * binds to whatever remaining claimant the default rules pick.
     *
     * <p>Must be called once after every mod's {@code onEnable} has run so
     * every live claim is already in {@link #claimsByName}. The predicate
     * returns {@code true} if the given mod id still exists as a discovered
     * container (any lifecycle state).
     *
     * @return number of override entries dropped.
     */
    public static synchronized int reconcile(java.util.function.Predicate<String> isModPresent) {
        int changes = 0;
        for (String name : new ArrayList<>(overrides.keySet())) {
            String modId = overrides.get(name);
            if (modId == null || SERVER_OWNER.equals(modId)) continue;
            if (!isModPresent.test(modId)) {
                overrides.remove(name);
                LOG.warning("[CommandResolver] dropped override '" + name + " -> "
                        + modId + "' (mod no longer present on disk)");
                changes++;
                applyWinner(name);
            }
        }
        if (changes > 0) save();
        return changes;
    }

    /** Clear every override. Persists. @return number of overrides cleared. */
    public static int clearAllOverrides() {
        if (overrides.isEmpty()) return 0;
        List<String> keys = new ArrayList<>(overrides.keySet());
        overrides.clear();
        save();
        for (String k : keys) applyWinner(k);
        return keys.size();
    }

    private static void applyWinner(String name) {
        LinkedHashMap<String, Claim> claims = claimsByName.get(name);
        if (claims == null || claims.isEmpty()) {
            CommandRegistry.unregister(name);
            return;
        }
        String winnerId = resolve(name);
        Claim winner = claims.get(winnerId);
        CommandRegistry.unregister(name);
        if (winner.opLevel() > 0) {
            CommandRegistry.registerOp(winner.name(), winner.description(), winner.opLevel(), winner.handler());
        } else {
            CommandRegistry.register(winner.name(), winner.description(), winner.handler());
        }
    }

    private static void loadOverrides() {
        overrides.clear();
        if (overrideFile == null || !Files.exists(overrideFile)) return;
        try (Reader r = Files.newBufferedReader(overrideFile)) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
            JsonObject map = root.getAsJsonObject("overrides");
            if (map == null) return;
            for (Map.Entry<String, com.google.gson.JsonElement> e : map.entrySet()) {
                overrides.put(e.getKey().toLowerCase(), e.getValue().getAsString());
            }
        } catch (IOException e) {
            LOG.warning("[CommandResolver] could not read " + overrideFile + ": " + e);
        }
    }

    private static synchronized void save() {
        if (overrideFile == null) return;
        try {
            Files.createDirectories(overrideFile.getParent());
            JsonObject root = new JsonObject();
            JsonObject map = new JsonObject();
            for (Map.Entry<String, String> e : overrides.entrySet()) map.addProperty(e.getKey(), e.getValue());
            root.add("overrides", map);
            try (Writer w = Files.newBufferedWriter(overrideFile)) {
                GSON.toJson(root, w);
            }
        } catch (IOException e) {
            LOG.warning("[CommandResolver] could not write " + overrideFile + ": " + e);
        }
    }

    /** One claim on a command name — kept per mod so the resolver can flip winners at any time. */
    public record Claim(String modId, String name, String description, int opLevel, Command handler) {}
}
