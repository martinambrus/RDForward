package com.github.martinambrus.rdforward.server.event;

import com.github.martinambrus.rdforward.protocol.event.EventResult;

/**
 * Called when a chat message is received from a player.
 * Return {@link EventResult#CANCEL} to suppress the message from being broadcast.
 *
 * The message can be inspected but not modified through this callback.
 * To modify messages, cancel and re-broadcast with a new message.
 */
@FunctionalInterface
public interface ChatCallback {
    EventResult onChat(String playerName, String message);
}
