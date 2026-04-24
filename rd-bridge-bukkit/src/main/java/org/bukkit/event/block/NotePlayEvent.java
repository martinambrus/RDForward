package org.bukkit.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class NotePlayEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    public NotePlayEvent(org.bukkit.block.Block arg0, org.bukkit.Instrument arg1, org.bukkit.Note arg2) { super((org.bukkit.block.Block) null); }
    public NotePlayEvent() { super((org.bukkit.block.Block) null); }
    public org.bukkit.Instrument getInstrument() {
        return null;
    }
    public org.bukkit.Note getNote() {
        return null;
    }
    public void setInstrument(org.bukkit.Instrument arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.NotePlayEvent.setInstrument(Lorg/bukkit/Instrument;)V");
    }
    public void setNote(org.bukkit.Note arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.NotePlayEvent.setNote(Lorg/bukkit/Note;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.NotePlayEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
