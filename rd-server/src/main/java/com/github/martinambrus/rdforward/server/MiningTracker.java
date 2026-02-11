package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.Capability;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side mining progress tracker for cross-version block breaking.
 *
 * RubyDung and Classic have instant block breaking — clicking a block
 * immediately destroys it. Alpha introduces mining progress where blocks
 * take time to break based on block type and tool.
 *
 * When a pre-Alpha client sends a block break request, the server does
 * not immediately break the block. Instead:
 *   1. Starts a mining timer for (player, block position)
 *   2. Checks block hardness to determine mining duration
 *   3. Confirms the break only after the timer completes
 *   4. Cancels mining if the player moves away or starts mining another block
 *
 * Alpha clients send PlayerDigging packets with status=0 (started) and
 * status=2 (finished), so the server can verify mining duration directly.
 *
 * Thread-safe: uses ConcurrentHashMap for the per-player state.
 */
public class MiningTracker {

    /** Mining progress entry for a player actively mining a block. */
    static class MiningState {
        final int x, y, z;
        final long startTick;
        final int requiredTicks;

        MiningState(int x, int y, int z, long startTick, int requiredTicks) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.startTick = startTick;
            this.requiredTicks = requiredTicks;
        }

        boolean isComplete(long currentTick) {
            return currentTick - startTick >= requiredTicks;
        }
    }

    /** Active mining state per player (key = player username). */
    private final Map<String, MiningState> activeMining = new ConcurrentHashMap<>();

    /**
     * Start mining a block for a legacy (pre-Alpha) client.
     * Returns true if the block should break instantly (creative-style for
     * blocks with zero hardness like flowers, torches, etc.).
     *
     * @param player      the player who is mining
     * @param x           block X
     * @param y           block Y
     * @param z           block Z
     * @param blockType   the block type at (x, y, z)
     * @param currentTick current server tick
     * @return true if the block should break immediately
     */
    public boolean startMining(ConnectedPlayer player, int x, int y, int z,
                                byte blockType, long currentTick) {
        // If client supports mining progress natively, don't track server-side
        if (Capability.MINING_PROGRESS.isAvailableIn(player.getProtocolVersion())) {
            return false;
        }

        int ticks = getMiningTicks(blockType);
        if (ticks <= 0) {
            // Instant break (air, flowers, etc.)
            activeMining.remove(player.getUsername());
            return true;
        }

        activeMining.put(player.getUsername(),
            new MiningState(x, y, z, currentTick, ticks));
        return false;
    }

    /**
     * Check if a player's current mining operation is complete.
     * Returns the mining state if complete, null otherwise.
     */
    public MiningState checkComplete(ConnectedPlayer player, long currentTick) {
        MiningState state = activeMining.get(player.getUsername());
        if (state != null && state.isComplete(currentTick)) {
            activeMining.remove(player.getUsername());
            return state;
        }
        return null;
    }

    /**
     * Cancel a player's current mining operation.
     * Called when the player starts mining a different block or disconnects.
     */
    public void cancelMining(ConnectedPlayer player) {
        activeMining.remove(player.getUsername());
    }

    /**
     * Process all active mining operations, returning completed ones.
     * Called each server tick.
     */
    public void tick(long currentTick, MiningCompleteCallback callback) {
        for (Map.Entry<String, MiningState> entry : activeMining.entrySet()) {
            MiningState state = entry.getValue();
            if (state.isComplete(currentTick)) {
                activeMining.remove(entry.getKey());
                callback.onMiningComplete(entry.getKey(), state.x, state.y, state.z);
            }
        }
    }

    /**
     * Get the number of server ticks required to mine a block.
     * Based on Minecraft Alpha's hardness values with bare hands.
     *
     * TODO: Factor in tool type and efficiency when inventory system
     * is implemented. Currently uses bare-hand mining times.
     *
     * @param blockType the block ID
     * @return ticks to mine (20 ticks = 1 second), 0 for instant break
     */
    private int getMiningTicks(byte blockType) {
        int id = blockType & 0xFF;
        switch (id) {
            case 0:  return 0;   // Air — instant
            case 1:  return 30;  // Stone — 1.5s
            case 2:  return 12;  // Grass — 0.6s
            case 3:  return 10;  // Dirt — 0.5s
            case 4:  return 40;  // Cobblestone — 2.0s
            case 5:  return 40;  // Planks — 2.0s
            case 6:  return 0;   // Sapling — instant
            case 7:  return -1;  // Bedrock — unbreakable
            case 12: return 10;  // Sand — 0.5s
            case 13: return 12;  // Gravel — 0.6s
            case 14: return 60;  // Gold Ore — 3.0s
            case 15: return 60;  // Iron Ore — 3.0s
            case 16: return 60;  // Coal Ore — 3.0s
            case 17: return 40;  // Log — 2.0s
            case 18: return 4;   // Leaves — 0.2s
            case 37: return 0;   // Flower — instant
            case 38: return 0;   // Flower — instant
            case 39: return 0;   // Mushroom — instant
            case 40: return 0;   // Mushroom — instant
            case 49: return 500; // Obsidian — 25s (bare hands = 250s, but capped)
            case 50: return 0;   // Torch — instant
            case 56: return 60;  // Diamond Ore — 3.0s
            default: return 20;  // Default 1 second for unknown blocks
        }
    }

    @FunctionalInterface
    public interface MiningCompleteCallback {
        void onMiningComplete(String playerName, int x, int y, int z);
    }
}
