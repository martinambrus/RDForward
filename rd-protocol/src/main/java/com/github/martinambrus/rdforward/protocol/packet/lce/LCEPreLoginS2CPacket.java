package com.github.martinambrus.rdforward.protocol.packet.lce;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * LCE PreLogin response packet (ID 2, S2C).
 *
 * Server responds to the client's PreLogin with connected player data,
 * UGC privilege info, and server settings.
 *
 * Same wire format as LCEPreLoginC2SPacket.
 */
public class LCEPreLoginS2CPacket implements Packet {

    private short netcodeVersion;
    private String loginKey;
    private byte friendsOnlyBits;
    private int ugcPlayersVersion;
    private int playerCount;
    private long[] playerXuids;
    private byte[] uniqueSaveName = new byte[14];
    private int serverSettings;
    private byte hostIndex;
    private int texturePackId;

    public LCEPreLoginS2CPacket() {}

    public LCEPreLoginS2CPacket(short netcodeVersion, String loginKey,
                                 byte friendsOnlyBits, int ugcPlayersVersion,
                                 int playerCount, long[] playerXuids,
                                 byte[] uniqueSaveName, int serverSettings,
                                 byte hostIndex, int texturePackId) {
        this.netcodeVersion = netcodeVersion;
        this.loginKey = loginKey;
        this.friendsOnlyBits = friendsOnlyBits;
        this.ugcPlayersVersion = ugcPlayersVersion;
        this.playerCount = playerCount;
        this.playerXuids = playerXuids;
        if (uniqueSaveName != null) {
            System.arraycopy(uniqueSaveName, 0, this.uniqueSaveName, 0,
                    Math.min(uniqueSaveName.length, 14));
        }
        this.serverSettings = serverSettings;
        this.hostIndex = hostIndex;
        this.texturePackId = texturePackId;
    }

    @Override
    public int getPacketId() { return 0x02; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeShort(netcodeVersion);
        McDataTypes.writeString16(buf, loginKey);
        buf.writeByte(friendsOnlyBits);
        buf.writeInt(ugcPlayersVersion);
        buf.writeByte(playerCount);
        for (int i = 0; i < playerCount; i++) {
            buf.writeLong(playerXuids[i]);
        }
        buf.writeBytes(uniqueSaveName);
        buf.writeInt(serverSettings);
        buf.writeByte(hostIndex);
        buf.writeInt(texturePackId);
    }

    @Override
    public void read(ByteBuf buf) {
        netcodeVersion = buf.readShort();
        loginKey = McDataTypes.readString16(buf);
        friendsOnlyBits = buf.readByte();
        ugcPlayersVersion = buf.readInt();
        playerCount = buf.readUnsignedByte();
        if (playerCount > 8) playerCount = 8;
        playerXuids = new long[playerCount];
        for (int i = 0; i < playerCount; i++) {
            playerXuids[i] = buf.readLong();
        }
        buf.readBytes(uniqueSaveName);
        serverSettings = buf.readInt();
        hostIndex = buf.readByte();
        texturePackId = buf.readInt();
    }
}
