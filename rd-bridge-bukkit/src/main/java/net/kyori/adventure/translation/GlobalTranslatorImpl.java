package net.kyori.adventure.translation;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
class GlobalTranslatorImpl implements net.kyori.adventure.translation.GlobalTranslator {
    public GlobalTranslatorImpl() {}
    public net.kyori.adventure.key.Key name() {
        return null;
    }
    public java.lang.Iterable sources() {
        return java.util.Collections.emptyList();
    }
    public boolean addSource(net.kyori.adventure.translation.Translator arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.translation.GlobalTranslatorImpl.addSource(Lnet/kyori/adventure/translation/Translator;)Z");
        return false;
    }
    public boolean removeSource(net.kyori.adventure.translation.Translator arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.translation.GlobalTranslatorImpl.removeSource(Lnet/kyori/adventure/translation/Translator;)Z");
        return false;
    }
    public net.kyori.adventure.util.TriState hasAnyTranslations() {
        return null;
    }
    public boolean canTranslate(java.lang.String arg0, java.util.Locale arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.translation.GlobalTranslatorImpl.canTranslate(Ljava/lang/String;Ljava/util/Locale;)Z");
        return false;
    }
    public java.text.MessageFormat translate(java.lang.String arg0, java.util.Locale arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.translation.GlobalTranslatorImpl.translate(Ljava/lang/String;Ljava/util/Locale;)Ljava/text/MessageFormat;");
        return null;
    }
    public net.kyori.adventure.text.Component translate(net.kyori.adventure.text.TranslatableComponent arg0, java.util.Locale arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.translation.GlobalTranslatorImpl.translate(Lnet/kyori/adventure/text/TranslatableComponent;Ljava/util/Locale;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    public java.util.stream.Stream examinableProperties() {
        return null;
    }
}
