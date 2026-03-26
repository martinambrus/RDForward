package com.github.martinambrus.rdforward.server;

/**
 * Resolves spawn position for a player from saved data or default.
 * Shared by AlphaConnectionHandler, NettyConnectionHandler, and LCEConnectionHandler.
 *
 * Steps:
 * 1. Look up saved position (fixed-point short[5])
 * 2. Validate bounds, convert to double, snap feet to block surface
 * 3. Check if inside solid blocks, relocate via findSafePosition if so
 * 4. If no saved position, default to chunk center + findSafePosition
 *
 * All returned coordinates are eye-level.
 */
public final class SpawnPositionResolver {

    static final double PLAYER_EYE_HEIGHT = (double) 1.62f;

    public static final class SpawnPosition {
        public final double x, y, z;
        public final float yaw, pitch;

        SpawnPosition(double x, double y, double z, float yaw, float pitch) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }

    /**
     * Resolve spawn position from saved data or world default.
     * Returns eye-level coordinates with Classic yaw convention.
     */
    public static SpawnPosition resolve(ServerWorld world, String username, String uuid) {
        short[] savedPos = world.getSavedPlayerPosition(username, uuid);

        double spawnX, spawnY, spawnZ;
        float spawnYaw = 0, spawnPitch = 0;

        // Validate saved position is within world bounds
        if (savedPos != null) {
            double sx = savedPos[0] / 32.0;
            double sy = savedPos[1] / 32.0;
            double sz = savedPos[2] / 32.0;
            if (sx < 0 || sx >= world.getWidth()
                    || sy < PLAYER_EYE_HEIGHT || sy >= world.getHeight() + PLAYER_EYE_HEIGHT
                    || sz < 0 || sz >= world.getDepth()) {
                savedPos = null;
            }
        }

        if (savedPos != null) {
            spawnX = savedPos[0] / 32.0;
            spawnY = savedPos[1] / 32.0;
            spawnZ = savedPos[2] / 32.0;
            spawnYaw = (savedPos[3] & 0xFF) * 360.0f / 256.0f;
            spawnPitch = (savedPos[4] & 0xFF) * 360.0f / 256.0f;
            if (spawnPitch > 180.0f) spawnPitch -= 360.0f;

            // Snap feet to nearest block surface if within fixed-point tolerance.
            // Eye-level Y is saved as fixed-point (round(eyeY*32)/32), so the
            // round-trip through fixed-point + eye height subtraction can shift
            // feet up to ~0.02 blocks from their original integer position.
            double feetY = spawnY - PLAYER_EYE_HEIGHT;
            double fracFeet = feetY - Math.floor(feetY);
            if (fracFeet > 1.0 - (1.0 / 16.0)) {
                feetY = Math.ceil(feetY);
                spawnY = feetY + PLAYER_EYE_HEIGHT;
            } else if (fracFeet > 0 && fracFeet < (1.0 / 16.0)) {
                feetY = Math.floor(feetY);
                spawnY = feetY + PLAYER_EYE_HEIGHT;
            }

            // If inside solid blocks, relocate to nearest safe position
            int feetBlockX = (int) Math.floor(spawnX);
            int feetBlockY = (int) Math.floor(feetY);
            int feetBlockZ = (int) Math.floor(spawnZ);
            if (world.inBounds(feetBlockX, feetBlockY, feetBlockZ)
                    && (world.getBlock(feetBlockX, feetBlockY, feetBlockZ) != 0
                        || world.getBlock(feetBlockX, feetBlockY + 1, feetBlockZ) != 0)) {
                int[] safe = world.findSafePosition(feetBlockX, feetBlockY, feetBlockZ, 50);
                spawnX = safe[0] + 0.5;
                spawnY = safe[1] + PLAYER_EYE_HEIGHT;
                spawnZ = safe[2] + 0.5;
            }
        } else {
            // Default: center of the chunk containing the world midpoint.
            // Using chunk center avoids 1.17+ LevelRenderer BFS issues at chunk boundaries.
            int cx = ((world.getWidth() / 2) >> 4) * 16 + 8;
            int cz = ((world.getDepth() / 2) >> 4) * 16 + 8;
            int heuristicY = world.getHeight() * 2 / 3 + 1;
            int[] safe = world.findSafePosition(cx, heuristicY, cz, 50);
            spawnX = safe[0] + 0.5;
            spawnY = safe[1] + PLAYER_EYE_HEIGHT;
            spawnZ = safe[2] + 0.5;
        }

        return new SpawnPosition(spawnX, spawnY, spawnZ, spawnYaw, spawnPitch);
    }

    private SpawnPositionResolver() {} // utility class
}
