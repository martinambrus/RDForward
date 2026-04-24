package net.kyori.option.value;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ValueType {
    static net.kyori.option.value.ValueType stringType() {
        return null;
    }
    static net.kyori.option.value.ValueType booleanType() {
        return null;
    }
    static net.kyori.option.value.ValueType integerType() {
        return null;
    }
    static net.kyori.option.value.ValueType doubleType() {
        return null;
    }
    static net.kyori.option.value.ValueType enumType(java.lang.Class arg0) {
        return null;
    }
    java.lang.Class type();
    java.lang.Object parse(java.lang.String arg0) throws java.lang.IllegalArgumentException;
}
