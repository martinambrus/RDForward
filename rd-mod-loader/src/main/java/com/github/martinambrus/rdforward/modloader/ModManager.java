package com.github.martinambrus.rdforward.modloader;

import com.github.martinambrus.rdforward.api.event.EventOwnership;
import com.github.martinambrus.rdforward.api.event.PrioritizedEvent;
import com.github.martinambrus.rdforward.modloader.admin.EventManager;
import com.github.martinambrus.rdforward.api.mod.ClientMod;
import com.github.martinambrus.rdforward.api.mod.Mod;
import com.github.martinambrus.rdforward.api.mod.ModDescriptor;
import com.github.martinambrus.rdforward.api.mod.Reloadable;
import com.github.martinambrus.rdforward.api.mod.ServerMod;
import com.github.martinambrus.rdforward.api.server.Server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Central coordinator that instantiates, enables, disables and reloads
 * mods on behalf of the server. Implements {@link com.github.martinambrus.rdforward.api.mod.ModManager}
 * so mods can query it through the public API.
 *
 * <p>All mutation happens on the server main thread — mods are free to
 * observe the manager from any thread but must not drive lifecycle
 * transitions themselves.
 */
public final class ModManager implements com.github.martinambrus.rdforward.api.mod.ModManager {

    private static final Logger LOG = Logger.getLogger(ModManager.class.getName());

    private final Server server;
    private final LinkedHashMap<String, ModContainer> containers = new LinkedHashMap<>();
    private final ModThreadTracker threadTracker = new ModThreadTracker();

    public ModManager(Server server) {
        this.server = server;
        EventOwnership.install();
    }

    /** @return the per-manager thread tracker used by hot-reload cleanup. */
    public ModThreadTracker threadTracker() { return threadTracker; }

    /** Install the containers discovered by {@link ModLoader}. Must be called before {@link #enableAll()}. */
    public void setContainers(List<ModContainer> resolved) {
        containers.clear();
        for (ModContainer c : resolved) containers.put(c.id(), c);
    }

    /** Enable every container in load order. Mods already ENABLED are skipped. */
    public void enableAll() {
        for (ModContainer c : containers.values()) {
            if (c.state() == ModState.DISCOVERED) enable(c);
        }
    }

    /**
     * Enable a single mod by id. Used by the {@code /mod <id> enable}
     * admin command to bring back a mod that was previously disabled.
     *
     * @throws IllegalArgumentException if the mod id is unknown
     * @throws IllegalStateException    if the mod is already enabled
     */
    public void enableOne(String modId) {
        ModContainer c = containers.get(modId);
        if (c == null) throw new IllegalArgumentException("unknown mod: " + modId);
        if (c.state() == ModState.ENABLED) throw new IllegalStateException(modId + " is already enabled");
        if (c.state() == ModState.DISABLED) {
            try {
                ModLoader.rebind(c);
            } catch (Exception e) {
                c.fail(e);
                throw new IllegalStateException("rebind failed: " + e.getMessage(), e);
            }
        }
        enable(c);
    }

    /** Disable a single mod by id. No-op if the mod is not enabled. */
    public void disableOne(String modId) {
        ModContainer c = containers.get(modId);
        if (c == null) throw new IllegalArgumentException("unknown mod: " + modId);
        if (c.state() == ModState.ENABLED) disable(c);
    }

    /** Disable every enabled mod in reverse load order. */
    public void disableAll() {
        List<ModContainer> reverse = new ArrayList<>(containers.values());
        Collections.reverse(reverse);
        for (ModContainer c : reverse) {
            if (c.state() == ModState.ENABLED) disable(c);
        }
    }

    /**
     * Reload a single mod: save state (if {@link Reloadable}), disable,
     * discard classloader, re-instantiate from disk, enable, restore state.
     *
     * @throws IllegalArgumentException if the mod id is unknown
     * @throws IllegalStateException    if the mod is not currently enabled
     *     or if re-loading fails; the container enters {@link ModState#ERROR}
     */
    public void reload(String modId) {
        ModContainer c = containers.get(modId);
        if (c == null) throw new IllegalArgumentException("unknown mod: " + modId);
        if (c.state() != ModState.ENABLED) {
            throw new IllegalStateException("cannot reload " + modId + " in state " + c.state());
        }

        Object savedState = null;
        Reloadable r = c.reloadable();
        if (r != null) {
            try {
                savedState = r.onSaveState();
            } catch (Throwable t) {
                LOG.warning("[ModLoader] " + modId + ".onSaveState() threw: " + t);
            }
        }

        // Weak-reference probe per plan §3.3 step 3b. Captures the old class
        // loader BEFORE disable so we can verify it is eligible for GC after
        // resources are swept and the container releases its reference.
        java.lang.ref.WeakReference<ClassLoader> oldLoaderRef =
                new java.lang.ref.WeakReference<>(c.classLoader());

        disable(c);
        detectOrphans(modId);

        // Drop strong refs to the old classloader before probing. rebind()
        // would do this too but we need it *before* the GC hint fires so
        // the weak-reference check is meaningful.
        try {
            if (c.classLoader() != null) c.classLoader().close();
        } catch (java.io.IOException ignored) {}
        c.setClassLoader(null);
        c.setServerInstance(null);
        c.setClientInstance(null);

        // GC hint (NOT relied on). If the classloader is still held by something
        // after the sweep, warn the admin — that's a likely leak source.
        System.gc();
        if (oldLoaderRef.get() != null) {
            LOG.warning("[ModLoader] WARNING: ClassLoader for " + modId
                    + " was not garbage collected. This may indicate a memory leak"
                    + " (static references, thread locals, etc.)");
        }

        try {
            ModLoader.rebind(c);
        } catch (Exception e) {
            c.fail(e);
            throw new IllegalStateException("reload failed for " + modId + ": " + e.getMessage(), e);
        }
        enable(c);
        EventManager.applyOverrides(id -> containers.containsKey(id));

        Reloadable r2 = c.reloadable();
        if (r2 != null) {
            try {
                r2.onRestoreState(savedState);
            } catch (Throwable t) {
                LOG.warning("[ModLoader] " + modId + ".onRestoreState() threw: " + t);
            }
        }
    }

