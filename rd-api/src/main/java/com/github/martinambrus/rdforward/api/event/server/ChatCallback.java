package com.github.martinambrus.rdforward.api.event.server;

import com.github.martinambrus.rdforward.api.event.EventResult;

/**
 * Called when a chat message is received from a player.
 * Return {@link EventResult#CANCEL} to suppress the message from being broadcast.
 *
 * <p>The message can be inspected but not modified through this callback.
 * To modify messages, cancel and re-broadcast with a new message.
 */
@FunctionalInterface
public interface ChatCallback {
    EventResult onChat(String playerName, String message);
}
