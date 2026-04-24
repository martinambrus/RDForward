package io.papermc.paper.tag;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PreFlattenTagRegistrar extends io.papermc.paper.plugin.lifecycle.event.registrar.Registrar {
    io.papermc.paper.registry.RegistryKey registryKey();
    java.util.Map getAllTags();
    boolean hasTag(io.papermc.paper.registry.tag.TagKey arg0);
    java.util.List getTag(io.papermc.paper.registry.tag.TagKey arg0);
    void addToTag(io.papermc.paper.registry.tag.TagKey arg0, java.util.Collection arg1);
    void setTag(io.papermc.paper.registry.tag.TagKey arg0, java.util.Collection arg1);
}
