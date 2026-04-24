package io.papermc.paper.connection;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PlayerConnection {
    void disconnect(net.kyori.adventure.text.Component arg0);
    boolean isConnected();
    boolean isTransferred();
    java.net.SocketAddress getAddress();
    java.net.InetSocketAddress getClientAddress();
    java.net.InetSocketAddress getVirtualHost();
    java.net.InetSocketAddress getHAProxyAddress();
}
