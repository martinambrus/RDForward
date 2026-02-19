package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.19.1 Play state, S2C packet 0x62: System Chat Message.
 *
 * Changed from V759: VarInt type replaced by Boolean overlay.
 *
 * Wire format:
 *   [String] jsonMessage (chat component JSON)
 *   [Boolean] overlay (true = action bar, false = chat area)
 */
public class SystemChatPacketV760 implements Packet {

    private String jsonMessage;
    private boolean overlay;

    public SystemChatPacketV760() {}

    public SystemChatPacketV760(String jsonMessage, boolean overlay) {
        this.jsonMessage = jsonMessage;
        this.overlay = overlay;
    }

    @Override
    public int getPacketId() { return 0x62; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarIntString(buf, jsonMessage);
        buf.writeBoolean(overlay);
    }

    @Override
    public void read(ByteBuf buf) {
        jsonMessage = McDataTypes.readVarIntString(buf);
        overlay = buf.readBoolean();
    }

    public String getJsonMessage() { return jsonMessage; }
}
