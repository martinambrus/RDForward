package net.fabricmc.loader.api.metadata;

/** Auto-generated stub from fabric-loader-0.18.4.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ModDependency {
    net.fabricmc.loader.api.metadata.ModDependency$Kind getKind();
    java.lang.String getModId();
    boolean matches(net.fabricmc.loader.api.Version arg0);
    java.util.Collection getVersionRequirements();
    java.util.List getVersionIntervals();
}
