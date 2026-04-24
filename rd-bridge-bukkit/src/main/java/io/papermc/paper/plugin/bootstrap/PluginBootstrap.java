package io.papermc.paper.plugin.bootstrap;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PluginBootstrap {
    void bootstrap(io.papermc.paper.plugin.bootstrap.BootstrapContext arg0);
    default org.bukkit.plugin.java.JavaPlugin createPlugin(io.papermc.paper.plugin.bootstrap.PluginProviderContext arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.plugin.bootstrap.PluginBootstrap.createPlugin(Lio/papermc/paper/plugin/bootstrap/PluginProviderContext;)Lorg/bukkit/plugin/java/JavaPlugin;");
        return null;
    }
}
