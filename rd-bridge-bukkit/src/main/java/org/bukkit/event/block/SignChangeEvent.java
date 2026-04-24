package org.bukkit.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class SignChangeEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    public SignChangeEvent(org.bukkit.block.Block arg0, org.bukkit.entity.Player arg1, java.util.List arg2, org.bukkit.block.sign.Side arg3) { super((org.bukkit.block.Block) null); }
    public SignChangeEvent(org.bukkit.block.Block arg0, org.bukkit.entity.Player arg1, java.util.List arg2) { super((org.bukkit.block.Block) null); }
    public SignChangeEvent(org.bukkit.block.Block arg0, org.bukkit.entity.Player arg1, java.lang.String[] arg2) { super((org.bukkit.block.Block) null); }
    public SignChangeEvent(org.bukkit.block.Block arg0, org.bukkit.entity.Player arg1, java.lang.String[] arg2, org.bukkit.block.sign.Side arg3) { super((org.bukkit.block.Block) null); }
    public SignChangeEvent() { super((org.bukkit.block.Block) null); }
    public org.bukkit.entity.Player getPlayer() {
        return null;
    }
    public java.util.List lines() {
        return java.util.Collections.emptyList();
    }
    public net.kyori.adventure.text.Component line(int arg0) throws java.lang.IndexOutOfBoundsException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.SignChangeEvent.line(I)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    public void line(int arg0, net.kyori.adventure.text.Component arg1) throws java.lang.IndexOutOfBoundsException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.SignChangeEvent.line(ILnet/kyori/adventure/text/Component;)V");
    }
    public java.lang.String[] getLines() {
        return new java.lang.String[0];
    }
    public java.lang.String getLine(int arg0) throws java.lang.IndexOutOfBoundsException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.SignChangeEvent.getLine(I)Ljava/lang/String;");
        return null;
    }
    public void setLine(int arg0, java.lang.String arg1) throws java.lang.IndexOutOfBoundsException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.SignChangeEvent.setLine(ILjava/lang/String;)V");
    }
    public org.bukkit.block.sign.Side getSide() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.SignChangeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
