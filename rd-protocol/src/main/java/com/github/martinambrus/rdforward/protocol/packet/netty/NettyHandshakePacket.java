package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Handshaking state, C2S packet 0x00: Handshake.
 *
 * Wire format:
 *   [VarInt] protocolVersion
 *   [String] serverAddress
 *   [ushort] serverPort
 *   [VarInt] nextState (1=Status, 2=Login)
 */
public class NettyHandshakePacket implements Packet {

    private int protocolVersion;
    private String serverAddress;
    private int serverPort;
    private int nextState;

    public NettyHandshakePacket() {}

    public NettyHandshakePacket(int protocolVersion, String serverAddress, int serverPort, int nextState) {
        this.protocolVersion = protocolVersion;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.nextState = nextState;
    }

    @Override
    public int getPacketId() { return 0x00; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, protocolVersion);
        McDataTypes.writeVarIntString(buf, serverAddress);
        buf.writeShort(serverPort);
        McDataTypes.writeVarInt(buf, nextState);
    }

    @Override
    public void read(ByteBuf buf) {
        protocolVersion = McDataTypes.readVarInt(buf);
        serverAddress = McDataTypes.readVarIntString(buf);
        serverPort = buf.readUnsignedShort();
        nextState = McDataTypes.readVarInt(buf);
    }

    public int getProtocolVersion() { return protocolVersion; }
    public String getServerAddress() { return serverAddress; }
    public int getServerPort() { return serverPort; }
    public int getNextState() { return nextState; }
}
