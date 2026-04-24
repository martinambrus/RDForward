package net.fabricmc.loader.api.metadata;

/** Auto-generated stub from fabric-loader-0.18.4.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ModMetadata {
    java.lang.String getType();
    java.lang.String getId();
    java.util.Collection getProvides();
    net.fabricmc.loader.api.Version getVersion();
    net.fabricmc.loader.api.metadata.ModEnvironment getEnvironment();
    java.util.Collection getDependencies();
    default java.util.Collection getDepends() {
        return java.util.Collections.emptyList();
    }
    default java.util.Collection getRecommends() {
        return java.util.Collections.emptyList();
    }
    default java.util.Collection getSuggests() {
        return java.util.Collections.emptyList();
    }
    default java.util.Collection getConflicts() {
        return java.util.Collections.emptyList();
    }
    default java.util.Collection getBreaks() {
        return java.util.Collections.emptyList();
    }
    java.lang.String getName();
    java.lang.String getDescription();
    java.util.Collection getAuthors();
    java.util.Collection getContributors();
    net.fabricmc.loader.api.metadata.ContactInformation getContact();
    java.util.Collection getLicense();
    java.util.Optional getIconPath(int arg0);
    boolean containsCustomValue(java.lang.String arg0);
    net.fabricmc.loader.api.metadata.CustomValue getCustomValue(java.lang.String arg0);
    java.util.Map getCustomValues();
    boolean containsCustomElement(java.lang.String arg0);
}
