package net.fabricmc.loader.api;

/** Auto-generated stub from fabric-loader-0.18.4.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface MappingResolver {
    java.util.Collection getNamespaces();
    java.lang.String getCurrentRuntimeNamespace();
    java.lang.String mapClassName(java.lang.String arg0, java.lang.String arg1);
    java.lang.String unmapClassName(java.lang.String arg0, java.lang.String arg1);
    java.lang.String mapFieldName(java.lang.String arg0, java.lang.String arg1, java.lang.String arg2, java.lang.String arg3);
    java.lang.String mapMethodName(java.lang.String arg0, java.lang.String arg1, java.lang.String arg2, java.lang.String arg3);
}
