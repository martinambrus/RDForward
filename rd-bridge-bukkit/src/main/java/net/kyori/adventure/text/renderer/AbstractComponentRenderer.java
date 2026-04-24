package net.kyori.adventure.text.renderer;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class AbstractComponentRenderer implements net.kyori.adventure.text.renderer.ComponentRenderer {
    public AbstractComponentRenderer() {}
    public net.kyori.adventure.text.Component render(net.kyori.adventure.text.Component arg0, java.lang.Object arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.renderer.AbstractComponentRenderer.render(Lnet/kyori/adventure/text/Component;Ljava/lang/Object;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    protected abstract net.kyori.adventure.text.Component renderBlockNbt(net.kyori.adventure.text.BlockNBTComponent arg0, java.lang.Object arg1);
    protected abstract net.kyori.adventure.text.Component renderEntityNbt(net.kyori.adventure.text.EntityNBTComponent arg0, java.lang.Object arg1);
    protected abstract net.kyori.adventure.text.Component renderStorageNbt(net.kyori.adventure.text.StorageNBTComponent arg0, java.lang.Object arg1);
    protected abstract net.kyori.adventure.text.Component renderKeybind(net.kyori.adventure.text.KeybindComponent arg0, java.lang.Object arg1);
    protected abstract net.kyori.adventure.text.Component renderScore(net.kyori.adventure.text.ScoreComponent arg0, java.lang.Object arg1);
    protected abstract net.kyori.adventure.text.Component renderSelector(net.kyori.adventure.text.SelectorComponent arg0, java.lang.Object arg1);
    protected abstract net.kyori.adventure.text.Component renderText(net.kyori.adventure.text.TextComponent arg0, java.lang.Object arg1);
    protected net.kyori.adventure.text.Component renderVirtual(net.kyori.adventure.text.VirtualComponent arg0, java.lang.Object arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.renderer.AbstractComponentRenderer.renderVirtual(Lnet/kyori/adventure/text/VirtualComponent;Ljava/lang/Object;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    protected abstract net.kyori.adventure.text.Component renderTranslatable(net.kyori.adventure.text.TranslatableComponent arg0, java.lang.Object arg1);
}
