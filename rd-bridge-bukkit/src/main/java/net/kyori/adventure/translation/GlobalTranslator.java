package net.kyori.adventure.translation;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface GlobalTranslator extends net.kyori.adventure.translation.Translator, net.kyori.examination.Examinable {
    static net.kyori.adventure.translation.GlobalTranslator translator() {
        return null;
    }
    static net.kyori.adventure.translation.GlobalTranslator get() {
        return null;
    }
    static net.kyori.adventure.text.renderer.TranslatableComponentRenderer renderer() {
        return null;
    }
    static net.kyori.adventure.text.Component render(net.kyori.adventure.text.Component arg0, java.util.Locale arg1) {
        return null;
    }
    java.lang.Iterable sources();
    boolean addSource(net.kyori.adventure.translation.Translator arg0);
    boolean removeSource(net.kyori.adventure.translation.Translator arg0);
}
