package net.kyori.adventure.translation;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface TranslationStore extends net.kyori.adventure.translation.Translator {
    static net.kyori.adventure.translation.TranslationStore component(net.kyori.adventure.key.Key arg0) {
        return null;
    }
    static net.kyori.adventure.translation.TranslationStore$StringBased messageFormat(net.kyori.adventure.key.Key arg0) {
        return null;
    }
    boolean contains(java.lang.String arg0);
    boolean contains(java.lang.String arg0, java.util.Locale arg1);
    default boolean canTranslate(java.lang.String arg0, java.util.Locale arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.translation.TranslationStore.canTranslate(Ljava/lang/String;Ljava/util/Locale;)Z");
        return false;
    }
    void defaultLocale(java.util.Locale arg0);
    void register(java.lang.String arg0, java.util.Locale arg1, java.lang.Object arg2);
    void registerAll(java.util.Locale arg0, java.util.Map arg1);
    void registerAll(java.util.Locale arg0, java.util.Set arg1, java.util.function.Function arg2);
    void unregister(java.lang.String arg0);
}
