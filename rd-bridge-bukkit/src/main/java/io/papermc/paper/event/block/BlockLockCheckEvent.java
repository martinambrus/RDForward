package io.papermc.paper.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class BlockLockCheckEvent extends org.bukkit.event.block.BlockEvent {
    public BlockLockCheckEvent(org.bukkit.block.Block arg0, org.bukkit.entity.Player arg1, net.kyori.adventure.text.Component arg2, net.kyori.adventure.sound.Sound arg3) { super((org.bukkit.block.Block) null); }
    public BlockLockCheckEvent() { super((org.bukkit.block.Block) null); }
    public io.papermc.paper.block.LockableTileState getBlockState() {
        return null;
    }
    public org.bukkit.entity.Player getPlayer() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getKeyItem() {
        return null;
    }
    public void setKeyItem(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.block.BlockLockCheckEvent.setKeyItem(Lorg/bukkit/inventory/ItemStack;)V");
    }
    public void resetKeyItem() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.block.BlockLockCheckEvent.resetKeyItem()V");
    }
    public boolean isUsingCustomKeyItemStack() {
        return false;
    }
    public org.bukkit.event.Event$Result getResult() {
        return null;
    }
    public void setResult(org.bukkit.event.Event$Result arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.block.BlockLockCheckEvent.setResult(Lorg/bukkit/event/Event$Result;)V");
    }
    public void denyWithMessageAndSound(net.kyori.adventure.text.Component arg0, net.kyori.adventure.sound.Sound arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.block.BlockLockCheckEvent.denyWithMessageAndSound(Lnet/kyori/adventure/text/Component;Lnet/kyori/adventure/sound/Sound;)V");
    }
    public net.kyori.adventure.text.Component getLockedMessage() {
        return null;
    }
    public void setLockedMessage(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.block.BlockLockCheckEvent.setLockedMessage(Lnet/kyori/adventure/text/Component;)V");
    }
    public net.kyori.adventure.sound.Sound getLockedSound() {
        return null;
    }
    public void setLockedSound(net.kyori.adventure.sound.Sound arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.block.BlockLockCheckEvent.setLockedSound(Lnet/kyori/adventure/sound/Sound;)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
