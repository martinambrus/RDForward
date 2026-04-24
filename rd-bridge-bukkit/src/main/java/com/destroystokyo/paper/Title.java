package com.destroystokyo.paper;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class Title {
    public static final int DEFAULT_FADE_IN = 20;
    public static final int DEFAULT_STAY = 200;
    public static final int DEFAULT_FADE_OUT = 20;
    public Title(net.md_5.bungee.api.chat.BaseComponent arg0) {}
    public Title(net.md_5.bungee.api.chat.BaseComponent[] arg0) {}
    public Title(java.lang.String arg0) {}
    public Title(net.md_5.bungee.api.chat.BaseComponent arg0, net.md_5.bungee.api.chat.BaseComponent arg1) {}
    public Title(net.md_5.bungee.api.chat.BaseComponent[] arg0, net.md_5.bungee.api.chat.BaseComponent[] arg1) {}
    public Title(java.lang.String arg0, java.lang.String arg1) {}
    public Title(net.md_5.bungee.api.chat.BaseComponent arg0, net.md_5.bungee.api.chat.BaseComponent arg1, int arg2, int arg3, int arg4) {}
    public Title(net.md_5.bungee.api.chat.BaseComponent[] arg0, net.md_5.bungee.api.chat.BaseComponent[] arg1, int arg2, int arg3, int arg4) {}
    public Title(java.lang.String arg0, java.lang.String arg1, int arg2, int arg3, int arg4) {}
    public Title() {}
    public net.md_5.bungee.api.chat.BaseComponent[] getTitle() {
        return new net.md_5.bungee.api.chat.BaseComponent[0];
    }
    public net.md_5.bungee.api.chat.BaseComponent[] getSubtitle() {
        return new net.md_5.bungee.api.chat.BaseComponent[0];
    }
    public int getFadeIn() {
        return 0;
    }
    public int getStay() {
        return 0;
    }
    public int getFadeOut() {
        return 0;
    }
    public void send(org.bukkit.entity.Player arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.Title.send(Lorg/bukkit/entity/Player;)V");
    }
    public void send(java.util.Collection arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.Title.send(Ljava/util/Collection;)V");
    }
    public void send(org.bukkit.entity.Player[] arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.Title.send([Lorg/bukkit/entity/Player;)V");
    }
    public void broadcast() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.Title.broadcast()V");
    }
    public static com.destroystokyo.paper.Title$Builder builder() {
        return null;
    }
}
