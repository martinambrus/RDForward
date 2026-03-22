package com.github.martinambrus.rdforward.server.auth;

import java.math.BigInteger;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/**
 * Verifies player authentication against Mojang's session server.
 * Used for Java Edition online-mode login (Release 1.3+).
 */
public final class MojangSessionVerifier {

    private MojangSessionVerifier() {}

    private static final String SESSION_SERVER_URL =
            "https://sessionserver.mojang.com/session/minecraft/hasJoined";

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .executor(Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r, "mojang-auth");
                t.setDaemon(true);
                return t;
            }))
            .build();

    /**
     * Result of a Mojang session server verification attempt.
     * On success, uuid and name are non-null. On failure, failureMessage
     * describes what went wrong (for use in disconnect messages).
     */
    public static final class AuthResult {
        public final String uuid;
        public final String name;
        public final String failureMessage;

        private AuthResult(String uuid, String name, String failureMessage) {
            this.uuid = uuid;
            this.name = name;
            this.failureMessage = failureMessage;
        }

        public boolean isSuccess() { return uuid != null; }

        public static AuthResult success(String uuid, String name) {
            return new AuthResult(uuid, name, null);
        }

        public static AuthResult failure(String message) {
            return new AuthResult(null, null, message);
        }
    }

    /**
     * Compute the Minecraft server hash for session authentication.
     *
     * The hash is SHA-1(serverId + sharedSecret + publicKey), formatted
     * as a signed hex string using BigInteger (negative values get a "-"
     * prefix, no zero padding).
     *
     * @param serverId     the server ID string (empty string for online mode)
     * @param sharedSecret the decrypted shared secret from the client
     * @param publicKey    the server's RSA public key (encoded form)
     * @return the hex server hash string
     */
    public static String computeServerHash(String serverId, byte[] sharedSecret, byte[] publicKey) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            sha1.update(serverId.getBytes("ISO-8859-1"));
            sha1.update(sharedSecret);
            sha1.update(publicKey);
            return new BigInteger(sha1.digest()).toString(16);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute server hash", e);
        }
    }

    /**
     * Verify that a player has joined with Mojang's session server.
     *
     * Makes an async HTTP GET to sessionserver.mojang.com. The returned
     * future completes with an {@link AuthResult} on success or null if
     * the player could not be verified.
     *
     * @param username   the player's username
     * @param serverHash the computed server hash
     * @return a future completing with the auth result, or null on failure
     */
    public static CompletableFuture<AuthResult> verifyJoin(String username, String serverHash) {
        try {
            String url = SESSION_SERVER_URL
                    + "?username=" + URLEncoder.encode(username, "UTF-8")
                    + "&serverId=" + URLEncoder.encode(serverHash, "UTF-8");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() != 200) {
                            System.err.println("[AUTH] Session server returned " + response.statusCode()
                                    + " for " + username + " (body: " + response.body() + ")");
                            return AuthResult.failure(
                                    "Authentication failed. Please restart your Minecraft client.");
                        }
                        String body = response.body();
                        String rawId = extractJsonStringValue(body, "id");
                        String name = extractJsonStringValue(body, "name");
                        if (rawId == null || name == null) {
                            System.err.println("[AUTH] Malformed session server response for "
                                    + username + " (body: " + body + ")");
                            return AuthResult.failure(
                                    "Authentication failed. Please restart your Minecraft client.");
                        }
                        return AuthResult.success(formatUuid(rawId), name);
                    })
                    .exceptionally(ex -> {
                        System.err.println("[AUTH] Session server request failed for " + username
                                + ": " + ex.getMessage());
                        return AuthResult.failure(
                                "Authentication server unavailable. Please try again later.");
                    });
        } catch (Exception e) {
            System.err.println("[AUTH] Failed to build session server request: " + e.getMessage());
            return CompletableFuture.completedFuture(
                    AuthResult.failure("Authentication server unavailable. Please try again later."));
        }
    }

    /**
     * Convenience method that computes the server hash and verifies the player's
     * session with Mojang in one call. Combines {@link #computeServerHash} and
     * {@link #verifyJoin}.
     *
     * @param username          the player's username
     * @param sharedSecret      the decrypted shared secret
     * @param publicKeyEncoded  the server's RSA public key (encoded form)
     * @return a future completing with the auth result, or null on failure
     */
    public static CompletableFuture<AuthResult> verifySession(
            String username, byte[] sharedSecret, byte[] publicKeyEncoded) {
        String serverHash = computeServerHash("", sharedSecret, publicKeyEncoded);
        return verifyJoin(username, serverHash);
    }

    /**
     * Format a 32-char hex UUID into the standard 8-4-4-4-12 hyphenated form.
     */
    static String formatUuid(String hex) {
        if (hex.length() != 32) return hex;
        return hex.substring(0, 8) + "-"
                + hex.substring(8, 12) + "-"
                + hex.substring(12, 16) + "-"
                + hex.substring(16, 20) + "-"
                + hex.substring(20);
    }

    /**
     * Extract a JSON string value by key. Simple parser for {"key":"value"} patterns.
     */
    private static String extractJsonStringValue(String json, String key) {
        int nameIdx = json.indexOf("\"" + key + "\"");
        if (nameIdx < 0) return null;
        int colonIdx = json.indexOf(':', nameIdx);
        if (colonIdx < 0) return null;
        int quoteStart = json.indexOf('"', colonIdx + 1);
        int quoteEnd = json.indexOf('"', quoteStart + 1);
        if (quoteStart >= 0 && quoteEnd > quoteStart) {
            return json.substring(quoteStart + 1, quoteEnd);
        }
        return null;
    }
}
