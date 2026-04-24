package pocketmine.plugin;

import pocketmine.event.Listener;

/**
 * Narrow slice of PocketMine-MP's {@code PluginManager}. Only the single
 * method a PocketMine plugin commonly calls — {@code registerEvents} —
 * is exposed. The implementation inside the bridge forwards to the
 * {@code PocketMineEventAdapter} so the listener's {@code @HandleEvent}
 * methods become rd-api {@code ServerEvents} subscriptions.
 */
public interface PluginManager {

    void registerEvents(Listener listener, Plugin plugin);
}
