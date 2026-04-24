package pocketmine.event.block;

import pocketmine.event.Cancellable;
import pocketmine.event.Event;

/**
 * Fired before a block is broken. Cancellable. Coordinates are world
 * block coordinates; {@code blockType} is the rd-api numeric block id.
 */
public class BlockBreakEvent extends Event implements Cancellable {

    private final String playerName;
    private final int x;
    private final int y;
    private final int z;
    private final int blockType;

    public BlockBreakEvent(String playerName, int x, int y, int z, int blockType) {
        this.playerName = playerName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockType = blockType;
    }

    public String getPlayerName() { return playerName; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getBlockType() { return blockType; }
}
