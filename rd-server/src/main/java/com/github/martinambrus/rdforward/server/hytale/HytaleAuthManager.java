package com.github.martinambrus.rdforward.server.hytale;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Manages OAuth device flow authentication with Hytale's session service.
 *
 * The release Hytale client requires mutual authentication through
 * sessions.hytale.com. This class implements the server-side OAuth flow:
 *
 * 1. Device flow: POST oauth.accounts.hytale.com/oauth2/device/auth
 *    → user_code + verification_uri (user opens in browser)
 * 2. Poll for OAuth token: POST oauth.accounts.hytale.com/oauth2/token
 * 3. Get profiles: GET account-data.hytale.com/my-account/get-profiles
 * 4. Create game session: POST sessions.hytale.com/game-session/new
 *    → sessionToken + identityToken
 *
 * The sessionToken is used as bearer for auth grant requests during
 * client handshake. The identityToken is sent to clients in AuthGrant.
 *
 * Triggered by the server command: hytale-auth
 */
public class HytaleAuthManager {

    private static final String OAUTH_DEVICE_AUTH_URL = "https://oauth.accounts.hytale.com/oauth2/device/auth";
    private static final String OAUTH_TOKEN_URL = "https://oauth.accounts.hytale.com/oauth2/token";
    private static final String PROFILES_URL = "https://account-data.hytale.com/my-account/get-profiles";
    private static final String SESSION_SERVICE_URL = "https://sessions.hytale.com";
    private static final String CLIENT_ID = "hytale-server";
    private static final String SCOPES = "openid offline auth:server";
    private static final String USER_AGENT = "RDForward/0.2.0";
    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(30);
    private static final int POLL_INTERVAL_SECONDS = 15;
    /** Refresh OAuth + game session 5 minutes before the typical 1-hour expiry. */
    private static final long REFRESH_INTERVAL_MINUTES = 55;
    private static final Path TOKEN_FILE = Paths.get("./hytale-auth.properties");

    private static volatile HytaleAuthManager instance;

