package com.github.martinambrus.rdforward.api.event.server;

import com.github.martinambrus.rdforward.api.event.EventResult;

/**
 * Called before a block is broken. Return {@link EventResult#CANCEL} to prevent the break.
 */
@FunctionalInterface
public interface BlockBreakCallback {
    EventResult onBlockBreak(String playerName, int x, int y, int z, int blockType);
}
