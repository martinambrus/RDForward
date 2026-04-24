package net.kyori.option;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Option {
    static net.kyori.option.Option booleanOption(java.lang.String arg0, boolean arg1) {
        return null;
    }
    static net.kyori.option.Option enumOption(java.lang.String arg0, java.lang.Class arg1, java.lang.Enum arg2) {
        return null;
    }
    java.lang.String id();
    default java.lang.Class type() {
        return null;
    }
    net.kyori.option.value.ValueType valueType();
    java.lang.Object defaultValue();
}
