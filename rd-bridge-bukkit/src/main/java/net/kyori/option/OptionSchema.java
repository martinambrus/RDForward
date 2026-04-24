package net.kyori.option;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface OptionSchema {
    static net.kyori.option.OptionSchema$Mutable globalSchema() {
        return null;
    }
    static net.kyori.option.OptionSchema$Mutable childSchema(net.kyori.option.OptionSchema arg0) {
        return null;
    }
    static net.kyori.option.OptionSchema$Mutable emptySchema() {
        return null;
    }
    java.util.Set knownOptions();
    boolean has(net.kyori.option.Option arg0);
    net.kyori.option.OptionState$Builder stateBuilder();
    net.kyori.option.OptionState$VersionedBuilder versionedStateBuilder();
    net.kyori.option.OptionState emptyState();
}
