package net.fabricmc.loader.api.metadata.version;

/** Auto-generated stub from fabric-loader-0.18.4.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public enum VersionComparisonOperator {
    GREATER_EQUAL, LESS_EQUAL, GREATER, LESS, EQUAL, SAME_TO_NEXT_MINOR, SAME_TO_NEXT_MAJOR;
    public final java.lang.String getSerialized() {
        return null;
    }
    public final boolean isMinInclusive() {
        return false;
    }
    public final boolean isMaxInclusive() {
        return false;
    }
    public final boolean test(net.fabricmc.loader.api.Version arg0, net.fabricmc.loader.api.Version arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.fabricmc.loader.api.metadata.version.VersionComparisonOperator.test(Lnet/fabricmc/loader/api/Version;Lnet/fabricmc/loader/api/Version;)Z");
        return false;
    }
    public boolean test(net.fabricmc.loader.api.SemanticVersion arg0, net.fabricmc.loader.api.SemanticVersion arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.fabricmc.loader.api.metadata.version.VersionComparisonOperator.test(Lnet/fabricmc/loader/api/SemanticVersion;Lnet/fabricmc/loader/api/SemanticVersion;)Z");
        return false;
    }
    public net.fabricmc.loader.api.SemanticVersion minVersion(net.fabricmc.loader.api.SemanticVersion arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.fabricmc.loader.api.metadata.version.VersionComparisonOperator.minVersion(Lnet/fabricmc/loader/api/SemanticVersion;)Lnet/fabricmc/loader/api/SemanticVersion;");
        return null;
    }
    public net.fabricmc.loader.api.SemanticVersion maxVersion(net.fabricmc.loader.api.SemanticVersion arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.fabricmc.loader.api.metadata.version.VersionComparisonOperator.maxVersion(Lnet/fabricmc/loader/api/SemanticVersion;)Lnet/fabricmc/loader/api/SemanticVersion;");
        return null;
    }
}
