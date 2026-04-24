package net.fabricmc.loader.api.metadata.version;

/** Auto-generated stub from fabric-loader-0.18.4.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface VersionPredicate extends java.util.function.Predicate {
    java.util.Collection getTerms();
    net.fabricmc.loader.api.metadata.version.VersionInterval getInterval();
    static net.fabricmc.loader.api.metadata.version.VersionPredicate parse(java.lang.String arg0) throws net.fabricmc.loader.api.VersionParsingException {
        return null;
    }
    static java.util.Collection parse(java.util.Collection arg0) throws net.fabricmc.loader.api.VersionParsingException {
        return java.util.Collections.emptyList();
    }
}
