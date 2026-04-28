// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit;

import org.bukkit.help.HelpMap;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicFactory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.SimpleServicesManager;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.plugin.messaging.PluginMessageListenerRegistration;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Holder for process-wide defaults shared by {@link Server}'s default
 * methods. Exists because an interface cannot hold non-constant instance
 * state, and the stub {@link SimpleServicesManager} carries no useful
 * state so one singleton is sufficient for every adapter.
 */
final class ServerSupport {

    static final ServicesManager SERVICES = new SimpleServicesManager();

    /** Inert {@link HelpMap} that accepts every registration but never
     *  produces a topic. WorldEdit's WEPIF help registration runs through
     *  this without effect; help text is sourced from rd-api's command
     *  registry instead. */
    static final HelpMap HELP_MAP = new HelpMap() {
        @Override public HelpTopic getHelpTopic(String name) { return null; }
        @Override public Collection getHelpTopics() { return Collections.emptyList(); }
        @Override public void addTopic(HelpTopic topic) {}
        @Override public void clear() {}
        @Override public void registerHelpTopicFactory(Class clazz, HelpTopicFactory factory) {}
        @Override public List getIgnoredPlugins() { return Collections.emptyList(); }
    };

    /** Inert {@link Messenger} that accepts every channel registration
     *  but never delivers payloads. WorldEdit's {@code WECUI:datapack}
     *  outgoing-channel registration runs through this without effect. */
    static final Messenger MESSENGER = new Messenger() {
        @Override public boolean isReservedChannel(String channel) { return false; }
        @Override public void registerOutgoingPluginChannel(Plugin p, String c) {}
        @Override public void unregisterOutgoingPluginChannel(Plugin p, String c) {}
        @Override public void unregisterOutgoingPluginChannel(Plugin p) {}
        @Override public PluginMessageListenerRegistration registerIncomingPluginChannel(Plugin p, String c, PluginMessageListener l) { return null; }
        @Override public void unregisterIncomingPluginChannel(Plugin p, String c, PluginMessageListener l) {}
        @Override public void unregisterIncomingPluginChannel(Plugin p, String c) {}
        @Override public void unregisterIncomingPluginChannel(Plugin p) {}
        @Override public Set getOutgoingChannels() { return Collections.emptySet(); }
        @Override public Set getOutgoingChannels(Plugin p) { return Collections.emptySet(); }
        @Override public Set getIncomingChannels() { return Collections.emptySet(); }
        @Override public Set getIncomingChannels(Plugin p) { return Collections.emptySet(); }
        @Override public Set getIncomingChannelRegistrations(Plugin p) { return Collections.emptySet(); }
        @Override public Set getIncomingChannelRegistrations(String c) { return Collections.emptySet(); }
        @Override public Set getIncomingChannelRegistrations(Plugin p, String c) { return Collections.emptySet(); }
        @Override public boolean isRegistrationValid(PluginMessageListenerRegistration r) { return false; }
        @Override public boolean isIncomingChannelRegistered(Plugin p, String c) { return false; }
        @Override public boolean isOutgoingChannelRegistered(Plugin p, String c) { return false; }
        @Override public void dispatchIncomingMessage(org.bukkit.entity.Player p, String c, byte[] m) {}
        @Override public void dispatchIncomingMessage(io.papermc.paper.connection.PlayerConnection p, String c, byte[] m) {}
    };

    /** Build a minimal {@link OfflinePlayer} for {@code name} when no
     *  online player matches. Implements only {@code getName} /
     *  {@code getUniqueId} / {@code isOnline} / {@code isConnected} /
     *  {@code getPlayer}; every other accessor returns the JVM default
     *  for its return type (null/false/0). Used by
     *  {@code Server.getOfflinePlayer(String)} for the offline branch
     *  so legacy plugins (Essentials's {@code OfflinePlayer.<init>})
     *  link without us implementing every Statistic/Ban/etc. accessor. */
    static OfflinePlayer offlinePlayerStub(String name) {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + (name == null ? "" : name))
                .getBytes(StandardCharsets.UTF_8));
        return (OfflinePlayer) Proxy.newProxyInstance(
                OfflinePlayer.class.getClassLoader(),
                new Class<?>[] { OfflinePlayer.class },
                (proxy, method, args) -> handleOfflinePlayerCall(method, args, name, uuid));
    }

    private static Object handleOfflinePlayerCall(Method method, Object[] args, String name, UUID uuid) {
        switch (method.getName()) {
            case "getName": return name;
            case "getUniqueId": return uuid;
            case "isOnline": return Boolean.FALSE;
            case "isConnected": return Boolean.FALSE;
            case "getPlayer": return null;
            case "hasPlayedBefore": return Boolean.FALSE;
            case "isBanned":
                return com.github.martinambrus.rdforward.server.api.BanManager.isPlayerBanned(name);
            case "setBanned":
                // Legacy CB-1.x mutator. Essentials's /ban routes through
                // OfflinePlayer.setBanned for offline targets.
                if (args != null && args.length >= 1 && args[0] instanceof Boolean banned) {
                    if (banned) {
                        com.github.martinambrus.rdforward.server.api.BanManager.banPlayer(name);
                    } else {
                        com.github.martinambrus.rdforward.server.api.BanManager.unbanPlayer(name);
                    }
                }
                return null;
            case "isWhitelisted": return Boolean.FALSE;
            case "isOp": return Boolean.FALSE;
            case "toString": return "OfflinePlayer{" + name + "}";
            case "equals": return Boolean.valueOf(method.equals(method));
            case "hashCode": return Integer.valueOf(uuid.hashCode());
            default: break;
        }
        Class<?> rt = method.getReturnType();
        if (rt == boolean.class) return Boolean.FALSE;
        if (rt == byte.class)    return Byte.valueOf((byte) 0);
        if (rt == short.class)   return Short.valueOf((short) 0);
        if (rt == int.class)     return Integer.valueOf(0);
        if (rt == long.class)    return Long.valueOf(0L);
        if (rt == float.class)   return Float.valueOf(0f);
        if (rt == double.class)  return Double.valueOf(0d);
        if (rt == char.class)    return Character.valueOf('\0');
        return null;
    }

    private ServerSupport() {}
}
