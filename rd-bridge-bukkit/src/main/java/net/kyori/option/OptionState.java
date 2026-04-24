package net.kyori.option;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface OptionState {
    static net.kyori.option.OptionState emptyOptionState() {
        return null;
    }
    static net.kyori.option.OptionState$Builder optionState() {
        return null;
    }
    static net.kyori.option.OptionState$VersionedBuilder versionedOptionState() {
        return null;
    }
    net.kyori.option.OptionSchema schema();
    boolean has(net.kyori.option.Option arg0);
    java.lang.Object value(net.kyori.option.Option arg0);
}
