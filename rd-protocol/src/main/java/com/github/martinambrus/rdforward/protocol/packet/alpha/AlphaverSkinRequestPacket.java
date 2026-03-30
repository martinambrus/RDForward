package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alphaver protocol 0x07 (Client -> Server): Skin Request.
 *
 * Sent by the Alphaver client after login to request a player's skin.
 * Wire format: [javaUTF] username
 */
public class AlphaverSkinRequestPacket implements Packet {

    private String username;

    public AlphaverSkinRequestPacket() {}

    @Override
    public int getPacketId() { return 0x07; }

    @Override
    public void read(ByteBuf buf) {
        username = McDataTypes.readJavaUTF(buf);
    }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeJavaUTF(buf, username);
    }

    public String getUsername() { return username; }
}