    private final HttpClient httpClient;
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "HytaleAuth-Refresh");
                t.setDaemon(true);
                return t;
            });
    private volatile ScheduledFuture<?> refreshTask;

    // OAuth tokens
    private volatile String oauthAccessToken;
    private volatile String oauthRefreshToken;

    // Game session tokens (used during client auth)
    private volatile String sessionToken;
    private volatile String identityToken;
    private volatile String serverCertFingerprint;

    // Profile
    private volatile UUID profileUuid;
    private volatile String profileUsername;

    private HytaleAuthManager() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(HTTP_TIMEOUT)
                .build();
    }

    public static HytaleAuthManager getInstance() {
        if (instance == null) {
            synchronized (HytaleAuthManager.class) {
                if (instance == null) {
                    instance = new HytaleAuthManager();
                }
            }
        }
        return instance;
    }

    public boolean isAuthenticated() {
        return sessionToken != null && identityToken != null;
    }

    public String getSessionToken() { return sessionToken; }
    public String getIdentityToken() { return identityToken; }
    public String getServerCertFingerprint() { return serverCertFingerprint; }
    public void setServerCertFingerprint(String fingerprint) { this.serverCertFingerprint = fingerprint; }

    /**
     * Start the OAuth device flow. Prints instructions to the console
     * and blocks until the user completes authorization or it times out.
     */
    public void startDeviceFlow() {
        System.out.println("[Hytale Auth] Starting OAuth device flow...");

        try {
            // Step 1: Request device authorization
            String body = "client_id=" + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8)
                    + "&scope=" + URLEncoder.encode(SCOPES, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OAUTH_DEVICE_AUTH_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("User-Agent", USER_AGENT)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.err.println("[Hytale Auth] Device auth request failed: HTTP " + response.statusCode()
                        + " - " + response.body());
                return;
            }

            // Parse device auth response (simple JSON parsing)
            String responseBody = response.body();
            String deviceCode = extractJsonString(responseBody, "device_code");
            String userCode = extractJsonString(responseBody, "user_code");
            String verificationUri = extractJsonString(responseBody, "verification_uri");
            String verificationUriComplete = extractJsonString(responseBody, "verification_uri_complete");
            int expiresIn = extractJsonInt(responseBody, "expires_in", 600);
            int interval = extractJsonInt(responseBody, "interval", POLL_INTERVAL_SECONDS);

            if (deviceCode == null || userCode == null) {
                System.err.println("[Hytale Auth] Invalid device auth response: " + responseBody);
                return;
            }

            System.out.println();
            System.out.println("========================================");
            System.out.println("  HYTALE SERVER AUTHENTICATION");
            System.out.println("========================================");
            System.out.println();
            if (verificationUriComplete != null) {
                System.out.println("  Open this URL in your browser:");
                System.out.println("  " + verificationUriComplete);
            } else {
                System.out.println("  Go to: " + verificationUri);
                System.out.println("  Enter code: " + userCode);
            }
            System.out.println();
            System.out.println("  Waiting for authorization...");
            System.out.println("========================================");
            System.out.println();

            // Step 2: Poll for token
            int pollInterval = Math.max(interval, POLL_INTERVAL_SECONDS);
            long deadline = System.currentTimeMillis() + (long) expiresIn * 1000L;

            while (System.currentTimeMillis() < deadline) {
                Thread.sleep((long) pollInterval * 1000L);

                String tokenBody = "grant_type=" + URLEncoder.encode(
                        "urn:ietf:params:oauth:grant-type:device_code", StandardCharsets.UTF_8)
                        + "&client_id=" + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8)
                        + "&device_code=" + URLEncoder.encode(deviceCode, StandardCharsets.UTF_8);

                HttpRequest tokenRequest = HttpRequest.newBuilder()
                        .uri(URI.create(OAUTH_TOKEN_URL))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("User-Agent", USER_AGENT)
                        .POST(HttpRequest.BodyPublishers.ofString(tokenBody))
                        .build();

                HttpResponse<String> tokenResponse = httpClient.send(tokenRequest,
                        HttpResponse.BodyHandlers.ofString());

                String tokenRespBody = tokenResponse.body();
                String error = extractJsonString(tokenRespBody, "error");

                if (error != null) {
                    if ("authorization_pending".equals(error)) {
                        continue;
                    }
                    if ("slow_down".equals(error)) {
                        pollInterval += 5;
                        continue;
                    }
                    System.err.println("[Hytale Auth] Authorization failed: " + error);
                    return;
                }

                if (tokenResponse.statusCode() == 200) {
                    this.oauthAccessToken = extractJsonString(tokenRespBody, "access_token");
                    this.oauthRefreshToken = extractJsonString(tokenRespBody, "refresh_token");

                    if (oauthAccessToken == null) {
                        System.err.println("[Hytale Auth] No access token in response");
                        return;
                    }

                    System.out.println("[Hytale Auth] OAuth authorization successful!");

                    // Step 3: Get profiles
                    if (!fetchProfiles()) return;

                    // Step 4: Create game session
                    if (!createGameSession()) return;

                    // Save tokens for auto-restore on restart
                    saveTokens();
                    scheduleRefresh();

                    System.out.println("[Hytale Auth] Server authenticated as " + profileUsername
                            + " — Hytale clients can now connect");
                    return;
                }
            }

            System.err.println("[Hytale Auth] Authorization timed out");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[Hytale Auth] Authorization interrupted");
        } catch (Exception e) {
            System.err.println("[Hytale Auth] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean fetchProfiles() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PROFILES_URL))
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + oauthAccessToken)
                    .header("User-Agent", USER_AGENT)
                    .timeout(HTTP_TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.err.println("[Hytale Auth] Failed to fetch profiles: HTTP " + response.statusCode()
                        + " - " + response.body());
                return false;
            }

            // Parse profiles from response
            // Response format: {"owner":"uuid","profiles":[{"uuid":"uuid","username":"name"}]}
            String body = response.body();
            // Find first profile UUID and username
            int profilesIdx = body.indexOf("\"profiles\"");
            if (profilesIdx < 0) {
                System.err.println("[Hytale Auth] No profiles in response: " + body);
                return false;
            }

            String profileUuidStr = extractJsonString(body.substring(profilesIdx), "uuid");
            String profileName = extractJsonString(body.substring(profilesIdx), "username");

            if (profileUuidStr == null || profileName == null) {
                System.err.println("[Hytale Auth] Could not parse profile from: " + body);
                return false;
            }

            this.profileUuid = UUID.fromString(profileUuidStr);
            this.profileUsername = profileName;
            System.out.println("[Hytale Auth] Using profile: " + profileName + " (" + profileUuid + ")");
            return true;

        } catch (Exception e) {
            System.err.println("[Hytale Auth] Error fetching profiles: " + e.getMessage());
            return false;
        }
    }

    private boolean createGameSession() {
        try {
            String body = "{\"uuid\":\"" + profileUuid + "\"}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SESSION_SERVICE_URL + "/game-session/new"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + oauthAccessToken)
                    .header("User-Agent", USER_AGENT)
                    .timeout(HTTP_TIMEOUT)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200 && response.statusCode() != 201) {
                System.err.println("[Hytale Auth] Failed to create game session: HTTP " + response.statusCode()
                        + " - " + response.body());
                return false;
            }

            String respBody = response.body();
            this.sessionToken = extractJsonString(respBody, "sessionToken");
            this.identityToken = extractJsonString(respBody, "identityToken");

            if (sessionToken == null || identityToken == null) {
                System.err.println("[Hytale Auth] Invalid session response: " + respBody);
                return false;
            }

            System.out.println("[Hytale Auth] Game session created successfully");
            return true;

        } catch (Exception e) {
            System.err.println("[Hytale Auth] Error creating game session: " + e.getMessage());
            return false;
        }
    }

    /**
     * Request an authorization grant from the session service.
     * Called during client login when the client sends their identity token.
     *
     * @param clientIdentityToken the client's identity token from the Connect packet
     * @return the authorization grant string, or null on failure
     */
    public String requestAuthGrant(String clientIdentityToken) {
        if (sessionToken == null) return null;

        try {
            // Server audience = our random session ID (same pattern as Hytale server)
            String serverAudience = profileUuid != null ? profileUuid.toString() : UUID.randomUUID().toString();

            String body = "{\"identityToken\":\"" + escapeJson(clientIdentityToken)
                    + "\",\"aud\":\"" + escapeJson(serverAudience) + "\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SESSION_SERVICE_URL + "/server-join/auth-grant"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + sessionToken)
                    .header("User-Agent", USER_AGENT)
                    .timeout(HTTP_TIMEOUT)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.err.println("[Hytale Auth] Auth grant request failed: HTTP " + response.statusCode()
                        + " - " + response.body());
                return null;
            }

            return extractJsonString(response.body(), "authorizationGrant");

        } catch (Exception e) {
            System.err.println("[Hytale Auth] Error requesting auth grant: " + e.getMessage());
            return null;
        }
    }

    /**
     * Exchange a server authorization grant for a server access token.
     * Called after receiving AuthToken from the client.
     *
     * @param serverAuthGrant the server authorization grant from the client's AuthToken
     * @return the server access token, or null on failure
     */
    public String exchangeAuthGrant(String serverAuthGrant) {
        if (sessionToken == null) return null;

        try {
            String fingerprint = serverCertFingerprint != null ? serverCertFingerprint : "";
            String body = "{\"authorizationGrant\":\"" + escapeJson(serverAuthGrant)
                    + "\",\"x509Fingerprint\":\"" + escapeJson(fingerprint) + "\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SESSION_SERVICE_URL + "/server-join/auth-token"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + sessionToken)
                    .header("User-Agent", USER_AGENT)
                    .timeout(HTTP_TIMEOUT)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.err.println("[Hytale Auth] Auth token exchange failed: HTTP " + response.statusCode()
                        + " - " + response.body());
                return null;
            }

            return extractJsonString(response.body(), "accessToken");

        } catch (Exception e) {
            System.err.println("[Hytale Auth] Error exchanging auth grant: " + e.getMessage());
            return null;
        }
    }

    /**
     * Refresh the OAuth access token using the refresh token.
     */
    public boolean refreshOAuthToken() {
        if (oauthRefreshToken == null) return false;

        try {
            String body = "grant_type=refresh_token"
                    + "&client_id=" + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8)
                    + "&refresh_token=" + URLEncoder.encode(oauthRefreshToken, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OAUTH_TOKEN_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("User-Agent", USER_AGENT)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.err.println("[Hytale Auth] Token refresh failed: HTTP " + response.statusCode());
                return false;
            }

            String respBody = response.body();
            this.oauthAccessToken = extractJsonString(respBody, "access_token");
            String newRefresh = extractJsonString(respBody, "refresh_token");
            if (newRefresh != null) {
                this.oauthRefreshToken = newRefresh;
                // Persist rotated refresh token immediately
                saveTokens();
            }

            return oauthAccessToken != null;

        } catch (Exception e) {
            System.err.println("[Hytale Auth] Error refreshing token: " + e.getMessage());
            return false;
        }
    }

    // -- Token persistence --

    /** Save refresh token and profile to disk for auto-restore on restart. */
    private void saveTokens() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("# Hytale server auth tokens (auto-generated, do not edit)\n");
            if (oauthRefreshToken != null) sb.append("refresh_token=").append(oauthRefreshToken).append('\n');
            if (profileUuid != null) sb.append("profile_uuid=").append(profileUuid).append('\n');
            if (profileUsername != null) sb.append("profile_username=").append(profileUsername).append('\n');
            Files.writeString(TOKEN_FILE, sb.toString());
            System.out.println("[Hytale Auth] Saved auth tokens to " + TOKEN_FILE);
        } catch (IOException e) {
            System.err.println("[Hytale Auth] Warning: could not save tokens: " + e.getMessage());
        }
    }

    /**
     * Try to restore auth from saved refresh token.
     * Called automatically when the server starts.
     * @return true if session was restored successfully
     */
    public boolean tryRestoreAuth() {
        if (!Files.exists(TOKEN_FILE)) return false;

        try {
            String content = Files.readString(TOKEN_FILE);
            String savedRefreshToken = null;
            String savedProfileUuid = null;
            String savedProfileName = null;

            for (String line : content.split("\n")) {
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty()) continue;
                int eq = line.indexOf('=');
                if (eq < 0) continue;
                String key = line.substring(0, eq);
                String val = line.substring(eq + 1);
                switch (key) {
                    case "refresh_token": savedRefreshToken = val; break;
                    case "profile_uuid": savedProfileUuid = val; break;
                    case "profile_username": savedProfileName = val; break;
                }
            }

            if (savedRefreshToken == null) return false;

            System.out.println("[Hytale Auth] Found saved auth tokens, restoring session...");
            this.oauthRefreshToken = savedRefreshToken;

            if (!refreshOAuthToken()) {
                System.err.println("[Hytale Auth] Refresh token expired — run 'hytale-auth' to re-authenticate");
                return false;
            }

            if (savedProfileUuid != null) {
                this.profileUuid = UUID.fromString(savedProfileUuid);
                this.profileUsername = savedProfileName;
            } else {
                if (!fetchProfiles()) return false;
            }

            if (!createGameSession()) return false;

            // Update saved tokens (refresh token may have been rotated)
            saveTokens();
            scheduleRefresh();

            System.out.println("[Hytale Auth] Session restored as " + profileUsername
                    + " — Hytale clients can connect");
            return true;

        } catch (Exception e) {
            System.err.println("[Hytale Auth] Error restoring auth: " + e.getMessage());
            return false;
        }
    }

    // -- Periodic token refresh --

    /** Schedule periodic refresh of OAuth token + game session. */
    private void scheduleRefresh() {
        if (refreshTask != null) {
            refreshTask.cancel(false);
        }
        refreshTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("[Hytale Auth] Refreshing OAuth token...");
                if (!refreshOAuthToken()) {
                    System.err.println("[Hytale Auth] Periodic token refresh failed — "
                            + "run 'hytale-auth' to re-authenticate");
                    return;
                }
                // Recreate game session with the fresh OAuth token
                if (!createGameSession()) {
                    System.err.println("[Hytale Auth] Periodic session refresh failed");
                    return;
                }
                saveTokens();
                System.out.println("[Hytale Auth] Token refresh successful, next in "
                        + REFRESH_INTERVAL_MINUTES + "m");
            } catch (Exception e) {
                System.err.println("[Hytale Auth] Error during periodic refresh: " + e.getMessage());
            }
        }, REFRESH_INTERVAL_MINUTES, REFRESH_INTERVAL_MINUTES, TimeUnit.MINUTES);
        System.out.println("[Hytale Auth] Scheduled token refresh every " + REFRESH_INTERVAL_MINUTES + "m");
    }

    // -- Simple JSON helpers (avoid adding a JSON dependency) --

    private static String extractJsonString(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        idx += search.length();
        // Skip whitespace and colon
        while (idx < json.length() && (json.charAt(idx) == ' ' || json.charAt(idx) == ':')) idx++;
        if (idx >= json.length() || json.charAt(idx) != '"') return null;
        idx++; // skip opening quote
        StringBuilder sb = new StringBuilder();
        while (idx < json.length() && json.charAt(idx) != '"') {
            if (json.charAt(idx) == '\\' && idx + 1 < json.length()) {
                idx++;
                char c = json.charAt(idx);
                if (c == '"' || c == '\\' || c == '/') sb.append(c);
                else if (c == 'n') sb.append('\n');
                else if (c == 't') sb.append('\t');
                else sb.append(c);
            } else {
                sb.append(json.charAt(idx));
            }
            idx++;
        }
        return sb.toString();
    }

    private static int extractJsonInt(String json, String key, int defaultValue) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return defaultValue;
        idx += search.length();
        while (idx < json.length() && (json.charAt(idx) == ' ' || json.charAt(idx) == ':')) idx++;
        StringBuilder sb = new StringBuilder();
        while (idx < json.length() && Character.isDigit(json.charAt(idx))) {
            sb.append(json.charAt(idx));
            idx++;
        }
        try {
            return Integer.parseInt(sb.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
