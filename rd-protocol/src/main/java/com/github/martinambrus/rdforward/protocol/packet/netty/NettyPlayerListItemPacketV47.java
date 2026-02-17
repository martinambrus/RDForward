package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, S2C packet 0x38: Player List Item.
 *
 * 1.8 completely redesigned this packet with action-based format.
 * Actions: 0=ADD_PLAYER, 1=UPDATE_GAMEMODE, 2=UPDATE_LATENCY,
 *          3=UPDATE_DISPLAY_NAME, 4=REMOVE_PLAYER
 *
 * Wire format:
 *   [VarInt] action
 *   [VarInt] count
 *   Per entry:
 *     [UUID] uuid (2 longs)
 *     Action-specific data:
 *       ADD_PLAYER: String name, VarInt propertyCount, [properties], VarInt gameMode, VarInt ping, boolean hasDisplayName, [optional displayName]
 *       REMOVE_PLAYER: (nothing)
 *       UPDATE_LATENCY: VarInt ping
 */
public class NettyPlayerListItemPacketV47 implements Packet {

    public static final int ADD_PLAYER = 0;
    public static final int UPDATE_GAMEMODE = 1;
    public static final int UPDATE_LATENCY = 2;
    public static final int UPDATE_DISPLAY_NAME = 3;
    public static final int REMOVE_PLAYER = 4;

    private int action;
    private long uuidMsb;
    private long uuidLsb;
    private String playerName;
    private int gameMode;
    private int ping;

    public NettyPlayerListItemPacketV47() {}

    /**
     * Create an ADD_PLAYER entry.
     */
    public static NettyPlayerListItemPacketV47 addPlayer(String uuid, String name, int gameMode, int ping) {
        NettyPlayerListItemPacketV47 pkt = new NettyPlayerListItemPacketV47();
        pkt.action = ADD_PLAYER;
        pkt.parseUuid(uuid);
        pkt.playerName = name;
        pkt.gameMode = gameMode;
        pkt.ping = ping;
        return pkt;
    }

    /**
     * Create a REMOVE_PLAYER entry.
     */
    public static NettyPlayerListItemPacketV47 removePlayer(String uuid) {
        NettyPlayerListItemPacketV47 pkt = new NettyPlayerListItemPacketV47();
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
    public int getPacketId() { return 0x38; }

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
        // Read is not needed (S2C only), but provide minimal implementation
        action = McDataTypes.readVarInt(buf);
        int count = McDataTypes.readVarInt(buf);
        for (int i = 0; i < count; i++) {
            buf.readLong(); // uuid msb
            buf.readLong(); // uuid lsb
            // Skip action-specific data
            buf.skipBytes(buf.readableBytes());
        }
    }

    public int getAction() { return action; }
    public String getPlayerName() { return playerName; }
}
