package net.kyori.adventure.text.renderer;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ComponentRenderer {
    net.kyori.adventure.text.Component render(net.kyori.adventure.text.Component arg0, java.lang.Object arg1);
    default net.kyori.adventure.text.renderer.ComponentRenderer mapContext(java.util.function.Function arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.renderer.ComponentRenderer.mapContext(Ljava/util/function/Function;)Lnet/kyori/adventure/text/renderer/ComponentRenderer;");
        return this;
    }
}
