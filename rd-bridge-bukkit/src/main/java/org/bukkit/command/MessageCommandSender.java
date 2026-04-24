package org.bukkit.command;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface MessageCommandSender extends org.bukkit.command.CommandSender {
    default void sendMessage(java.lang.String[] arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.MessageCommandSender.sendMessage([Ljava/lang/String;)V");
    }
    default void sendMessage(java.util.UUID arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.MessageCommandSender.sendMessage(Ljava/util/UUID;Ljava/lang/String;)V");
    }
    default void sendMessage(java.util.UUID arg0, java.lang.String[] arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.MessageCommandSender.sendMessage(Ljava/util/UUID;[Ljava/lang/String;)V");
    }
    default org.bukkit.Server getServer() {
        return null;
    }
    default net.kyori.adventure.text.Component name() {
        return null;
    }
    default java.lang.String getName() {
        return null;
    }
    default boolean isOp() {
        return false;
    }
    default void setOp(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.MessageCommandSender.setOp(Z)V");
    }
    default boolean isPermissionSet(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.MessageCommandSender.isPermissionSet(Ljava/lang/String;)Z");
        return false;
    }
    default boolean isPermissionSet(org.bukkit.permissions.Permission arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.MessageCommandSender.isPermissionSet(Lorg/bukkit/permissions/Permission;)Z");
        return false;
    }
    default boolean hasPermission(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.MessageCommandSender.hasPermission(Ljava/lang/String;)Z");
        return false;
    }
    default boolean hasPermission(org.bukkit.permissions.Permission arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.MessageCommandSender.hasPermission(Lorg/bukkit/permissions/Permission;)Z");
        return false;
    }
    default org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin arg0, java.lang.String arg1, boolean arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.MessageCommandSender.addAttachment(Lorg/bukkit/plugin/Plugin;Ljava/lang/String;Z)Lorg/bukkit/permissions/PermissionAttachment;");
        return null;
    }
    default org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.MessageCommandSender.addAttachment(Lorg/bukkit/plugin/Plugin;)Lorg/bukkit/permissions/PermissionAttachment;");
        return null;
    }
    default org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin arg0, java.lang.String arg1, boolean arg2, int arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.MessageCommandSender.addAttachment(Lorg/bukkit/plugin/Plugin;Ljava/lang/String;ZI)Lorg/bukkit/permissions/PermissionAttachment;");
        return null;
    }
    default org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin arg0, int arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.MessageCommandSender.addAttachment(Lorg/bukkit/plugin/Plugin;I)Lorg/bukkit/permissions/PermissionAttachment;");
        return null;
    }
    default void removeAttachment(org.bukkit.permissions.PermissionAttachment arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.MessageCommandSender.removeAttachment(Lorg/bukkit/permissions/PermissionAttachment;)V");
    }
    default void recalculatePermissions() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.MessageCommandSender.recalculatePermissions()V");
    }
    default java.util.Set getEffectivePermissions() {
        return java.util.Collections.emptySet();
    }
    default org.bukkit.command.CommandSender$Spigot spigot() {
        return null;
    }
}
