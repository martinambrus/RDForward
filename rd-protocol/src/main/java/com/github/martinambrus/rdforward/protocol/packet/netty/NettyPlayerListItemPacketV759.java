package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.19 Play state, S2C packet 0x34: Player List Item.
 *
 * Same as V47 but ADD_PLAYER action gains an Optional ProfilePublicKey
 * (boolean false = absent) after the display name.
 */
public class NettyPlayerListItemPacketV759 implements Packet {

    public static final int ADD_PLAYER = 0;
    public static final int REMOVE_PLAYER = 4;
    public static final int UPDATE_LATENCY = 2;

    private int action;
    private long uuidMsb;
    private long uuidLsb;
    private String playerName;
    private int gameMode;
    private int ping;

    public NettyPlayerListItemPacketV759() {}

    public static NettyPlayerListItemPacketV759 addPlayer(String uuid, String name, int gameMode, int ping) {
        NettyPlayerListItemPacketV759 pkt = new NettyPlayerListItemPacketV759();
        pkt.action = ADD_PLAYER;
        pkt.parseUuid(uuid);
        pkt.playerName = name;
        pkt.gameMode = gameMode;
        pkt.ping = ping;
        return pkt;
    }

    public static NettyPlayerListItemPacketV759 removePlayer(String uuid) {
        NettyPlayerListItemPacketV759 pkt = new NettyPlayerListItemPacketV759();
        pkt.action = REMOVE_PLAYER;
        pkt.parseUuid(uuid);
        return pkt;
    }

    private void parseUuid(String uuidStr) {
        String noDashes = uuidStr.replace("-", "");
        uuidMsb = Long.parseUnsignedLong(noDashes.substring(0, 16), 16);
        uuidLsb = Long.parseUnsignedLong(noDashes.substring(16, 32), 16);
    }

    @Override
    public int getPacketId() { return 0x34; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, action);
        McDataTypes.writeVarInt(buf, 1); // 1 entry

        buf.writeLong(uuidMsb);
        buf.writeLong(uuidLsb);

        switch (action) {
            case ADD_PLAYER:
                McDataTypes.writeVarIntString(buf, playerName);
                McDataTypes.writeVarInt(buf, 0); // 0 properties
                McDataTypes.writeVarInt(buf, gameMode);
                McDataTypes.writeVarInt(buf, ping);
                buf.writeBoolean(false); // no display name
                buf.writeBoolean(false); // no profile public key
                break;
            case REMOVE_PLAYER:
                // No extra data
                break;
            case UPDATE_LATENCY:
                McDataTypes.writeVarInt(buf, ping);
                break;
        }
    }

    @Override
    public void read(ByteBuf buf) {
        action = McDataTypes.readVarInt(buf);
        int count = McDataTypes.readVarInt(buf);
        for (int i = 0; i < count; i++) {
            buf.readLong(); // uuid msb
            buf.readLong(); // uuid lsb
            buf.skipBytes(buf.readableBytes());
        }
    }

    public int getAction() { return action; }
    public String getPlayerName() { return playerName; }
}
