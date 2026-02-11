package com.github.martinambrus.rdforward.client.ui;

/**
 * Skeleton for the graphical server browser screen.
 *
 * Will allow players to:
 * - See a list of saved servers (name, address, last ping)
 * - Add, edit, and remove servers
 * - Connect to a selected server
 * - Refresh server status (ping + player count)
 *
 * The server list is persisted to a file (e.g., servers.dat or servers.json).
 *
 * This is a placeholder for the Alpha stage. The actual implementation
 * will be built when the Alpha client's graphical UI system is ready.
 * Currently, server connection is handled via CLI (--server flag) or
 * the F6 key toggle in RubyDungMixin.
 */
public abstract class ServerListScreen implements GameScreen {

    /**
     * A saved server entry.
     */
    public static class ServerEntry {
        private String name;
        private String address;
        private int port;

        public ServerEntry(String name, String address, int port) {
            this.name = name;
            this.address = address;
            this.port = port;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
    }

    /**
     * Load the server list from persistent storage.
     */
    protected abstract java.util.List<ServerEntry> loadServers();

    /**
     * Save the server list to persistent storage.
     */
    protected abstract void saveServers(java.util.List<ServerEntry> servers);

    /**
     * Ping a server to check its status (online, player count, MOTD).
     * Should run asynchronously to avoid blocking the render thread.
     */
    protected abstract void pingServer(ServerEntry server);
}
