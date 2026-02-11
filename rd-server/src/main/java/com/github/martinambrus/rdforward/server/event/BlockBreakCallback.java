package com.github.martinambrus.rdforward.server.event;

import com.github.martinambrus.rdforward.protocol.event.EventResult;

/**
 * Called before a block is broken. Return {@link EventResult#CANCEL} to prevent the break.
 */
@FunctionalInterface
public interface BlockBreakCallback {
    EventResult onBlockBreak(String playerName, int x, int y, int z, int blockType);
}