    private void enable(ModContainer c) {
        c.setState(ModState.LOADING);
        try {
            ServerMod sm = c.serverMod();
            if (sm != null) {
                EventOwnership.withOwner(c.id(), () -> sm.onEnable(server));
            }
            c.setState(ModState.ENABLED);
            LOG.info("[ModLoader] Enabled " + c.id() + " v" + c.descriptor().version());
        } catch (Throwable t) {
            c.fail(t);
            LOG.severe("[ModLoader] " + c.id() + ".onEnable() threw: " + t);
        }
    }

    private void disable(ModContainer c) {
        c.setState(ModState.DISABLING);
        try {
            ServerMod sm = c.serverMod();
            if (sm != null) sm.onDisable();
            ClientMod cm = c.clientMod();
            if (cm != null && cm != sm) cm.onClientStop();
        } catch (Throwable t) {
            LOG.warning("[ModLoader] " + c.id() + ".onDisable() threw: " + t);
        }
        sweepOwnedResources(c.id());
        c.setState(ModState.DISABLED);
    }

    /**
     * Remove every server resource tagged with this mod id — plain Event
     * listeners, PrioritizedEvent listeners across every instance, command
     * registrations, pending scheduler tasks, and plugin channels. Called
     * after the mod's {@code onDisable()} so the mod can react to shutdown
     * before its resources are yanked.
     */
    private void sweepOwnedResources(String modId) {
        int events = EventOwnership.unregisterAllForMod(modId);
        int prioritized = PrioritizedEvent.unregisterAllByOwner(modId);
        int commands = server.getCommandRegistry().unregisterByOwner(modId);
        int tasks = server.getScheduler().cancelByOwner(modId);
        int channels = com.github.martinambrus.rdforward.server.network.PluginChannelManager
                .removeChannelsOwnedBy(modId);
        int threads = threadTracker.stopThreadsOwnedBy(modId);
        int extras = com.github.martinambrus.rdforward.api.mod.ResourceSweeper.sweep(modId);
        LOG.info("[ModLoader] Cleaned up " + modId
                + ": " + events + " event listener(s), "
                + prioritized + " prioritized event(s), "
                + commands + " command(s), "
                + tasks + " task(s), "
                + channels + " plugin channel(s), "
                + threads + " thread(s), "
                + extras + " client resource(s)");
    }

    /**
     * Orphan detection per plan §3.3 step 5. Scans the resource registries
     * for any listener/command/task/channel still tagged with {@code modId}
     * AFTER {@link #sweepOwnedResources} has run. Finding any indicates a
     * bug in our cleanup logic and logs an ERROR.
     *
     * @return number of orphaned entries found across all registries.
     */
    int detectOrphans(String modId) {
        int orphans = 0;
        for (PrioritizedEvent<?> ev : PrioritizedEvent.allInstances()) {
            for (com.github.martinambrus.rdforward.api.event.ListenerInfo info : ev.getListenerInfo()) {
                if (modId.equals(info.modId())) orphans++;
            }
        }
        for (com.github.martinambrus.rdforward.api.registry.RegistryKey k :
                com.github.martinambrus.rdforward.server.network.PluginChannelManager.channelIds()) {
            com.github.martinambrus.rdforward.server.network.DefaultPluginChannel ch =
                    com.github.martinambrus.rdforward.server.network.PluginChannelManager.get(k);
            if (ch != null && modId.equals(ch.ownerModId())) orphans++;
        }
        for (Map.Entry<Thread, String> e : threadTracker.snapshot().entrySet()) {
            if (modId.equals(e.getValue()) && e.getKey().isAlive()) orphans++;
        }
        if (orphans > 0) {
            LOG.severe("[ModLoader] ERROR: " + orphans + " orphaned resource(s) remain for "
                    + modId + " after sweep — bug in cleanup logic");
        }
        return orphans;
    }

    // -- api.mod.ModManager queries --

    @Override
    public ModDescriptor get(String modId) {
        ModContainer c = containers.get(modId);
        return c == null ? null : c.descriptor();
    }

    @Override
    public boolean isLoaded(String modId) {
        ModContainer c = containers.get(modId);
        return c != null && c.state() == ModState.ENABLED;
    }

    @Override
    public Collection<ModDescriptor> all() {
        List<ModDescriptor> out = new ArrayList<>(containers.size());
        for (ModContainer c : containers.values()) {
            if (c.state() == ModState.ENABLED) out.add(c.descriptor());
        }
        return out;
    }

    /** @return containers in load order, for loader/admin tooling. Includes failed and disabled mods. */
    public Collection<ModContainer> containers() {
        return Collections.unmodifiableCollection(containers.values());
    }

    /** @return the entrypoint class, for internal tooling to test instanceof against {@link Mod}. */
    Map<String, ModContainer> containersById() {
        return containers;
    }
}
