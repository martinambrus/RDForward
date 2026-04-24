package io.papermc.paper.plugin.bootstrap;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PluginProviderContext {
    io.papermc.paper.plugin.configuration.PluginMeta getConfiguration();
    java.nio.file.Path getDataDirectory();
    net.kyori.adventure.text.logger.slf4j.ComponentLogger getLogger();
    java.nio.file.Path getPluginSource();
}
