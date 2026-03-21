package com.github.martinambrus.rdforward.server.mcpe;

import java.util.UUID;

/**
 * Pure data holders for decoded MCPE C2S packets and S2C broadcast parameters.
 * Used by {@link MCPEPacketCodec} implementations to return structured data
 * from packet reads and to accept structured data for packet writes.
 */
public final class MCPEPacketData {

    private MCPEPacketData() {}

    public static class MovePlayerData {
        public final long entityId;
        public final float x, y, z, yaw, pitch;
        /** Whether Y is eye-level (v27+) or feet-level (v9-v20). */
        public final boolean isEyeLevel;

        public MovePlayerData(long entityId, float x, float y, float z,
                              float yaw, float pitch, boolean isEyeLevel) {
            this.entityId = entityId;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.isEyeLevel = isEyeLevel;
        }
    }

    public static class RemoveBlockData {
        public final int x, y, z;

        public RemoveBlockData(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static class UseItemData {
        public final int blockX, blockY, blockZ, face, itemId, meta;

        public UseItemData(int blockX, int blockY, int blockZ,
                           int face, int itemId, int meta) {
            this.blockX = blockX;
            this.blockY = blockY;
            this.blockZ = blockZ;
            this.face = face;
            this.itemId = itemId;
            this.meta = meta;
        }
    }

    public static class RequestChunkData {
        /** True if v91 radius request; false if specific chunk coordinate request. */
        public final boolean isRadiusRequest;
        /** Chunk X coordinate, or radius if {@link #isRadiusRequest} is true. */
        public final int chunkXOrRadius;
        /** Chunk Z coordinate (unused when {@link #isRadiusRequest} is true). */
        public final int chunkZ;

        public RequestChunkData(boolean isRadiusRequest, int chunkXOrRadius, int chunkZ) {
            this.isRadiusRequest = isRadiusRequest;
            this.chunkXOrRadius = chunkXOrRadius;
            this.chunkZ = chunkZ;
        }
    }

    public static class PlayerActionData {
        public final int action, x, y, z, face;

        public PlayerActionData(int action, int x, int y, int z, int face) {
            this.action = action;
            this.x = x;
            this.y = y;
            this.z = z;
            this.face = face;
        }
    }

    public static class AnimateData {
        public final int action, entityId;

        public AnimateData(int action, int entityId) {
            this.action = action;
            this.entityId = entityId;
        }
    }

    public static class PlaceBlockData {
        public final int x, y, z, blockId, meta, face;

        public PlaceBlockData(int x, int y, int z, int blockId, int meta, int face) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockId = blockId;
            this.meta = meta;
            this.face = face;
        }
    }

    public static class SpawnPlayerData {
        public final int playerId;
        public final String playerName;
        public final float x, y, z, yaw, pitch;
        public final byte[] skinData;
        public final int skinSlim;

        public SpawnPlayerData(int playerId, String playerName,
                               float x, float y, float z, float yaw, float pitch,
                               byte[] skinData, int skinSlim) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.skinData = skinData;
            this.skinSlim = skinSlim;
        }
    }

    public static class DespawnPlayerData {
        public final int entityId;
        public final UUID uuid;

        public DespawnPlayerData(int entityId, UUID uuid) {
            this.entityId = entityId;
            this.uuid = uuid;
        }
    }
}
