package net.kyori.adventure.translation;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface TranslationRegistry extends net.kyori.adventure.translation.Translator, net.kyori.adventure.translation.TranslationStore$StringBased {
    public static final java.util.regex.Pattern SINGLE_QUOTE_PATTERN = null;
    static net.kyori.adventure.translation.TranslationRegistry create(net.kyori.adventure.key.Key arg0) {
        return null;
    }
    boolean contains(java.lang.String arg0);
    java.text.MessageFormat translate(java.lang.String arg0, java.util.Locale arg1);
    void defaultLocale(java.util.Locale arg0);
    void register(java.lang.String arg0, java.util.Locale arg1, java.text.MessageFormat arg2);
    default void registerAll(java.util.Locale arg0, java.util.Map arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.translation.TranslationRegistry.registerAll(Ljava/util/Locale;Ljava/util/Map;)V");
    }
    default void registerAll(java.util.Locale arg0, java.nio.file.Path arg1, boolean arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.translation.TranslationRegistry.registerAll(Ljava/util/Locale;Ljava/nio/file/Path;Z)V");
    }
    default void registerAll(java.util.Locale arg0, java.util.ResourceBundle arg1, boolean arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.translation.TranslationRegistry.registerAll(Ljava/util/Locale;Ljava/util/ResourceBundle;Z)V");
    }
    default void registerAll(java.util.Locale arg0, java.util.Set arg1, java.util.function.Function arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.translation.TranslationRegistry.registerAll(Ljava/util/Locale;Ljava/util/Set;Ljava/util/function/Function;)V");
    }
    void unregister(java.lang.String arg0);
    default void register(java.lang.String arg0, java.util.Locale arg1, java.lang.Object arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.translation.TranslationRegistry.register(Ljava/lang/String;Ljava/util/Locale;Ljava/lang/Object;)V");
    }
}
