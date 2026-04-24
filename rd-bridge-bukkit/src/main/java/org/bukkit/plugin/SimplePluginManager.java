package org.bukkit.plugin;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class SimplePluginManager implements org.bukkit.plugin.PluginManager {
    public final java.util.Map permissions = java.util.Collections.emptyMap();
    public final java.util.Map defaultPerms = java.util.Collections.emptyMap();
    public final java.util.Map permSubs = java.util.Collections.emptyMap();
    public final java.util.Map defSubs = java.util.Collections.emptyMap();
    public org.bukkit.plugin.PluginManager paperPluginManager = null;
    public SimplePluginManager(org.bukkit.Server arg0, org.bukkit.command.SimpleCommandMap arg1) {}
    public SimplePluginManager() {}
    public void registerInterface(java.lang.Class arg0) throws java.lang.IllegalArgumentException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.registerInterface(Ljava/lang/Class;)V");
    }
    public org.bukkit.plugin.Plugin[] loadPlugins(java.io.File arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.loadPlugins(Ljava/io/File;)[Lorg/bukkit/plugin/Plugin;");
        return new org.bukkit.plugin.Plugin[0];
    }
    public org.bukkit.plugin.Plugin[] loadPlugins(java.io.File arg0, java.util.List arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.loadPlugins(Ljava/io/File;Ljava/util/List;)[Lorg/bukkit/plugin/Plugin;");
        return new org.bukkit.plugin.Plugin[0];
    }
    public org.bukkit.plugin.Plugin[] loadPlugins(java.io.File[] arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.loadPlugins([Ljava/io/File;)[Lorg/bukkit/plugin/Plugin;");
        return new org.bukkit.plugin.Plugin[0];
    }
    public org.bukkit.plugin.Plugin loadPlugin(java.io.File arg0) throws org.bukkit.plugin.InvalidPluginException, org.bukkit.plugin.UnknownDependencyException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.loadPlugin(Ljava/io/File;)Lorg/bukkit/plugin/Plugin;");
        return null;
    }
    public org.bukkit.plugin.Plugin getPlugin(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.getPlugin(Ljava/lang/String;)Lorg/bukkit/plugin/Plugin;");
        return null;
    }
    public org.bukkit.plugin.Plugin[] getPlugins() {
        return new org.bukkit.plugin.Plugin[0];
    }
    public boolean isPluginEnabled(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.isPluginEnabled(Ljava/lang/String;)Z");
        return false;
    }
    public boolean isPluginEnabled(org.bukkit.plugin.Plugin arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.isPluginEnabled(Lorg/bukkit/plugin/Plugin;)Z");
        return false;
    }
    public void enablePlugin(org.bukkit.plugin.Plugin arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.enablePlugin(Lorg/bukkit/plugin/Plugin;)V");
    }
    public void disablePlugins() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.disablePlugins()V");
    }
    public void disablePlugin(org.bukkit.plugin.Plugin arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.disablePlugin(Lorg/bukkit/plugin/Plugin;)V");
    }
    public void clearPlugins() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.clearPlugins()V");
    }
    public void callEvent(org.bukkit.event.Event arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.callEvent(Lorg/bukkit/event/Event;)V");
    }
    public void registerEvents(org.bukkit.event.Listener arg0, org.bukkit.plugin.Plugin arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.registerEvents(Lorg/bukkit/event/Listener;Lorg/bukkit/plugin/Plugin;)V");
    }
    public void registerEvent(java.lang.Class arg0, org.bukkit.event.Listener arg1, org.bukkit.event.EventPriority arg2, org.bukkit.plugin.EventExecutor arg3, org.bukkit.plugin.Plugin arg4) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.registerEvent(Ljava/lang/Class;Lorg/bukkit/event/Listener;Lorg/bukkit/event/EventPriority;Lorg/bukkit/plugin/EventExecutor;Lorg/bukkit/plugin/Plugin;)V");
    }
    public void registerEvent(java.lang.Class arg0, org.bukkit.event.Listener arg1, org.bukkit.event.EventPriority arg2, org.bukkit.plugin.EventExecutor arg3, org.bukkit.plugin.Plugin arg4, boolean arg5) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.registerEvent(Ljava/lang/Class;Lorg/bukkit/event/Listener;Lorg/bukkit/event/EventPriority;Lorg/bukkit/plugin/EventExecutor;Lorg/bukkit/plugin/Plugin;Z)V");
    }
    public org.bukkit.permissions.Permission getPermission(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.getPermission(Ljava/lang/String;)Lorg/bukkit/permissions/Permission;");
        return null;
    }
    public void addPermission(org.bukkit.permissions.Permission arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.addPermission(Lorg/bukkit/permissions/Permission;)V");
    }
    public void addPermission(org.bukkit.permissions.Permission arg0, boolean arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.addPermission(Lorg/bukkit/permissions/Permission;Z)V");
    }
    public java.util.Set getDefaultPermissions(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.getDefaultPermissions(Z)Ljava/util/Set;");
        return java.util.Collections.emptySet();
    }
    public void removePermission(org.bukkit.permissions.Permission arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.removePermission(Lorg/bukkit/permissions/Permission;)V");
    }
    public void removePermission(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.removePermission(Ljava/lang/String;)V");
    }
    public void recalculatePermissionDefaults(org.bukkit.permissions.Permission arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.recalculatePermissionDefaults(Lorg/bukkit/permissions/Permission;)V");
    }
    public void dirtyPermissibles() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.dirtyPermissibles()V");
    }
    public void subscribeToPermission(java.lang.String arg0, org.bukkit.permissions.Permissible arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.subscribeToPermission(Ljava/lang/String;Lorg/bukkit/permissions/Permissible;)V");
    }
    public void unsubscribeFromPermission(java.lang.String arg0, org.bukkit.permissions.Permissible arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.unsubscribeFromPermission(Ljava/lang/String;Lorg/bukkit/permissions/Permissible;)V");
    }
    public java.util.Set getPermissionSubscriptions(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.getPermissionSubscriptions(Ljava/lang/String;)Ljava/util/Set;");
        return java.util.Collections.emptySet();
    }
    public void subscribeToDefaultPerms(boolean arg0, org.bukkit.permissions.Permissible arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.subscribeToDefaultPerms(ZLorg/bukkit/permissions/Permissible;)V");
    }
    public void unsubscribeFromDefaultPerms(boolean arg0, org.bukkit.permissions.Permissible arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.unsubscribeFromDefaultPerms(ZLorg/bukkit/permissions/Permissible;)V");
    }
    public java.util.Set getDefaultPermSubscriptions(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.getDefaultPermSubscriptions(Z)Ljava/util/Set;");
        return java.util.Collections.emptySet();
    }
    public java.util.Set getPermissions() {
        return java.util.Collections.emptySet();
    }
    public boolean isTransitiveDepend(org.bukkit.plugin.PluginDescriptionFile arg0, org.bukkit.plugin.PluginDescriptionFile arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.isTransitiveDepend(Lorg/bukkit/plugin/PluginDescriptionFile;Lorg/bukkit/plugin/PluginDescriptionFile;)Z");
        return false;
    }
    public boolean useTimings() {
        return false;
    }
    public void useTimings(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.useTimings(Z)V");
    }
    public void clearPermissions() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.clearPermissions()V");
    }
    public boolean isTransitiveDependency(io.papermc.paper.plugin.configuration.PluginMeta arg0, io.papermc.paper.plugin.configuration.PluginMeta arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.isTransitiveDependency(Lio/papermc/paper/plugin/configuration/PluginMeta;Lio/papermc/paper/plugin/configuration/PluginMeta;)Z");
        return false;
    }
    public void overridePermissionManager(org.bukkit.plugin.Plugin arg0, io.papermc.paper.plugin.PermissionManager arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.overridePermissionManager(Lorg/bukkit/plugin/Plugin;Lio/papermc/paper/plugin/PermissionManager;)V");
    }
    public void addPermissions(java.util.List arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimplePluginManager.addPermissions(Ljava/util/List;)V");
    }
}
