package net.kyori.adventure.translation;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Translator {
    static java.util.Locale parseLocale(java.lang.String arg0) {
        return null;
    }
    net.kyori.adventure.key.Key name();
    default net.kyori.adventure.util.TriState hasAnyTranslations() {
        return null;
    }
    default boolean canTranslate(java.lang.String arg0, java.util.Locale arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.translation.Translator.canTranslate(Ljava/lang/String;Ljava/util/Locale;)Z");
        return false;
    }
    java.text.MessageFormat translate(java.lang.String arg0, java.util.Locale arg1);
    default net.kyori.adventure.text.Component translate(net.kyori.adventure.text.TranslatableComponent arg0, java.util.Locale arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.translation.Translator.translate(Lnet/kyori/adventure/text/TranslatableComponent;Ljava/util/Locale;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
}
