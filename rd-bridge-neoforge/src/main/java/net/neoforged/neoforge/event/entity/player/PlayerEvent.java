package net.neoforged.neoforge.event.entity.player;

/**
 * NeoForge's player event parent. Extends the Forge parent; nested events
 * extend the matching Forge ones so a single bus dispatch feeds subscribers
 * targeting either package.
 */
public class PlayerEvent extends net.minecraftforge.event.entity.player.PlayerEvent {

    public PlayerEvent(String playerName) { super(playerName); }

    public static class PlayerLoggedInEvent extends net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent {
        public PlayerLoggedInEvent(String playerName) { super(playerName); }
    }

    public static class PlayerLoggedOutEvent extends net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent {
        public PlayerLoggedOutEvent(String playerName) { super(playerName); }
    }
}
