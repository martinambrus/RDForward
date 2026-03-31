package com.github.martinambrus.rdforward.protocol.codec;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Encodes Packet objects into raw Minecraft wire format (no length prefix).
 *
 * Wire format: [1 byte packetId][payload]
 *
 * This is the format expected by real MC Classic/Alpha clients, which do
 * not use the 4-byte Nati length prefix that our custom client uses.
 */
public class RawPacketEncoder extends MessageToByteEncoder<Packet> {

    private volatile boolean useString16 = false;

    /** Global packet trace flag — set via /debug packets command. */
    private static volatile boolean tracePackets = false;

    public static void setTracePackets(boolean on) { tracePackets = on; }
    public static boolean isTracePackets() { return tracePackets; }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) {
        if (tracePackets) {
            int startIdx = out.writerIndex();
            out.writeByte(packet.getPacketId());
            McDataTypes.STRING16_MODE.set(useString16);
            try {
                packet.write(out);
            } finally {
                McDataTypes.STRING16_MODE.remove();
            }
            int size = out.writerIndex() - startIdx;
            System.out.println("[DEBUG/PKT] S2C 0x"
                    + Integer.toHexString(packet.getPacketId())
                    + " (" + packet.getClass().getSimpleName() + ") "
                    + size + "B → " + ctx.channel().remoteAddress());
        } else {
            out.writeByte(packet.getPacketId());
            McDataTypes.STRING16_MODE.set(useString16);
            try {
                packet.write(out);
            } finally {
                McDataTypes.STRING16_MODE.remove();
            }
        }
    }

    public void setUseString16(boolean useString16) {
        this.useString16 = useString16;
    }
}
