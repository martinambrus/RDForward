package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.21.2 Play state, S2C: Update Recipes (Synchronize Recipes).
 *
 * Wire format changed in 1.21.2. Old format (1.13-1.21) was VarInt count + recipe definitions.
 * New format is:
 *   [VarInt] propertySetCount  — recipe input property sets (furnace_input, etc.)
 *     [for each]: String key + VarInt itemCount + VarInt[] itemIds
 *   [VarInt] stonecutterRecipeCount
 *     [for each]: HolderSet ingredient + SlotDisplay result
 *
 * We send 0 property sets and 0 stonecutter recipes.
 */
public class UpdateRecipesPacketV768 implements Packet {

    @Override
    public int getPacketId() { return 0x7E; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, 0); // 0 recipe property sets
        McDataTypes.writeVarInt(buf, 0); // 0 stonecutter recipes
    }

    @Override
    public void read(ByteBuf buf) {
        // S2C only — no server-side decoding needed
    }
}
