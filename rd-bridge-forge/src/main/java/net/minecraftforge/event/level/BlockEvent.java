// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.minecraftforge.event.level;

import net.minecraftforge.eventbus.api.Event;

/**
 * Stub of Forge's {@code BlockEvent} parent. Contains {@link BreakEvent}
 * + {@link EntityPlaceEvent}. Bridge maps these to rd-api
 * {@code BLOCK_BREAK} / {@code BLOCK_PLACE} and threads cancellation back.
 */
public class BlockEvent extends Event {

    private final int x;
    private final int y;
    private final int z;
    private final int blockType;

    public BlockEvent(int x, int y, int z, int blockType) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockType = blockType;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getBlockType() { return blockType; }

    public static class BreakEvent extends BlockEvent {
        private final String playerName;
        public BreakEvent(String playerName, int x, int y, int z, int blockType) {
            super(x, y, z, blockType);
            this.playerName = playerName;
        }
        public String getPlayerName() { return playerName; }
    }

    public static class EntityPlaceEvent extends BlockEvent {
        private final String playerName;
        public EntityPlaceEvent(String playerName, int x, int y, int z, int blockType) {
            super(x, y, z, blockType);
            this.playerName = playerName;
        }
        public String getPlayerName() { return playerName; }
    }
}
