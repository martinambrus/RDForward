package com.github.martinambrus.rdforward.protocol.packet.classic;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Classic protocol 0x0D (Both directions): Message.
 *
 * Client -> Server: player ID is unused (0xFF), message is the chat text.
 * Server -> Client: player ID identifies the sender, message is the text.
 *
 * Messages are limited to 64 characters (Classic string length).
 *
 * Wire format (65 bytes payload):
 *   [1 byte]  player ID (signed; 0xFF/unused for C->S)
 *   [64 bytes] message (space-padded US-ASCII)
 */
public class MessagePacket implements Packet {

    private int playerId;
    private String message;

    public MessagePacket() {}

    public MessagePacket(int playerId, String message) {
        this.playerId = playerId;
        this.message = message;
    }

    @Override
    public int getPacketId() {
        return 0x0D;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(playerId);
        McDataTypes.writeClassicString(buf, message);
    }

    @Override
    public void read(ByteBuf buf) {
        playerId = buf.readByte(); // signed
        message = McDataTypes.readClassicString(buf);
    }

    public int getPlayerId() { return playerId; }
    public String getMessage() { return message; }
}
