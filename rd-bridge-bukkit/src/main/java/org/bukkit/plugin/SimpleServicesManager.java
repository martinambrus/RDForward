package org.bukkit.plugin;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class SimpleServicesManager implements org.bukkit.plugin.ServicesManager {
    public SimpleServicesManager() {}
    public void register(java.lang.Class arg0, java.lang.Object arg1, org.bukkit.plugin.Plugin arg2, org.bukkit.plugin.ServicePriority arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimpleServicesManager.register(Ljava/lang/Class;Ljava/lang/Object;Lorg/bukkit/plugin/Plugin;Lorg/bukkit/plugin/ServicePriority;)V");
    }
    public void unregisterAll(org.bukkit.plugin.Plugin arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimpleServicesManager.unregisterAll(Lorg/bukkit/plugin/Plugin;)V");
    }
    public void unregister(java.lang.Class arg0, java.lang.Object arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimpleServicesManager.unregister(Ljava/lang/Class;Ljava/lang/Object;)V");
    }
    public void unregister(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimpleServicesManager.unregister(Ljava/lang/Object;)V");
    }
    public java.lang.Object load(java.lang.Class arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimpleServicesManager.load(Ljava/lang/Class;)Ljava/lang/Object;");
        return null;
    }
    public org.bukkit.plugin.RegisteredServiceProvider getRegistration(java.lang.Class arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimpleServicesManager.getRegistration(Ljava/lang/Class;)Lorg/bukkit/plugin/RegisteredServiceProvider;");
        return null;
    }
    public java.util.List getRegistrations(org.bukkit.plugin.Plugin arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimpleServicesManager.getRegistrations(Lorg/bukkit/plugin/Plugin;)Ljava/util/List;");
        return java.util.Collections.emptyList();
    }
    public java.util.List getRegistrations(java.lang.Class arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimpleServicesManager.getRegistrations(Ljava/lang/Class;)Ljava/util/List;");
        return java.util.Collections.emptyList();
    }
    public java.util.Set getKnownServices() {
        return java.util.Collections.emptySet();
    }
    public boolean isProvidedFor(java.lang.Class arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.SimpleServicesManager.isProvidedFor(Ljava/lang/Class;)Z");
        return false;
    }
}
