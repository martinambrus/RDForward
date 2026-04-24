package com.github.martinambrus.rdforward.modloader.admin;

import com.github.martinambrus.rdforward.api.event.EventPriority;
import com.github.martinambrus.rdforward.api.event.ListenerInfo;
import com.github.martinambrus.rdforward.api.event.PrioritizedEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Runtime admin control plane for {@link PrioritizedEvent} listeners.
 * Gives ops a way to disable a misbehaving mod's listener on a specific
 * event without taking the whole mod down, and persists those overrides
 * to {@code event-overrides.json} so they survive restarts.
 *
 * <p>Each event is identified by {@code HolderSimpleName#FIELD_NAME}
 * (e.g. {@code ServerEvents#BLOCK_BREAK}) — the holder classes are
 * scanned reflectively at {@link #install(List, Path)} time.
 *
 * <p>Each listener is identified by the tuple
 * {@code (eventId, modId, priority, listenerClassName)}. Disabling a
 * listener records that tuple; {@link PrioritizedEvent#dispatchGate}
 * returns {@code false} for matching listeners so they are skipped on
 * the next invoker rebuild.
 */
public final class EventManager {

    private static final Logger LOG = Logger.getLogger(EventManager.class.getName());
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static final Map<String, PrioritizedEvent<?>> byId = new LinkedHashMap<>();
    private static final Map<PrioritizedEvent<?>, String> idByEvent = new ConcurrentHashMap<>();
    private static final Set<String> disabledKeys = ConcurrentHashMap.newKeySet();
    /** eventId -> modId -> priority/position override. */
    private static final Map<String, Map<String, Override>> overrides = new ConcurrentHashMap<>();
    private static volatile Path overrideFile;
    private static volatile boolean installed;

    private EventManager() {}

    /**
     * Build the event registry from the given holder classes, load any
     * persisted overrides from {@code overrideFile}, and install the
     * dispatch gate on {@link PrioritizedEvent}. Safe to call more than
     * once — subsequent calls re-scan and reconcile.
     */
    public static synchronized void install(List<Class<?>> holders, Path overrideFile) {
        byId.clear();
        idByEvent.clear();
        for (Class<?> holder : holders) {
            scan(holder);
        }
        EventManager.overrideFile = overrideFile;
        loadOverrides();
        if (!installed) {
            PrioritizedEvent.dispatchGate = EventManager::allow;
            installed = true;
        }
        refreshAll();
        LOG.info("[EventManager] installed with " + byId.size() + " event(s), "
                + disabledKeys.size() + " override(s)");
    }

    private static void scan(Class<?> holder) {
        for (Field f : holder.getDeclaredFields()) {
            int mods = f.getModifiers();
            if (!(Modifier.isStatic(mods) && Modifier.isFinal(mods))) continue;
            if (!PrioritizedEvent.class.isAssignableFrom(f.getType())) continue;
            try {
                f.setAccessible(true);
                PrioritizedEvent<?> ev = (PrioritizedEvent<?>) f.get(null);
                if (ev == null) continue;
                String id = holder.getSimpleName() + "#" + f.getName();
                byId.put(id, ev);
                idByEvent.put(ev, id);
            } catch (ReflectiveOperationException e) {
                LOG.warning("[EventManager] failed to scan " + holder.getName() + "#" + f.getName() + ": " + e);
            }
        }
    }

    private static boolean allow(PrioritizedEvent<?> event, ListenerInfo info) {
        String eventId = idByEvent.get(event);
        if (eventId == null) return true;
        return !disabledKeys.contains(key(eventId, info));
    }

    private static String key(String eventId, ListenerInfo info) {
        return eventId + "|" + info.modId() + "|" + info.priority().name() + "|" + info.listenerClass();
    }

    /** @return every live listener on every known event, plus its disabled flag. */
    public static List<Entry> snapshot() {
        List<Entry> out = new ArrayList<>();
        for (Map.Entry<String, PrioritizedEvent<?>> e : byId.entrySet()) {
            String eventId = e.getKey();
            for (ListenerInfo info : e.getValue().getListenerInfo()) {
                out.add(new Entry(eventId, info, disabledKeys.contains(key(eventId, info))));
            }
        }
        return out;
    }

    /** Disable the matching listener(s) and persist. Returns number disabled. */
    public static int disable(String eventId, String modId, EventPriority priority, String listenerClassName) {
        PrioritizedEvent<?> ev = byId.get(eventId);
        if (ev == null) return 0;
        int count = 0;
        for (ListenerInfo info : ev.getListenerInfo()) {
            if (matches(info, modId, priority, listenerClassName)
                    && disabledKeys.add(key(eventId, info))) {
                count++;
            }
        }
        if (count > 0) { ev.refresh(); save(); }
        return count;
    }

    /** Enable a previously-disabled listener. Returns number re-enabled. */
    public static int enable(String eventId, String modId, EventPriority priority, String listenerClassName) {
        PrioritizedEvent<?> ev = byId.get(eventId);
        if (ev == null) return 0;
        int count = 0;
        for (ListenerInfo info : ev.getListenerInfo()) {
            if (matches(info, modId, priority, listenerClassName)
                    && disabledKeys.remove(key(eventId, info))) {
                count++;
            }
        }
        if (count > 0) { ev.refresh(); save(); }
        return count;
    }

    /** Drop every override across every event and persist the empty file. */
    public static void clearAll() {
        boolean hadDisabled = !disabledKeys.isEmpty();
        boolean hadOverrides = !overrides.isEmpty();
        if (!hadDisabled && !hadOverrides) return;
        // Revert priority/position overrides first so listeners go back to original priority.
        for (Map.Entry<String, Map<String, Override>> e : overrides.entrySet()) {
            PrioritizedEvent<?> ev = byId.get(e.getKey());
            if (ev == null) continue;
            for (Map.Entry<String, Override> m : e.getValue().entrySet()) {
                ev.moveListener(m.getKey(), null, m.getValue().originalPriority(), null);
            }
        }
        disabledKeys.clear();
        overrides.clear();
        refreshAll();
        save();
    }

    /**
     * Change a listener's priority on a specific event. Records an override
     * so the change survives restarts (applied during {@link #applyOverrides(java.util.function.Predicate)}).
     * @return true if the listener was found and moved.
     */
    public static boolean setPriority(String eventId, String modId, EventPriority newPriority) {
        PrioritizedEvent<?> ev = byId.get(eventId);
        if (ev == null) return false;
        EventPriority original = ev.findPriority(modId, null);
        if (original == null) return false;
        if (!ev.moveListener(modId, null, newPriority, null)) return false;
        Map<String, Override> eventOverrides = overrides.computeIfAbsent(eventId, k -> new ConcurrentHashMap<>());
        Override existing = eventOverrides.get(modId);
        EventPriority keepOriginal = existing != null ? existing.originalPriority() : original;
        eventOverrides.put(modId, new Override(keepOriginal, newPriority, 0));
        save();
        return true;
    }

    /**
     * Reorder a listener within its current priority bucket. Records the
     * override so the position survives restarts. @return true on success.
     */
    public static boolean setPosition(String eventId, String modId, int newPosition) {
        PrioritizedEvent<?> ev = byId.get(eventId);
        if (ev == null) return false;
        EventPriority current = ev.findPriority(modId, null);
        if (current == null) return false;
        if (!ev.setPosition(modId, null, newPosition)) return false;
        Map<String, Override> eventOverrides = overrides.computeIfAbsent(eventId, k -> new ConcurrentHashMap<>());
        Override existing = eventOverrides.get(modId);
        EventPriority keepOriginal = existing != null ? existing.originalPriority() : current;
        EventPriority keepPriority = existing != null ? existing.overridePriority() : current;
        eventOverrides.put(modId, new Override(keepOriginal, keepPriority, newPosition));
        save();
        return true;
    }

    /** Reset all priority/position overrides for a specific event. */
    public static boolean resetEvent(String eventId) {
        Map<String, Override> eventOverrides = overrides.remove(eventId);
        if (eventOverrides == null || eventOverrides.isEmpty()) return false;
        PrioritizedEvent<?> ev = byId.get(eventId);
        if (ev != null) {
            for (Map.Entry<String, Override> e : eventOverrides.entrySet()) {
                ev.moveListener(e.getKey(), null, e.getValue().originalPriority(), null);
            }
        }
        save();
        return true;
    }

    /** Reset all priority/position overrides on all events. */
    public static void resetAllOverrides() {
        if (overrides.isEmpty()) return;
        for (Map.Entry<String, Map<String, Override>> e : overrides.entrySet()) {
            PrioritizedEvent<?> ev = byId.get(e.getKey());
            if (ev == null) continue;
            for (Map.Entry<String, Override> m : e.getValue().entrySet()) {
                ev.moveListener(m.getKey(), null, m.getValue().originalPriority(), null);
            }
        }
        overrides.clear();
        save();
    }

    /**
     * Startup reconciliation per plan §4.5.4: drop overrides whose mods
     * are no longer loaded or no longer listen to the event, apply the
     * rest. The caller supplies a predicate that returns true if a mod
     * id is currently loaded.
     */
    public static synchronized void applyOverrides(java.util.function.Predicate<String> isModLoaded) {
        List<String> eventsToRemove = new ArrayList<>();
        for (Map.Entry<String, Map<String, Override>> e : overrides.entrySet()) {
            String eventId = e.getKey();
            PrioritizedEvent<?> ev = byId.get(eventId);
            Map<String, Override> eventOverrides = e.getValue();
            List<String> modsToRemove = new ArrayList<>();
            for (Map.Entry<String, Override> m : eventOverrides.entrySet()) {
                String modId = m.getKey();
                Override ov = m.getValue();
                if (ev == null) { modsToRemove.add(modId); continue; }
                if (!isModLoaded.test(modId)) {
                    LOG.info("[EventManager] Removed event overrides for missing mod '" + modId + "'");
                    modsToRemove.add(modId);
                    continue;
                }
                EventPriority current = ev.findPriority(modId, null);
                if (current == null) {
                    LOG.info("[EventManager] Removed stale " + eventId + " override for '"
                            + modId + "' (no longer listens)");
                    modsToRemove.add(modId);
                    continue;
                }
                ev.moveListener(modId, null, ov.overridePriority(), ov.position());
                LOG.info("[EventManager] Applied override: " + modId + " " + eventId
                        + " -> " + ov.overridePriority() + " priority");
            }
            for (String modId : modsToRemove) eventOverrides.remove(modId);
            if (eventOverrides.isEmpty()) eventsToRemove.add(eventId);
        }
        for (String eventId : eventsToRemove) overrides.remove(eventId);
        save();
    }

    /** Snapshot of overrides for admin/diag. */
    public static Map<String, Map<String, Override>> overridesSnapshot() {
        Map<String, Map<String, Override>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Override>> e : overrides.entrySet()) {
            copy.put(e.getKey(), new LinkedHashMap<>(e.getValue()));
        }
        return copy;
    }

    /** @return the set of known event ids in insertion order (holder-class scan order). */
    public static Set<String> eventIds() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(byId.keySet()));
    }

    private static boolean matches(ListenerInfo info, String modId, EventPriority priority, String listenerClassName) {
        if (modId != null && !modId.equals(info.modId())) return false;
        if (priority != null && priority != info.priority()) return false;
        if (listenerClassName != null && !listenerClassName.equals(info.listenerClass())) return false;
        return true;
    }

    private static void refreshAll() {
        for (PrioritizedEvent<?> ev : byId.values()) ev.refresh();
    }

    private static void loadOverrides() {
        disabledKeys.clear();
        overrides.clear();
        if (overrideFile == null || !Files.exists(overrideFile)) return;
        try (Reader r = Files.newBufferedReader(overrideFile)) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
            JsonArray disabledArr = root.getAsJsonArray("disabled");
            if (disabledArr != null) {
                for (int i = 0; i < disabledArr.size(); i++) {
                    JsonObject o = disabledArr.get(i).getAsJsonObject();
                    disabledKeys.add(key(
                            o.get("eventId").getAsString(),
                            new ListenerInfo(
                                    o.get("modId").getAsString(),
                                    EventPriority.valueOf(o.get("priority").getAsString()),
                                    o.get("listenerClassName").getAsString())));
                }
            }
            JsonObject overrideObj = root.getAsJsonObject("overrides");
            if (overrideObj != null) {
                for (Map.Entry<String, com.google.gson.JsonElement> e : overrideObj.entrySet()) {
                    JsonObject perEvent = e.getValue().getAsJsonObject();
                    Map<String, Override> perEventMap = new ConcurrentHashMap<>();
                    for (Map.Entry<String, com.google.gson.JsonElement> m : perEvent.entrySet()) {
                        JsonObject ov = m.getValue().getAsJsonObject();
                        perEventMap.put(m.getKey(), new Override(
                                EventPriority.valueOf(ov.get("original_priority").getAsString()),
                                EventPriority.valueOf(ov.get("override_priority").getAsString()),
                                ov.has("position_in_priority") ? ov.get("position_in_priority").getAsInt() : 0));
                    }
                    overrides.put(e.getKey(), perEventMap);
                }
            }
        } catch (IOException e) {
            LOG.warning("[EventManager] could not read " + overrideFile + ": " + e);
        }
    }

    private static synchronized void save() {
        if (overrideFile == null) return;
        try {
            Files.createDirectories(overrideFile.getParent());
            JsonObject root = new JsonObject();
            root.addProperty("version", 1);
            JsonArray disabledArr = new JsonArray();
            for (String key : disabledKeys) {
                String[] parts = key.split("\\|", 4);
                if (parts.length != 4) continue;
                JsonObject o = new JsonObject();
                o.addProperty("eventId", parts[0]);
                o.addProperty("modId", parts[1]);
                o.addProperty("priority", parts[2]);
                o.addProperty("listenerClassName", parts[3]);
                disabledArr.add(o);
            }
            root.add("disabled", disabledArr);
            JsonObject overrideObj = new JsonObject();
            for (Map.Entry<String, Map<String, Override>> e : overrides.entrySet()) {
                JsonObject perEvent = new JsonObject();
                for (Map.Entry<String, Override> m : e.getValue().entrySet()) {
                    JsonObject ov = new JsonObject();
                    ov.addProperty("original_priority", m.getValue().originalPriority().name());
                    ov.addProperty("override_priority", m.getValue().overridePriority().name());
                    ov.addProperty("position_in_priority", m.getValue().position());
                    perEvent.add(m.getKey(), ov);
                }
                overrideObj.add(e.getKey(), perEvent);
            }
            root.add("overrides", overrideObj);
            try (Writer w = Files.newBufferedWriter(overrideFile)) {
                GSON.toJson(root, w);
            }
        } catch (IOException e) {
            LOG.warning("[EventManager] could not write " + overrideFile + ": " + e);
        }
    }

    /** Snapshot row used by admin commands and HTTP tooling. */
    public record Entry(String eventId, ListenerInfo listener, boolean disabled) {}

    /** Priority/position override per plan §4.5.3. */
    public record Override(EventPriority originalPriority, EventPriority overridePriority, int position) {}
}
