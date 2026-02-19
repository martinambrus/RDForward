package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.19.3 Play state, S2C packet 0x36: Player Info Update.
 *
 * Replaces the action-based PlayerListItem format (V47/V759) with a bitmask-based
 * action system. Multiple actions can be combined in a single packet.
 *
 * Wire format:
 *   [Byte] actions bitmask (0x01=ADD_PLAYER, 0x02=INIT_CHAT, 0x04=UPDATE_GAME_MODE,
 *                            0x08=UPDATE_LISTED, 0x10=UPDATE_LATENCY, 0x20=UPDATE_DISPLAY_NAME)
 *   [VarInt] count
 *   Per player:
 *     [UUID] (2 longs)
 *     Per set action bit:
 *       ADD_PLAYER: String name, VarInt properties count, [properties...]
 *       INIT_CHAT: Boolean hasSignatureData, [if true: signature data]
 *       UPDATE_GAME_MODE: VarInt gameMode
 *       UPDATE_LISTED: Boolean listed
 *       UPDATE_LATENCY: VarInt ping
 *       UPDATE_DISPLAY_NAME: Boolean hasDisplayName, [if true: Chat component]
 */
public class NettyPlayerInfoUpdatePacketV761 implements Packet {

    private static final byte ADD_PLAYER     = 0x01;
    private static final byte UPDATE_GAME_MODE = 0x04;
    private static final byte UPDATE_LISTED  = 0x08;
    private static final byte UPDATE_LATENCY = 0x10;

    private byte actions;
    private long uuidMsb;
    private long uuidLsb;
    private String playerName;
    private int gameMode;
    private int ping;

    public NettyPlayerInfoUpdatePacketV761() {}

    /**
     * Create an ADD_PLAYER packet with game mode, listed, and latency actions.
     * Actions bitmask: 0x01 | 0x04 | 0x08 | 0x10 = 0x1D
     */
    public static NettyPlayerInfoUpdatePacketV761 addPlayer(String uuid, String name, int gameMode, int ping) {
        NettyPlayerInfoUpdatePacketV761 pkt = new NettyPlayerInfoUpdatePacketV761();
        pkt.actions = ADD_PLAYER | UPDATE_GAME_MODE | UPDATE_LISTED | UPDATE_LATENCY;
        pkt.parseUuid(uuid);
        pkt.playerName = name;
        pkt.gameMode = gameMode;
        pkt.ping = ping;
        return pkt;
    }

    private void parseUuid(String uuidStr) {
        String noDashes = uuidStr.replace("-", "");
        uuidMsb = Long.parseUnsignedLong(noDashes.substring(0, 16), 16);
        uuidLsb = Long.parseUnsignedLong(noDashes.substring(16, 32), 16);
    }

    @Override
    public int getPacketId() { return 0x36; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(actions);
        McDataTypes.writeVarInt(buf, 1); // 1 entry

        buf.writeLong(uuidMsb);
        buf.writeLong(uuidLsb);

        // ADD_PLAYER (0x01)
        if ((actions & ADD_PLAYER) != 0) {
            McDataTypes.writeVarIntString(buf, playerName);
            McDataTypes.writeVarInt(buf, 0); // 0 properties
        }

        // INIT_CHAT (0x02) — not set

        // UPDATE_GAME_MODE (0x04)
        if ((actions & UPDATE_GAME_MODE) != 0) {
            McDataTypes.writeVarInt(buf, gameMode);
        }

        // UPDATE_LISTED (0x08)
        if ((actions & UPDATE_LISTED) != 0) {
            buf.writeBoolean(true); // listed
        }

        // UPDATE_LATENCY (0x10)
        if ((actions & UPDATE_LATENCY) != 0) {
            McDataTypes.writeVarInt(buf, ping);
        }

        // UPDATE_DISPLAY_NAME (0x20) — not set
    }

    @Override
    public void read(ByteBuf buf) {
        // S2C only — skip all remaining bytes
        buf.skipBytes(buf.readableBytes());
    }

    public String getPlayerName() { return playerName; }
}
