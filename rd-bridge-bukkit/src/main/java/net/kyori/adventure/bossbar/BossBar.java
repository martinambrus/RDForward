package net.kyori.adventure.bossbar;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface BossBar extends net.kyori.examination.Examinable {
    public static final float MIN_PROGRESS = 0.0f;
    public static final float MAX_PROGRESS = 1.0f;
    public static final float MIN_PERCENT = 0.0f;
    public static final float MAX_PERCENT = 1.0f;
    static net.kyori.adventure.bossbar.BossBar bossBar(net.kyori.adventure.text.ComponentLike arg0, float arg1, net.kyori.adventure.bossbar.BossBar$Color arg2, net.kyori.adventure.bossbar.BossBar$Overlay arg3) {
        return null;
    }
    static net.kyori.adventure.bossbar.BossBar bossBar(net.kyori.adventure.text.Component arg0, float arg1, net.kyori.adventure.bossbar.BossBar$Color arg2, net.kyori.adventure.bossbar.BossBar$Overlay arg3) {
        return null;
    }
    static net.kyori.adventure.bossbar.BossBar bossBar(net.kyori.adventure.text.ComponentLike arg0, float arg1, net.kyori.adventure.bossbar.BossBar$Color arg2, net.kyori.adventure.bossbar.BossBar$Overlay arg3, java.util.Set arg4) {
        return null;
    }
    static net.kyori.adventure.bossbar.BossBar bossBar(net.kyori.adventure.text.Component arg0, float arg1, net.kyori.adventure.bossbar.BossBar$Color arg2, net.kyori.adventure.bossbar.BossBar$Overlay arg3, java.util.Set arg4) {
        return null;
    }
    net.kyori.adventure.text.Component name();
    default net.kyori.adventure.bossbar.BossBar name(net.kyori.adventure.text.ComponentLike arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.bossbar.BossBar.name(Lnet/kyori/adventure/text/ComponentLike;)Lnet/kyori/adventure/bossbar/BossBar;");
        return this;
    }
    net.kyori.adventure.bossbar.BossBar name(net.kyori.adventure.text.Component arg0);
    float progress();
    net.kyori.adventure.bossbar.BossBar progress(float arg0);
    default float percent() {
        return 0.0f;
    }
    default net.kyori.adventure.bossbar.BossBar percent(float arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.bossbar.BossBar.percent(F)Lnet/kyori/adventure/bossbar/BossBar;");
        return this;
    }
    net.kyori.adventure.bossbar.BossBar$Color color();
    net.kyori.adventure.bossbar.BossBar color(net.kyori.adventure.bossbar.BossBar$Color arg0);
    net.kyori.adventure.bossbar.BossBar$Overlay overlay();
    net.kyori.adventure.bossbar.BossBar overlay(net.kyori.adventure.bossbar.BossBar$Overlay arg0);
    java.util.Set flags();
    net.kyori.adventure.bossbar.BossBar flags(java.util.Set arg0);
    boolean hasFlag(net.kyori.adventure.bossbar.BossBar$Flag arg0);
    net.kyori.adventure.bossbar.BossBar addFlag(net.kyori.adventure.bossbar.BossBar$Flag arg0);
    net.kyori.adventure.bossbar.BossBar removeFlag(net.kyori.adventure.bossbar.BossBar$Flag arg0);
    net.kyori.adventure.bossbar.BossBar addFlags(net.kyori.adventure.bossbar.BossBar$Flag[] arg0);
    net.kyori.adventure.bossbar.BossBar removeFlags(net.kyori.adventure.bossbar.BossBar$Flag[] arg0);
    net.kyori.adventure.bossbar.BossBar addFlags(java.lang.Iterable arg0);
    net.kyori.adventure.bossbar.BossBar removeFlags(java.lang.Iterable arg0);
    net.kyori.adventure.bossbar.BossBar addListener(net.kyori.adventure.bossbar.BossBar$Listener arg0);
    net.kyori.adventure.bossbar.BossBar removeListener(net.kyori.adventure.bossbar.BossBar$Listener arg0);
    java.lang.Iterable viewers();
    default net.kyori.adventure.bossbar.BossBar addViewer(net.kyori.adventure.audience.Audience arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.bossbar.BossBar.addViewer(Lnet/kyori/adventure/audience/Audience;)Lnet/kyori/adventure/bossbar/BossBar;");
        return this;
    }
    default net.kyori.adventure.bossbar.BossBar removeViewer(net.kyori.adventure.audience.Audience arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.bossbar.BossBar.removeViewer(Lnet/kyori/adventure/audience/Audience;)Lnet/kyori/adventure/bossbar/BossBar;");
        return this;
    }
}
