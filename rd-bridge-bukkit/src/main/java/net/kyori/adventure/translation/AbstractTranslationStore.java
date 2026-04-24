package net.kyori.adventure.translation;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class AbstractTranslationStore implements net.kyori.examination.Examinable, net.kyori.adventure.translation.TranslationStore {
    protected AbstractTranslationStore(net.kyori.adventure.key.Key arg0) {}
    protected AbstractTranslationStore() {}
    protected java.lang.Object translationValue(java.lang.String arg0, java.util.Locale arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.translation.AbstractTranslationStore.translationValue(Ljava/lang/String;Ljava/util/Locale;)Ljava/lang/Object;");
        return null;
    }
    public final boolean contains(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.translation.AbstractTranslationStore.contains(Ljava/lang/String;)Z");
        return false;
    }
    public final boolean contains(java.lang.String arg0, java.util.Locale arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.translation.AbstractTranslationStore.contains(Ljava/lang/String;Ljava/util/Locale;)Z");
        return false;
    }
    public final boolean canTranslate(java.lang.String arg0, java.util.Locale arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.translation.AbstractTranslationStore.canTranslate(Ljava/lang/String;Ljava/util/Locale;)Z");
        return false;
    }
    public final void defaultLocale(java.util.Locale arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.translation.AbstractTranslationStore.defaultLocale(Ljava/util/Locale;)V");
    }
    public final void register(java.lang.String arg0, java.util.Locale arg1, java.lang.Object arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.translation.AbstractTranslationStore.register(Ljava/lang/String;Ljava/util/Locale;Ljava/lang/Object;)V");
    }
    public final void registerAll(java.util.Locale arg0, java.util.Map arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.translation.AbstractTranslationStore.registerAll(Ljava/util/Locale;Ljava/util/Map;)V");
    }
    public final void registerAll(java.util.Locale arg0, java.util.Set arg1, java.util.function.Function arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.translation.AbstractTranslationStore.registerAll(Ljava/util/Locale;Ljava/util/Set;Ljava/util/function/Function;)V");
    }
    public final void unregister(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.translation.AbstractTranslationStore.unregister(Ljava/lang/String;)V");
    }
    public final net.kyori.adventure.key.Key name() {
        return null;
    }
    public final net.kyori.adventure.util.TriState hasAnyTranslations() {
        return null;
    }
    public final java.util.stream.Stream examinableProperties() {
        return null;
    }
    public final boolean equals(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.translation.AbstractTranslationStore.equals(Ljava/lang/Object;)Z");
        return false;
    }
    public final int hashCode() {
        return 0;
    }
    public final java.lang.String toString() {
        return null;
    }
}
