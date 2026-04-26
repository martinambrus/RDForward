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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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

    private ServerSupport() {}
}
