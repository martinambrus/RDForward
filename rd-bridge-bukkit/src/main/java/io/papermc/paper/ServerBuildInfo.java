package io.papermc.paper;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ServerBuildInfo {
    public static final net.kyori.adventure.key.Key BRAND_PAPER_ID = null;
    static io.papermc.paper.ServerBuildInfo buildInfo() {
        return null;
    }
    net.kyori.adventure.key.Key brandId();
    boolean isBrandCompatible(net.kyori.adventure.key.Key arg0);
    java.lang.String brandName();
    java.lang.String minecraftVersionId();
    java.lang.String minecraftVersionName();
    java.util.OptionalInt buildNumber();
    java.time.Instant buildTime();
    java.util.Optional gitBranch();
    java.util.Optional gitCommit();
    java.lang.String asString(io.papermc.paper.ServerBuildInfo$StringRepresentation arg0);
}
