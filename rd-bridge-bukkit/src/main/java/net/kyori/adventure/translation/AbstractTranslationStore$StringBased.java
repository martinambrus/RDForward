package net.kyori.adventure.translation;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class AbstractTranslationStore$StringBased extends net.kyori.adventure.translation.AbstractTranslationStore implements net.kyori.adventure.translation.TranslationStore$StringBased {
    protected AbstractTranslationStore$StringBased(net.kyori.adventure.key.Key arg0) { super((net.kyori.adventure.key.Key) null); }
    protected AbstractTranslationStore$StringBased() { super((net.kyori.adventure.key.Key) null); }
    protected abstract java.lang.Object parse(java.lang.String arg0, java.util.Locale arg1);
    public final void registerAll(java.util.Locale arg0, java.nio.file.Path arg1, boolean arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.translation.AbstractTranslationStore$StringBased.registerAll(Ljava/util/Locale;Ljava/nio/file/Path;Z)V");
    }
    public final void registerAll(java.util.Locale arg0, java.util.ResourceBundle arg1, boolean arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.translation.AbstractTranslationStore$StringBased.registerAll(Ljava/util/Locale;Ljava/util/ResourceBundle;Z)V");
    }
}
