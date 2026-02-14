package com.github.martinambrus.rdforward.protocol.packet.alpha;

/**
 * Shared interface for block placement packets across Alpha protocol versions.
 *
 * v6 (Alpha 1.2.x) and v10-v14 (Alpha 1.0.x) use different field orders
 * on the wire, but expose the same logical data. Handlers can work with
 * either version through this interface.
 */
public interface BlockPlacementData {
    int getX();
    int getY();
    int getZ();
    int getDirection();
    short getItemId();
}
