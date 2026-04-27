package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x03 (bidirectional): Chat Message.
 *
 * Uses string16 encoding (vs Classic's 64-byte fixed ASCII).
 * Max 119 characters in Alpha.
 *
 * Wire format:
 *   [string16] message
 */
public class ChatPacket implements Packet {

    /** Hard cap for legacy MC chat string (Beta 1.7.3 / pre-Netty Release).
     *  The client decoder throws IOException at strings longer than this. */
    public static final int MAX_LEGACY_CHARS = 119;

    /** Latch so the warn-on-oversize stack trace fires once per JVM and
     *  isn't drowned by repeat callers.  Primary mitigation is the truncate;
     *  the trace is only to surface the unsplit code path during dev. */
    private static final java.util.concurrent.atomic.AtomicBoolean OVERSIZE_LOGGED =
            new java.util.concurrent.atomic.AtomicBoolean();

    private String message;

    public ChatPacket() {}

    public ChatPacket(String message) {
        this.message = message;
    }

    @Override
    public int getPacketId() {
        return 0x03;
    }

    @Override
    public void write(ByteBuf buf) {
        // Defensive guard: if a caller bypassed PlayerManager.splitChatMessage
        // and handed us a >119-char message, the client would disconnect with
        // "Received string length longer than maximum allowed (N > 119)".
        // Truncate so the connection survives, and emit a one-time stack
        // trace so the offending call site is visible. ChatDispatch.send and
        // PlayerManager.broadcastChat/sendChat already pre-split, so this
        // path should never fire — if it does, we want to know.
        String out = message;
        if (out != null && out.length() > MAX_LEGACY_CHARS) {
            if (OVERSIZE_LOGGED.compareAndSet(false, true)) {
                new Throwable("[ChatPacket] Oversize message reached encoder (len=" + out.length()
                        + ", cap=" + MAX_LEGACY_CHARS + "); truncating. Caller bypassed splitChatMessage.")
                        .printStackTrace();
            }
            out = out.substring(0, MAX_LEGACY_CHARS);
        }
        McDataTypes.writeStringAdaptive(buf, out);
    }

    @Override
    public void read(ByteBuf buf) {
        message = McDataTypes.readStringAdaptive(buf);
    }

    public String getMessage() { return message; }
}
