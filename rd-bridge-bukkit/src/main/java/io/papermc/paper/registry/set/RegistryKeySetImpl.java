package io.papermc.paper.registry.set;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
record RegistryKeySetImpl(io.papermc.paper.registry.RegistryKey registryKey, java.util.List values) implements io.papermc.paper.registry.set.RegistryKeySet {
    public boolean contains(io.papermc.paper.registry.TypedKey arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.registry.set.RegistryKeySetImpl.contains(Lio/papermc/paper/registry/TypedKey;)Z");
        return false;
    }
    public java.util.Collection resolve(org.bukkit.Registry arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.registry.set.RegistryKeySetImpl.resolve(Lorg/bukkit/Registry;)Ljava/util/Collection;");
        return java.util.Collections.emptyList();
    }
}
