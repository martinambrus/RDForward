package com.github.martinambrus.rdforward.api.client;

/**
 * Read-only view of the client-side world. World mutation happens only on
 * the server; clients observe the world through this interface for raycasts,
 * waypoint marker visibility checks, minimap rendering, and similar
 * passive queries.
 */
public interface ClientWorld {

    int getWidth();

    int getHeight();

    int getDepth();

    boolean isSolid(int x, int y, int z);

    boolean isInBounds(int x, int y, int z);
}
