package io.papermc.paper.chat;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ChatRenderer {
    net.kyori.adventure.text.Component render(org.bukkit.entity.Player arg0, net.kyori.adventure.text.Component arg1, net.kyori.adventure.text.Component arg2, net.kyori.adventure.audience.Audience arg3);
    static io.papermc.paper.chat.ChatRenderer defaultRenderer() {
        return null;
    }
    static io.papermc.paper.chat.ChatRenderer viewerUnaware(io.papermc.paper.chat.ChatRenderer$ViewerUnaware arg0) {
        return null;
    }
}
