package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x01 (Client -> Server): Login Request.
 *
 * Sent after the handshake to initiate login.
 * Self-adaptive: reads mapSeed/dimension only for post-rewrite v3-v9,
 * since the decoder doesn't yet know the client's version when
 * decoding this packet.
 *
 * For Beta v10+ (Beta 1.4+), protocol number 10 clashes with pre-rewrite
 * Alpha v10 (which has no mapSeed). The PacketRegistry creates this packet
 * with forceMapSeed=true for Beta v10+ to override the self-adaptive condition.
 *
 * Wire format (v3-v9 or forceMapSeed, post-rewrite with mapSeed):
 *   [int]      protocol version
 *   [string16] username
 *   [string16] unused (empty, was password in early tests)
 *   [long]     map seed (0, not used by client)
 *   [byte]     dimension (0, not used by client)
 *
 * Wire format (v1-v2, v10-v14 pre-rewrite, no mapSeed):
 *   [int]      protocol version
 *   [string16] username
 *   [string16] unused
 *
 * Wire format (v17+, Beta 1.8+):
 *   [int]      protocol version (17)
 *   [string16] username
 *   [long]     map seed (0)
 *   [int]      game mode (0, not used server-side)
 *   [byte]     dimension (0)
 *   [byte]     difficulty (0, not used)
 *   [byte]     world height (0, not used)
 *   [byte]     max players (0, not used)
 *   Note: Beta 1.8 removed the "unused" String16 field.
 *
 * Wire format (v23, Release 1.1):
 *   Same as v17 but with [string16] level type inserted after seed and before game mode.
 *
 * Wire format (v28+, Release 1.2.1+):
 *   [int]      protocol version (28)
 *   [string16] username
 *   [string16] level type ("default")
 *   [int]      game mode (0, not used)
 *   [int]      dimension (0)
 *   [byte]     difficulty (0, not used)
 *   [byte]     world height (0, not used)
 *   [byte]     max players (0, not used)
 *   Note: seed removed, dimension changed from byte to int.
 */
public class LoginC2SPacket implements Packet {

    private int protocolVersion;
    private String username;
    private long mapSeed;
    private byte dimension;
    private final boolean forceMapSeed;

    public LoginC2SPacket() {
        this.forceMapSeed = false;
    }

    public LoginC2SPacket(boolean forceMapSeed) {
        this.forceMapSeed = forceMapSeed;
    }

    public LoginC2SPacket(int protocolVersion, String username) {
        this.protocolVersion = protocolVersion;
        this.username = username;
        this.forceMapSeed = false;
    }

    public LoginC2SPacket(int protocolVersion, String username, boolean forceMapSeed) {
        this.protocolVersion = protocolVersion;
        this.username = username;
        this.forceMapSeed = forceMapSeed;
    }

    @Override
    public int getPacketId() {
        return 0x01;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(protocolVersion);
        McDataTypes.writeStringAdaptive(buf, username);
        if (protocolVersion >= 28) {
            // Release 1.2.1+: no seed, int dimension
            McDataTypes.writeStringAdaptive(buf, "default");
            buf.writeInt(0);   // gameMode
            buf.writeInt(dimension);
            buf.writeByte(0);  // difficulty
            buf.writeByte(0);  // worldHeight
            buf.writeByte(0);  // maxPlayers
        } else if (protocolVersion >= 17) {
            // Beta 1.8+: no "unused" String16, additional fields after mapSeed
            buf.writeLong(mapSeed);
            if (protocolVersion >= 23) {
                // Release 1.1+: levelType inserted between seed and gameMode
                McDataTypes.writeStringAdaptive(buf, "default");
            }
            buf.writeInt(0);   // gameMode (not used)
            buf.writeByte(dimension);
            buf.writeByte(0);  // difficulty
            buf.writeByte(0);  // worldHeight
            buf.writeByte(0);  // maxPlayers
        } else {
            McDataTypes.writeStringAdaptive(buf, ""); // unused
            if (forceMapSeed || (protocolVersion >= 3 && protocolVersion < 10)) {
                buf.writeLong(mapSeed);
                buf.writeByte(dimension);
            }
        }
    }

    @Override
    public void read(ByteBuf buf) {
        protocolVersion = buf.readInt();
        username = McDataTypes.readStringAdaptive(buf);
        if (protocolVersion >= 28) {
            // Release 1.2.1+: no seed, int dimension
            McDataTypes.readStringAdaptive(buf); // levelType
            buf.readInt();   // gameMode
            dimension = (byte) buf.readInt();
            buf.readByte();  // difficulty
            buf.readByte();  // worldHeight
            buf.readByte();  // maxPlayers
        } else if (protocolVersion >= 17) {
            // Beta 1.8+: no "unused" String16, additional fields after mapSeed
            mapSeed = buf.readLong();
            if (protocolVersion >= 23) {
                // Release 1.1+: levelType inserted between seed and gameMode
                McDataTypes.readStringAdaptive(buf);
            }
            buf.readInt();   // gameMode (not used)
            dimension = buf.readByte();
            buf.readByte();  // difficulty
            buf.readByte();  // worldHeight
            buf.readByte();  // maxPlayers
        } else {
            McDataTypes.readStringAdaptive(buf); // unused
            if (forceMapSeed || (protocolVersion >= 3 && protocolVersion < 10)) {
                mapSeed = buf.readLong();
                dimension = buf.readByte();
            }
        }
    }

    public int getProtocolVersion() { return protocolVersion; }
    public String getUsername() { return username; }
    public long getMapSeed() { return mapSeed; }
    public byte getDimension() { return dimension; }
}
