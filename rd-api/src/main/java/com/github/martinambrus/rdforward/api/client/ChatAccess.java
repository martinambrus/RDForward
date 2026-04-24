package com.github.martinambrus.rdforward.api.client;

/**
 * Client-side chat helper. Lets mods display local-only messages in the
 * chat HUD without round-tripping through the server, and send chat
 * messages to the server as if the player had typed them.
 */
public interface ChatAccess {

    /** Display a message only in the local chat overlay. Not sent to the server. */
    void addLocalMessage(String message);

    /** Send a chat message to the server as the local player. */
    void sendMessage(String message);
}
