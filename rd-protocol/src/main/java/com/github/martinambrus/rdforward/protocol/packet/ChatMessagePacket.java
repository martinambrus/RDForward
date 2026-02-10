package com.github.martinambrus.rdforward.protocol.packet;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * Simple chat message packet.
 *
 * Client -> Server: player sends a message (senderId is ignored, server uses session)
 * Server -> Client: broadcast message to all players with the sender's ID
 */
public class ChatMessagePacket implements Packet {

    private int senderId;
    private String message;

    public ChatMessagePacket() {
    }

    public ChatMessagePacket(int senderId, String message) {
        this.senderId = senderId;
        this.message = message;
    }

    @Override
    public PacketType getType() {
        return PacketType.CHAT_MESSAGE;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(senderId);
        byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(msgBytes.length);
        buf.writeBytes(msgBytes);
    }

    @Override
    public void read(ByteBuf buf) {
        senderId = buf.readInt();
        int msgLength = buf.readUnsignedShort();
        byte[] msgBytes = new byte[msgLength];
        buf.readBytes(msgBytes);
        message = new String(msgBytes, StandardCharsets.UTF_8);
    }

    public int getSenderId() { return senderId; }
    public String getMessage() { return message; }
}
