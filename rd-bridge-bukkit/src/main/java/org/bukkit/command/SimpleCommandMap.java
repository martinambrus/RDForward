// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.command;

/** Bukkit-shaped {@code SimpleCommandMap}. Plugins (notably WorldEdit's
 *  {@code CommandRegistration}) reflectively read this from
 *  {@link org.bukkit.plugin.SimplePluginManager#commandMap} and call
 *  {@link #register} on it. RDForward's primary command flow goes through
 *  the rd-api {@code CommandConflictResolver}; this map is a secondary
 *  registry that quietly accepts dynamic registrations so plugins that
 *  use it (for late-bound or per-instance command names) don't fall
 *  back to their own storage and emit SEVERE log lines. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class SimpleCommandMap implements org.bukkit.command.CommandMap {

    private final java.util.Map<String, org.bukkit.command.Command> known =
            new java.util.LinkedHashMap<>();

    public SimpleCommandMap(org.bukkit.Server arg0, java.util.Map arg1) {}
    public SimpleCommandMap(org.bukkit.Server arg0) {}
    public SimpleCommandMap() {}

    public void setFallbackCommands() {}

    public void registerAll(java.lang.String fallbackPrefix, java.util.List commands) {
        if (commands == null) return;
        for (Object o : commands) {
            if (o instanceof org.bukkit.command.Command cmd) {
                register(fallbackPrefix, cmd);
            }
        }
    }

    @Override
    public boolean register(java.lang.String label, org.bukkit.command.Command command) {
        return register(label, null, command);
    }

    @Override
    public boolean register(java.lang.String label, java.lang.String fallbackPrefix,
                            org.bukkit.command.Command command) {
        if (command == null) return false;
        String primary = (label == null || label.isEmpty()) ? command.getName() : label;
        if (primary == null) return false;
        boolean firstOwner = !known.containsKey(primary);
        if (firstOwner) {
            known.put(primary, command);
            command.setLabel(primary);
        } else if (fallbackPrefix != null && !fallbackPrefix.isEmpty()) {
            String prefixed = fallbackPrefix + ":" + primary;
            if (!known.containsKey(prefixed)) {
                known.put(prefixed, command);
            }
        }
        if (command.getAliases() != null) {
            for (Object a : command.getAliases()) {
                String alias = String.valueOf(a);
                if (!known.containsKey(alias)) known.put(alias, command);
            }
        }
        return firstOwner;
    }

    @Override
    public boolean dispatch(org.bukkit.command.CommandSender sender, java.lang.String commandLine)
            throws org.bukkit.command.CommandException {
        if (commandLine == null || commandLine.isEmpty()) return false;
        String[] parts = commandLine.split(" ");
        org.bukkit.command.Command cmd = known.get(parts[0]);
        if (cmd == null) return false;
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);
        try {
            return cmd.execute(sender, parts[0], args);
        } catch (Throwable t) {
            throw new org.bukkit.command.CommandException(
                    "Unhandled exception executing '" + commandLine + "'", t);
        }
    }

    @Override
    public void clearCommands() { known.clear(); }

    @Override
    public org.bukkit.command.Command getCommand(java.lang.String name) {
        return name == null ? null : known.get(name);
    }

    @Override
    public java.util.List tabComplete(org.bukkit.command.CommandSender sender, java.lang.String cmdLine) {
        return java.util.Collections.emptyList();
    }

    @Override
    public java.util.List tabComplete(org.bukkit.command.CommandSender sender, java.lang.String cmdLine,
                                      org.bukkit.Location location) {
        return java.util.Collections.emptyList();
    }

    public java.util.Collection getCommands() {
        return java.util.Collections.unmodifiableCollection(known.values());
    }

    public void registerServerAliases() {}

    public java.util.Map getKnownCommands() {
        return java.util.Collections.unmodifiableMap(known);
    }
}
