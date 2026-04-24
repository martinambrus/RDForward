package com.github.martinambrus.rdforward.api.client;

/**
 * Read-only view of the local player. Position is in world coordinates at
 * eye level (feet + 1.62). Yaw follows the RDForward internal convention
 * where 0 degrees is North.
 */
public interface ClientPlayer {

    String getName();

    double getX();

    double getY();

    double getZ();

    float getYaw();

    float getPitch();
}
