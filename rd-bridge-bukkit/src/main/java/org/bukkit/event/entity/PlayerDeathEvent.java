package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerDeathEvent extends org.bukkit.event.entity.EntityDeathEvent {
    public PlayerDeathEvent(org.bukkit.entity.Player arg0, org.bukkit.damage.DamageSource arg1, java.util.List arg2, int arg3, net.kyori.adventure.text.Component arg4, boolean arg5) { super(); }
    public PlayerDeathEvent(org.bukkit.entity.Player arg0, org.bukkit.damage.DamageSource arg1, java.util.List arg2, int arg3, int arg4, net.kyori.adventure.text.Component arg5, boolean arg6) { super(); }
    public PlayerDeathEvent(org.bukkit.entity.Player arg0, org.bukkit.damage.DamageSource arg1, java.util.List arg2, int arg3, int arg4, int arg5, int arg6, net.kyori.adventure.text.Component arg7, boolean arg8) { super(); }
    public PlayerDeathEvent(org.bukkit.entity.Player arg0, org.bukkit.damage.DamageSource arg1, java.util.List arg2, int arg3, int arg4, int arg5, int arg6, net.kyori.adventure.text.Component arg7, boolean arg8, boolean arg9) { super(); }
    public PlayerDeathEvent(org.bukkit.entity.Player arg0, org.bukkit.damage.DamageSource arg1, java.util.List arg2, int arg3, java.lang.String arg4) { super(); }
    public PlayerDeathEvent(org.bukkit.entity.Player arg0, org.bukkit.damage.DamageSource arg1, java.util.List arg2, int arg3, int arg4, java.lang.String arg5) { super(); }
    public PlayerDeathEvent(org.bukkit.entity.Player arg0, org.bukkit.damage.DamageSource arg1, java.util.List arg2, int arg3, int arg4, int arg5, int arg6, java.lang.String arg7) { super(); }
    public PlayerDeathEvent(org.bukkit.entity.Player arg0, org.bukkit.damage.DamageSource arg1, java.util.List arg2, int arg3, int arg4, int arg5, int arg6, java.lang.String arg7, boolean arg8) { super(); }
    @Override
    public org.bukkit.entity.Player getEntity() {
        return null;
    }
    public boolean getShowDeathMessages() {
        return false;
    }
    public void setShowDeathMessages(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.PlayerDeathEvent.setShowDeathMessages(Z)V");
    }
    public org.bukkit.entity.Player getPlayer() {
        return null;
    }
    public int getNewExp() {
        return 0;
    }
    public void setNewExp(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.PlayerDeathEvent.setNewExp(I)V");
    }
    public int getNewLevel() {
        return 0;
    }
    public void setNewLevel(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.PlayerDeathEvent.setNewLevel(I)V");
    }
    public int getNewTotalExp() {
        return 0;
    }
    public void setNewTotalExp(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.PlayerDeathEvent.setNewTotalExp(I)V");
    }
    public void deathMessage(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.PlayerDeathEvent.deathMessage(Lnet/kyori/adventure/text/Component;)V");
    }
    public net.kyori.adventure.text.Component deathMessage() {
        return null;
    }
    public void setDeathMessage(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.PlayerDeathEvent.setDeathMessage(Ljava/lang/String;)V");
    }
    public java.lang.String getDeathMessage() {
        return null;
    }
    public void deathScreenMessageOverride(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.PlayerDeathEvent.deathScreenMessageOverride(Lnet/kyori/adventure/text/Component;)V");
    }
    public net.kyori.adventure.text.Component deathScreenMessageOverride() {
        return null;
    }
    public boolean shouldDropExperience() {
        return false;
    }
    public void setShouldDropExperience(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.PlayerDeathEvent.setShouldDropExperience(Z)V");
    }
    public boolean getKeepLevel() {
        return false;
    }
    public void setKeepLevel(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.PlayerDeathEvent.setKeepLevel(Z)V");
    }
    public void setKeepInventory(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.PlayerDeathEvent.setKeepInventory(Z)V");
    }
    public boolean getKeepInventory() {
        return false;
    }
    public java.util.List getItemsToKeep() {
        return java.util.Collections.emptyList();
    }
}
