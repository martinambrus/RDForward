package com.github.martinambrus.rdforward.protocol.packet.lce;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * LCE Chat packet (ID 3, bidirectional).
 *
 * LCE uses a structured chat format instead of Java's raw string:
 *   [short]    messageType (0 = custom text, others = localized messages)
 *   [short]    packedCounts (bits 4-7 = stringCount, bits 0-3 = intCount)
 *   [string16] stringArgs[stringCount]
 *   [int]      intArgs[intCount]
 *
 * For custom chat (type 0), stringArgs[0] is the message text.
 */
public class LCEChatPacket implements Packet {

    private short messageType;
    private List<String> stringArgs = new ArrayList<>();
    private List<Integer> intArgs = new ArrayList<>();

    public LCEChatPacket() {}

    public LCEChatPacket(short messageType, String message) {
        this.messageType = messageType;
        this.stringArgs.add(message);
    }

    @Override
    public int getPacketId() { return 0x03; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeShort(messageType);

        short packedCounts = (short) (((stringArgs.size() & 0xF) << 4) | (intArgs.size() & 0xF));
        buf.writeShort(packedCounts);

        for (String s : stringArgs) {
            McDataTypes.writeString16(buf, s);
        }
        for (int i : intArgs) {
            buf.writeInt(i);
        }
    }

    @Override
    public void read(ByteBuf buf) {
        messageType = buf.readShort();

        short packedCounts = buf.readShort();
        int stringCount = (packedCounts >> 4) & 0xF;
        int intCount = packedCounts & 0xF;

        stringArgs.clear();
        for (int i = 0; i < stringCount; i++) {
            stringArgs.add(McDataTypes.readString16(buf));
        }

        intArgs.clear();
        for (int i = 0; i < intCount; i++) {
            intArgs.add(buf.readInt());
        }
    }

    public short getMessageType() { return messageType; }
    public List<String> getStringArgs() { return stringArgs; }
    public List<Integer> getIntArgs() { return intArgs; }

    /** Get the message text (first string arg, or empty if none). */
    public String getMessage() {
        return stringArgs.isEmpty() ? "" : stringArgs.get(0);
    }
}
