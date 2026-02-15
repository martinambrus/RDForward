package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Beta protocol 0x6A (bidirectional): Confirm Transaction.
 *
 * Used to confirm or reject window click actions.
 *
 * Wire format (4 bytes payload):
 *   [byte]    window ID
 *   [short]   action number
 *   [boolean] accepted
 */
public class ConfirmTransactionPacket implements Packet {

    private int windowId;
    private short actionNum;
    private boolean accepted;

    public ConfirmTransactionPacket() {}

    public ConfirmTransactionPacket(int windowId, int actionNum, boolean accepted) {
        this.windowId = windowId;
        this.actionNum = (short) actionNum;
        this.accepted = accepted;
    }

    @Override
    public int getPacketId() {
        return 0x6A;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(windowId);
        buf.writeShort(actionNum);
        buf.writeBoolean(accepted);
    }

    @Override
    public void read(ByteBuf buf) {
        windowId = buf.readByte();
        actionNum = buf.readShort();
        accepted = buf.readBoolean();
    }

    public int getWindowId() { return windowId; }
    public short getActionNum() { return actionNum; }
    public boolean isAccepted() { return accepted; }
}
