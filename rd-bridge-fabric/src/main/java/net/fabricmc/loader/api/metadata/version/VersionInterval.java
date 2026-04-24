package net.fabricmc.loader.api.metadata.version;

/** Auto-generated stub from fabric-loader-0.18.4.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface VersionInterval {
    public static final net.fabricmc.loader.api.metadata.version.VersionInterval INFINITE = null;
    boolean isSemantic();
    net.fabricmc.loader.api.Version getMin();
    boolean isMinInclusive();
    net.fabricmc.loader.api.Version getMax();
    boolean isMaxInclusive();
    default net.fabricmc.loader.api.metadata.version.VersionInterval and(net.fabricmc.loader.api.metadata.version.VersionInterval arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.fabricmc.loader.api.metadata.version.VersionInterval.and(Lnet/fabricmc/loader/api/metadata/version/VersionInterval;)Lnet/fabricmc/loader/api/metadata/version/VersionInterval;");
        return this;
    }
    default java.util.List or(java.util.Collection arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.fabricmc.loader.api.metadata.version.VersionInterval.or(Ljava/util/Collection;)Ljava/util/List;");
        return java.util.Collections.emptyList();
    }
    default java.util.List not() {
        return java.util.Collections.emptyList();
    }
    static net.fabricmc.loader.api.metadata.version.VersionInterval and(net.fabricmc.loader.api.metadata.version.VersionInterval arg0, net.fabricmc.loader.api.metadata.version.VersionInterval arg1) {
        return null;
    }
    static java.util.List and(java.util.Collection arg0, java.util.Collection arg1) {
        return java.util.Collections.emptyList();
    }
    static java.util.List or(java.util.Collection arg0, net.fabricmc.loader.api.metadata.version.VersionInterval arg1) {
        return java.util.Collections.emptyList();
    }
    static java.util.List not(net.fabricmc.loader.api.metadata.version.VersionInterval arg0) {
        return java.util.Collections.emptyList();
    }
    static java.util.List not(java.util.Collection arg0) {
        return java.util.Collections.emptyList();
    }
}
