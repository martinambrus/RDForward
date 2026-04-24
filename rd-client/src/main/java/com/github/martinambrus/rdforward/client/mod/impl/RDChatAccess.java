package com.github.martinambrus.rdforward.client.mod.impl;

import com.github.martinambrus.rdforward.api.client.ChatAccess;
import com.github.martinambrus.rdforward.client.ChatRenderer;
import com.github.martinambrus.rdforward.multiplayer.RDClient;

/**
 * Client-side {@link ChatAccess} backed by {@link ChatRenderer} for local
 * messages and {@link RDClient} for outbound chat. A null RDClient is
 * tolerated (singleplayer preview) — sendMessage becomes a no-op.
 */
public final class RDChatAccess implements ChatAccess {

    @Override
    public void addLocalMessage(String message) {
        if (message == null) return;
        ChatRenderer.addMessage(message);
    }

    @Override
    public void sendMessage(String message) {
        if (message == null) return;
        RDClient client = RDClient.getInstance();
        if (client == null) return;
        client.sendChat(message);
    }
}
