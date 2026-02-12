package com.github.martinambrus.rdforward.android;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.TextInputListener;

/**
 * Android-specific server connect dialog. Uses libGDX's native text input
 * dialog (which displays an Android AlertDialog) to get the server address.
 * <p>
 * On desktop, server connect uses the {@code --server} flag or F6 toggle.
 * On Android, this dialog is shown from a UI button overlay.
 */
public class ServerConnectDialog {

    /** Callback for when the user enters a server address. */
    @FunctionalInterface
    public interface ConnectCallback {
        void onConnect(String host, int port, String username);
    }

    /**
     * Show the server address input dialog.
     *
     * @param defaultAddress pre-filled address (e.g. "localhost:25565")
     * @param defaultUsername pre-filled username for the next dialog
     * @param callback called with parsed host, port, and username when confirmed
     */
    public static void show(String defaultAddress, String defaultUsername, ConnectCallback callback) {
        Gdx.input.getTextInput(new TextInputListener() {
            @Override
            public void input(String text) {
                parseAndConnect(text, defaultUsername, callback);
            }

            @Override
            public void canceled() {
                // User cancelled — do nothing
            }
        }, "Connect to Server", defaultAddress, "host:port");
    }

    /**
     * Show the username input dialog, then connect.
     *
     * @param host     server host
     * @param port     server port
     * @param defaultUsername pre-filled username
     * @param callback called with final connection details
     */
    private static void showWithUsername(String host, int port, String defaultUsername, ConnectCallback callback) {
        Gdx.input.getTextInput(new TextInputListener() {
            @Override
            public void input(String text) {
                String username = text.trim().isEmpty() ? "" : text.trim();
                callback.onConnect(host, port, username);
            }

            @Override
            public void canceled() {
                // User cancelled — do nothing
            }
        }, "Player Name", defaultUsername, "leave blank for auto-assign");
    }

    private static void parseAndConnect(String text, String defaultUsername, ConnectCallback callback) {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) return;

        String host;
        int port = 25565;

        int colonIdx = trimmed.lastIndexOf(':');
        if (colonIdx > 0) {
            host = trimmed.substring(0, colonIdx);
            try {
                port = Integer.parseInt(trimmed.substring(colonIdx + 1));
            } catch (NumberFormatException e) {
                host = trimmed;
            }
        } else {
            host = trimmed;
        }

        final String finalHost = host;
        final int finalPort = port;
        showWithUsername(finalHost, finalPort, defaultUsername, callback);
    }
}
