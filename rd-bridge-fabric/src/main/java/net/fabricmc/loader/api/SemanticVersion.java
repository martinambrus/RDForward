package net.fabricmc.loader.api;

/** Auto-generated stub from fabric-loader-0.18.4.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface SemanticVersion extends net.fabricmc.loader.api.Version {
    public static final int COMPONENT_WILDCARD = -2147483648;
    int getVersionComponentCount();
    int getVersionComponent(int arg0);
    java.util.Optional getPrereleaseKey();
    java.util.Optional getBuildKey();
    boolean hasWildcard();
    default int compareTo(net.fabricmc.loader.api.SemanticVersion arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.fabricmc.loader.api.SemanticVersion.compareTo(Lnet/fabricmc/loader/api/SemanticVersion;)I");
        return 0;
    }
    static net.fabricmc.loader.api.SemanticVersion parse(java.lang.String arg0) throws net.fabricmc.loader.api.VersionParsingException {
        return null;
    }
}
