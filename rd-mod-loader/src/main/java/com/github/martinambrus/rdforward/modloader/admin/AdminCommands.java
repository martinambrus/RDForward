package com.github.martinambrus.rdforward.modloader.admin;

import com.github.martinambrus.rdforward.api.event.EventPriority;
import com.github.martinambrus.rdforward.api.event.ListenerInfo;
import com.github.martinambrus.rdforward.modloader.ModContainer;
import com.github.martinambrus.rdforward.modloader.ModManager;
import com.github.martinambrus.rdforward.modloader.ModState;
import com.github.martinambrus.rdforward.server.api.Command;
import com.github.martinambrus.rdforward.server.api.CommandContext;
import com.github.martinambrus.rdforward.server.api.CommandRegistry;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Registers built-in admin commands ({@code /events}, later {@code /mods}
 * and {@code /commands}) against rd-server's static command registry.
 * Registered once during {@link com.github.martinambrus.rdforward.modloader.ModSystem#boot}.
 */
public final class AdminCommands {

    private static final int OP_LEVEL = 4;
    private static volatile boolean registered;
    private static volatile ModManager modManager;

    private AdminCommands() {}

    public static synchronized void register() {
        if (registered) return;
        registered = true;
        CommandRegistry.registerOp("events",
                "Inspect and control prioritized event listeners — see /events help",
                OP_LEVEL, AdminCommands::handleEvents);
        CommandRegistry.registerOp("commands",
                "Inspect and resolve command name conflicts — see /commands help",
                OP_LEVEL, AdminCommands::handleCommands);
        CommandRegistry.registerOp("mods",
                "List every loaded mod with its state and version",
                OP_LEVEL, AdminCommands::handleMods);
        CommandRegistry.registerOp("mod",
                "Inspect or control a single mod — see /mod help",
                OP_LEVEL, AdminCommands::handleMod);
        CommandRegistry.registerOp("reload",
                "Hot-reload every enabled mod",
                OP_LEVEL, AdminCommands::handleReload);
    }

    /** Called by {@link com.github.martinambrus.rdforward.modloader.ModSystem#boot}. */
    public static void bindManager(ModManager manager) {
        modManager = manager;
    }

    private static void handleEvents(CommandContext ctx) {
        String[] args = ctx.getArgs();
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            help(ctx);
            return;
        }
        switch (args[0].toLowerCase()) {
            case "list" -> cmdList(ctx, args);
            case "info" -> cmdInfo(ctx, args);
            case "mod" -> cmdEventsMod(ctx, args);
            case "setpriority" -> cmdSetPriority(ctx, args);
            case "setposition" -> cmdSetPosition(ctx, args);
            case "reset" -> cmdEventsReset(ctx, args);
            case "disable" -> cmdToggle(ctx, args, true);
            case "enable" -> cmdToggle(ctx, args, false);
            case "clear" -> cmdClear(ctx);
            default -> ctx.reply("Unknown subcommand: " + args[0] + " — try /events help");
        }
    }

    private static void help(CommandContext ctx) {
        ctx.reply("/events list                          — list every event id");
        ctx.reply("/events list <eventId>                — list listeners on that event");
        ctx.reply("/events info <eventId>                — listeners grouped by priority");
        ctx.reply("/events mod <modId>                   — events a specific mod listens to");
        ctx.reply("/events setpriority <eventId> <modId> <priority>");
        ctx.reply("/events setposition <eventId> <modId> <index>");
        ctx.reply("/events reset <eventId>               — drop priority/position overrides on event");
        ctx.reply("/events reset all                     — drop every priority/position override");
        ctx.reply("/events disable <eventId> <modId> <priority> [listenerClass]");
        ctx.reply("/events enable  <eventId> <modId> <priority> [listenerClass]");
        ctx.reply("/events clear                         — wipe every override (disabled + priority)");
    }

    private static void cmdList(CommandContext ctx, String[] args) {
        if (args.length < 2) {
            for (String id : EventManager.eventIds()) ctx.reply("  " + id);
            return;
        }
        String eventId = args[1];
        boolean found = false;
        for (EventManager.Entry e : EventManager.snapshot()) {
            if (!e.eventId().equals(eventId)) continue;
            found = true;
            ListenerInfo info = e.listener();
            String tag = e.disabled() ? "[DISABLED] " : "           ";
            ctx.reply(tag + info.modId() + " " + info.priority() + " " + info.listenerClass());
        }
        if (!found) ctx.reply("No listeners on " + eventId + " (or event does not exist)");
    }

    private static void cmdInfo(CommandContext ctx, String[] args) {
        if (args.length < 2) {
            ctx.reply("Usage: /events info <eventId>");
            return;
        }
        String eventId = args[1];
        if (!EventManager.eventIds().contains(eventId)) {
            ctx.reply("No such event: " + eventId);
            return;
        }
        ctx.reply("=== " + eventId + " Listeners ===");
        Map<EventPriority, List<EventManager.Entry>> byPriority = new java.util.EnumMap<>(EventPriority.class);
        for (EventPriority p : EventPriority.values()) byPriority.put(p, new java.util.ArrayList<>());
        for (EventManager.Entry e : EventManager.snapshot()) {
            if (e.eventId().equals(eventId)) byPriority.get(e.listener().priority()).add(e);
        }
        int position = 0;
        for (EventPriority p : EventPriority.values()) {
            ctx.reply("Priority: " + p.name());
            List<EventManager.Entry> bucket = byPriority.get(p);
            if (bucket.isEmpty()) {
                ctx.reply("  (none)");
                continue;
            }
            for (EventManager.Entry e : bucket) {
                position++;
                ListenerInfo info = e.listener();
                String flags = e.disabled() ? "[DISABLED]" : (p == EventPriority.MONITOR ? "read-only" : "");
                ctx.reply("  " + position + ". [" + info.modId() + "] " + info.listenerClass()
                        + "  (position: " + position + (flags.isEmpty() ? "" : ", " + flags) + ")");
            }
        }
    }

    private static void cmdEventsMod(CommandContext ctx, String[] args) {
        if (args.length < 2) {
            ctx.reply("Usage: /events mod <modId>");
            return;
        }
        String modId = args[1];
        boolean found = false;
        for (EventManager.Entry e : EventManager.snapshot()) {
            if (!e.listener().modId().equals(modId)) continue;
            found = true;
            String tag = e.disabled() ? "[DISABLED] " : "           ";
            ctx.reply(tag + e.eventId() + " @ " + e.listener().priority() + " (" + e.listener().listenerClass() + ")");
        }
        if (!found) ctx.reply("No listeners registered by mod: " + modId);
    }

    private static void cmdSetPriority(CommandContext ctx, String[] args) {
        if (args.length < 4) {
            ctx.reply("Usage: /events setpriority <eventId> <modId> <priority>");
            return;
        }
        String eventId = args[1];
        String modId = args[2];
        EventPriority newPriority;
        try {
            newPriority = EventPriority.valueOf(args[3].toUpperCase());
        } catch (IllegalArgumentException e) {
            ctx.reply("Unknown priority: " + args[3] + " — valid: LOWEST, LOW, NORMAL, HIGH, HIGHEST, MONITOR");
            return;
        }
        if (EventManager.setPriority(eventId, modId, newPriority)) {
            ctx.reply("Moved " + modId + "'s " + eventId + " listener to " + newPriority);
        } else {
            ctx.reply("No matching listener on " + eventId + " for mod " + modId);
        }
    }

    private static void cmdSetPosition(CommandContext ctx, String[] args) {
        if (args.length < 4) {
            ctx.reply("Usage: /events setposition <eventId> <modId> <index>");
            return;
        }
        String eventId = args[1];
        String modId = args[2];
        int index;
        try {
            index = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            ctx.reply("Not a number: " + args[3]);
            return;
        }
        if (EventManager.setPosition(eventId, modId, index)) {
            ctx.reply("Moved " + modId + "'s " + eventId + " listener to position " + index);
        } else {
            ctx.reply("No matching listener on " + eventId + " for mod " + modId);
        }
    }

    private static void cmdEventsReset(CommandContext ctx, String[] args) {
        if (args.length < 2) {
            ctx.reply("Usage: /events reset <eventId>  OR  /events reset all");
            return;
        }
        if ("all".equalsIgnoreCase(args[1])) {
            EventManager.resetAllOverrides();
            ctx.reply("All priority/position overrides cleared");
            return;
        }
        if (EventManager.resetEvent(args[1])) {
            ctx.reply("Overrides cleared for " + args[1]);
        } else {
            ctx.reply("No overrides on " + args[1]);
        }
    }

    private static void cmdToggle(CommandContext ctx, String[] args, boolean disable) {
        if (args.length < 4) {
            ctx.reply("Usage: /events " + (disable ? "disable" : "enable")
                    + " <eventId> <modId> <priority> [listenerClass]");
            return;
        }
        String eventId = args[1];
        String modId = args[2];
        EventPriority priority;
        try {
            priority = EventPriority.valueOf(args[3].toUpperCase());
        } catch (IllegalArgumentException e) {
            ctx.reply("Unknown priority: " + args[3]
                    + " — valid: LOWEST, LOW, NORMAL, HIGH, HIGHEST, MONITOR");
            return;
        }
        String listenerClass = args.length >= 5 ? args[4] : null;
        int count = disable
                ? EventManager.disable(eventId, modId, priority, listenerClass)
                : EventManager.enable(eventId, modId, priority, listenerClass);
        ctx.reply((disable ? "Disabled " : "Enabled ") + count + " listener(s) on " + eventId);
    }

    private static void cmdClear(CommandContext ctx) {
        EventManager.clearAll();
        ctx.reply("Cleared all event overrides");
    }

    private static void handleCommands(CommandContext ctx) {
        String[] args = ctx.getArgs();
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            cmdCommandsHelp(ctx);
            return;
        }
        switch (args[0].toLowerCase()) {
            case "list" -> cmdCommandsList(ctx);
            case "conflicts" -> cmdCommandsConflicts(ctx);
            case "info" -> cmdCommandsInfo(ctx, args);
            // "assign" is the plan's name; "resolve" kept as backward-compat alias.
            case "assign", "resolve" -> cmdCommandsAssign(ctx, args);
            case "reset" -> cmdCommandsReset(ctx, args);
            default -> ctx.reply("Unknown subcommand: " + args[0] + " — try /commands help");
        }
    }

    private static void cmdCommandsHelp(CommandContext ctx) {
        ctx.reply("/commands list                       — every claimed name with its owners");
        ctx.reply("/commands conflicts                  — only names with multiple claimants");
        ctx.reply("/commands info <name>                — show providers and active handler");
        ctx.reply("/commands assign <name> <modId>      — pin the bare alias to <modId>");
        ctx.reply("/commands reset <name>               — remove override, revert to default rule");
        ctx.reply("/commands reset all                  — clear every override");
    }

    private static void cmdCommandsList(CommandContext ctx) {
        for (Map.Entry<String, List<String>> e : CommandConflictResolver.allClaims().entrySet()) {
            String winner = CommandConflictResolver.resolve(e.getKey());
            ctx.reply("  " + e.getKey() + " -> " + winner
                    + " (claimants: " + String.join(", ", e.getValue()) + ")");
        }
    }

    private static void cmdCommandsConflicts(CommandContext ctx) {
        List<String> conflicts = CommandConflictResolver.conflictedNames();
        if (conflicts.isEmpty()) {
            ctx.reply("No command conflicts.");
            return;
        }
        for (String name : conflicts) {
            List<String> claimants = CommandConflictResolver.allClaims().get(name);
            String winner = CommandConflictResolver.resolve(name);
            ctx.reply("  " + name + " -> " + winner
                    + " (claimants: " + String.join(", ", claimants) + ")");
        }
    }

    private static void cmdCommandsInfo(CommandContext ctx, String[] args) {
        if (args.length < 2) {
            ctx.reply("Usage: /commands info <name>");
            return;
        }
        String name = args[1].toLowerCase();
        List<String> claimants = CommandConflictResolver.allClaims().get(name);
        if (claimants == null) {
            ctx.reply("No such command: " + name);
            return;
        }
        String winner = CommandConflictResolver.resolve(name);
        ctx.reply("/" + name);
        for (String mod : claimants) {
            boolean active = mod.equals(winner);
            String activeTag = active ? " (ACTIVE)" : "";
            ctx.reply("  -> [" + mod + "]" + activeTag);
            ctx.reply("     /" + mod + ":" + name);
        }
    }

    private static void cmdCommandsAssign(CommandContext ctx, String[] args) {
        if (args.length < 3) {
            ctx.reply("Usage: /commands assign <name> <modId>");
            return;
        }
        String name = args[1];
        String modId = args[2];
        List<String> claimants = CommandConflictResolver.allClaims().get(name.toLowerCase());
        if (claimants == null) {
            ctx.reply("No such command: " + name);
            return;
        }
        if (!claimants.contains(modId)) {
            ctx.reply(modId + " has not claimed " + name + " (claimants: "
                    + String.join(", ", claimants) + ")");
            return;
        }
        CommandConflictResolver.setOverride(name, modId);
        ctx.reply("Pinned /" + name + " to " + modId);
    }

    private static void cmdCommandsReset(CommandContext ctx, String[] args) {
        if (args.length < 2) {
            ctx.reply("Usage: /commands reset <name>  OR  /commands reset all");
            return;
        }
        if ("all".equalsIgnoreCase(args[1])) {
            int cleared = CommandConflictResolver.clearAllOverrides();
            ctx.reply("Cleared " + cleared + " command override(s)");
            return;
        }
        CommandConflictResolver.clearOverride(args[1]);
        String winner = CommandConflictResolver.resolve(args[1]);
        ctx.reply("Reset /" + args[1] + " -> " + (winner == null ? "<unclaimed>" : winner));
    }

    private static void handleMods(CommandContext ctx) {
        if (modManager == null) { ctx.reply("Mod system not initialized."); return; }
        Collection<ModContainer> all = modManager.containers();
        if (all.isEmpty()) { ctx.reply("No mods loaded."); return; }
        for (ModContainer c : all) {
            ctx.reply("  " + c.id() + " v" + c.descriptor().version() + " [" + c.state() + "]");
        }
    }

    private static void handleMod(CommandContext ctx) {
        if (modManager == null) { ctx.reply("Mod system not initialized."); return; }
        String[] args = ctx.getArgs();
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            ctx.reply("/mod <modId>                  — show state, description, dependencies");
            ctx.reply("/mod <modId> reload           — hot-reload the mod from its jar");
            ctx.reply("/mod <modId> disable          — disable without unloading");
            ctx.reply("/mod <modId> enable           — re-enable a disabled mod");
            return;
        }
        String modId = args[0];
        ModContainer c = findContainer(modId);
        if (c == null) { ctx.reply("Unknown mod: " + modId); return; }
        if (args.length == 1) { cmdModShow(ctx, c); return; }
        switch (args[1].toLowerCase()) {
            case "reload" -> cmdModReload(ctx, modId);
            case "disable" -> cmdModDisable(ctx, c);
            case "enable" -> cmdModEnable(ctx, c);
            default -> ctx.reply("Unknown subcommand: " + args[1] + " — try /mod help");
        }
    }

    private static ModContainer findContainer(String modId) {
        for (ModContainer c : modManager.containers()) {
            if (c.id().equals(modId)) return c;
        }
        return null;
    }

    private static void cmdModShow(CommandContext ctx, ModContainer c) {
        var d = c.descriptor();
        ctx.reply("id:          " + d.id());
        ctx.reply("version:     " + d.version());
        ctx.reply("name:        " + d.name());
        ctx.reply("description: " + d.description());
        ctx.reply("state:       " + c.state());
        if (!d.dependencies().isEmpty()) {
            ctx.reply("dependencies:");
            for (Map.Entry<String, String> e : d.dependencies().entrySet()) {
                ctx.reply("  " + e.getKey() + " " + e.getValue());
            }
        }
        if (c.lastError() != null) {
            ctx.reply("lastError: " + c.lastError().getClass().getSimpleName()
                    + ": " + c.lastError().getMessage());
        }
    }

    private static void cmdModReload(CommandContext ctx, String modId) {
        try {
            modManager.reload(modId);
            ctx.reply("Reloaded " + modId);
        } catch (Exception e) {
            ctx.reply("Reload failed: " + e.getMessage());
        }
    }

    private static void cmdModDisable(CommandContext ctx, ModContainer c) {
        if (c.state() != ModState.ENABLED) {
            ctx.reply(c.id() + " is not enabled (state: " + c.state() + ")");
            return;
        }
        modManager.disableOne(c.id());
        ctx.reply("Disabled " + c.id());
    }

    private static void cmdModEnable(CommandContext ctx, ModContainer c) {
        if (c.state() == ModState.ENABLED) {
            ctx.reply(c.id() + " is already enabled");
            return;
        }
        try {
            modManager.enableOne(c.id());
            ctx.reply("Enabled " + c.id());
        } catch (Exception e) {
            ctx.reply("Enable failed: " + e.getMessage());
        }
    }

    private static void handleReload(CommandContext ctx) {
        if (modManager == null) { ctx.reply("Mod system not initialized."); return; }
        int reloaded = 0, failed = 0;
        for (ModContainer c : modManager.containers()) {
            if (c.state() != ModState.ENABLED) continue;
            try { modManager.reload(c.id()); reloaded++; }
            catch (Exception e) { failed++; ctx.reply("  " + c.id() + ": " + e.getMessage()); }
        }
        ctx.reply("Reloaded " + reloaded + " mod(s)" + (failed > 0 ? ", " + failed + " failed" : ""));
    }
}
