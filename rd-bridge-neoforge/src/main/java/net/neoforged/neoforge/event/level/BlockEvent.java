package net.neoforged.neoforge.event.level;

/**
 * NeoForge's block event parent. Extends the Forge parent; nested events
 * extend the matching Forge nested events so a single bus dispatch feeds
 * subscribers targeting either package.
 */
public class BlockEvent extends net.minecraftforge.event.level.BlockEvent {

    public BlockEvent(int x, int y, int z, int blockType) { super(x, y, z, blockType); }

    public static class BreakEvent extends net.minecraftforge.event.level.BlockEvent.BreakEvent {
        public BreakEvent(String playerName, int x, int y, int z, int blockType) {
            super(playerName, x, y, z, blockType);
        }
    }

    public static class EntityPlaceEvent extends net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent {
        public EntityPlaceEvent(String playerName, int x, int y, int z, int blockType) {
            super(playerName, x, y, z, blockType);
        }
    }
}
