package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.13 Play state, S2C packet 0x54: Update Recipes (Declare Recipes).
 *
 * Sends recipe definitions to the client. We send an empty recipe list
 * since our server doesn't implement crafting.
 *
 * Wire format:
 *   [VarInt] recipeCount (0)
 */
public class UpdateRecipesPacketV393 implements Packet {

    @Override
    public int getPacketId() { return 0x54; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, 0); // 0 recipes
    }

    @Override
    public void read(ByteBuf buf) {
        // S2C only — no server-side decoding needed
    }
}
