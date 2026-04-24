package org.bukkit.conversations;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class MessagePrompt implements org.bukkit.conversations.Prompt {
    public MessagePrompt() {}
    public boolean blocksForInput(org.bukkit.conversations.ConversationContext arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.conversations.MessagePrompt.blocksForInput(Lorg/bukkit/conversations/ConversationContext;)Z");
        return false;
    }
    public org.bukkit.conversations.Prompt acceptInput(org.bukkit.conversations.ConversationContext arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.conversations.MessagePrompt.acceptInput(Lorg/bukkit/conversations/ConversationContext;Ljava/lang/String;)Lorg/bukkit/conversations/Prompt;");
        return null;
    }
    protected abstract org.bukkit.conversations.Prompt getNextPrompt(org.bukkit.conversations.ConversationContext arg0);
}
