package io.papermc.paper.datapack;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface DatapackManager {
    void refreshPacks();
    io.papermc.paper.datapack.Datapack getPack(java.lang.String arg0);
    java.util.Collection getPacks();
    java.util.Collection getEnabledPacks();
}
