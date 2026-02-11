package com.github.martinambrus.rdforward.protocol.packet;

/**
 * Direction a packet can travel.
 *
 * Since Classic (and Alpha) reuse the same packet IDs for different
 * purposes depending on direction (e.g., 0x00 is PlayerIdentification
 * from client but ServerIdentification from server), the direction
 * is needed to resolve the correct Packet class.
 */
public enum PacketDirection {

    /** Packets sent from client to server. */
    CLIENT_TO_SERVER,

    /** Packets sent from server to client. */
    SERVER_TO_CLIENT
}
