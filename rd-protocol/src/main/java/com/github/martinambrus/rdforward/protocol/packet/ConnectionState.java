package com.github.martinambrus.rdforward.protocol.packet;

/**
 * Connection states for the 1.7.2+ Netty protocol.
 *
 * The Netty rewrite introduced connection states where packet IDs
 * are scoped per state. The same packet ID can mean different things
 * in different states (e.g., 0x00 = Handshake in HANDSHAKING,
 * StatusRequest in STATUS, LoginStart in LOGIN, KeepAlive in PLAY).
 */
public enum ConnectionState {
    HANDSHAKING,
    STATUS,
    LOGIN,
    PLAY
}
