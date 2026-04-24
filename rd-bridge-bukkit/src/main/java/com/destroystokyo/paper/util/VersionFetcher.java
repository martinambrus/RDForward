package com.destroystokyo.paper.util;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface VersionFetcher {
    long getCacheTime();
    net.kyori.adventure.text.Component getVersionMessage();
    default net.kyori.adventure.text.Component getVersionMessage(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.util.VersionFetcher.getVersionMessage(Ljava/lang/String;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
}
