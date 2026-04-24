package net.kyori.adventure.chat;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ChatType extends net.kyori.examination.Examinable, net.kyori.adventure.key.Keyed {
    public static final net.kyori.adventure.chat.ChatType CHAT = null;
    public static final net.kyori.adventure.chat.ChatType SAY_COMMAND = null;
    public static final net.kyori.adventure.chat.ChatType MSG_COMMAND_INCOMING = null;
    public static final net.kyori.adventure.chat.ChatType MSG_COMMAND_OUTGOING = null;
    public static final net.kyori.adventure.chat.ChatType TEAM_MSG_COMMAND_INCOMING = null;
    public static final net.kyori.adventure.chat.ChatType TEAM_MSG_COMMAND_OUTGOING = null;
    public static final net.kyori.adventure.chat.ChatType EMOTE_COMMAND = null;
    static net.kyori.adventure.chat.ChatType chatType(net.kyori.adventure.key.Keyed arg0) {
        return null;
    }
    default net.kyori.adventure.chat.ChatType$Bound bind(net.kyori.adventure.text.ComponentLike arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.chat.ChatType.bind(Lnet/kyori/adventure/text/ComponentLike;)Lnet/kyori/adventure/chat/ChatType$Bound;");
        return null;
    }
    default net.kyori.adventure.chat.ChatType$Bound bind(net.kyori.adventure.text.ComponentLike arg0, net.kyori.adventure.text.ComponentLike arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.chat.ChatType.bind(Lnet/kyori/adventure/text/ComponentLike;Lnet/kyori/adventure/text/ComponentLike;)Lnet/kyori/adventure/chat/ChatType$Bound;");
        return null;
    }
    default java.util.stream.Stream examinableProperties() {
        return null;
    }
    net.kyori.adventure.key.Key key();
}
