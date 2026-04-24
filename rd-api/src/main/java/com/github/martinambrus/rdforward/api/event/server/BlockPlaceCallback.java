package com.github.martinambrus.rdforward.api.event.server;

import com.github.martinambrus.rdforward.api.event.EventResult;

/**
 * Called before a block is placed. Return {@link EventResult#CANCEL} to prevent the placement.
 */
@FunctionalInterface
public interface BlockPlaceCallback {
    EventResult onBlockPlace(String playerName, int x, int y, int z, int newBlockType);
}
