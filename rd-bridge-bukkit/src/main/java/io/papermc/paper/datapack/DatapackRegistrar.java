package io.papermc.paper.datapack;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface DatapackRegistrar extends io.papermc.paper.plugin.lifecycle.event.registrar.Registrar {
    boolean hasPackDiscovered(java.lang.String arg0);
    io.papermc.paper.datapack.DiscoveredDatapack getDiscoveredPack(java.lang.String arg0);
    boolean removeDiscoveredPack(java.lang.String arg0);
    java.util.Map getDiscoveredPacks();
    default io.papermc.paper.datapack.DiscoveredDatapack discoverPack(java.net.URI arg0, java.lang.String arg1) throws java.io.IOException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.datapack.DatapackRegistrar.discoverPack(Ljava/net/URI;Ljava/lang/String;)Lio/papermc/paper/datapack/DiscoveredDatapack;");
        return null;
    }
    io.papermc.paper.datapack.DiscoveredDatapack discoverPack(java.net.URI arg0, java.lang.String arg1, java.util.function.Consumer arg2) throws java.io.IOException;
    default io.papermc.paper.datapack.DiscoveredDatapack discoverPack(java.nio.file.Path arg0, java.lang.String arg1) throws java.io.IOException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.datapack.DatapackRegistrar.discoverPack(Ljava/nio/file/Path;Ljava/lang/String;)Lio/papermc/paper/datapack/DiscoveredDatapack;");
        return null;
    }
    io.papermc.paper.datapack.DiscoveredDatapack discoverPack(java.nio.file.Path arg0, java.lang.String arg1, java.util.function.Consumer arg2) throws java.io.IOException;
    io.papermc.paper.datapack.DiscoveredDatapack discoverPack(io.papermc.paper.plugin.configuration.PluginMeta arg0, java.net.URI arg1, java.lang.String arg2, java.util.function.Consumer arg3) throws java.io.IOException;
    io.papermc.paper.datapack.DiscoveredDatapack discoverPack(io.papermc.paper.plugin.configuration.PluginMeta arg0, java.nio.file.Path arg1, java.lang.String arg2, java.util.function.Consumer arg3) throws java.io.IOException;
}
