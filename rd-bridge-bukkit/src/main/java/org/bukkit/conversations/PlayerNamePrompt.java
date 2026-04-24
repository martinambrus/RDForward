package org.bukkit.conversations;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class PlayerNamePrompt extends org.bukkit.conversations.ValidatingPrompt {
    public PlayerNamePrompt(org.bukkit.plugin.Plugin arg0) {}
    protected PlayerNamePrompt() {}
    protected boolean isInputValid(org.bukkit.conversations.ConversationContext arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.conversations.PlayerNamePrompt.isInputValid(Lorg/bukkit/conversations/ConversationContext;Ljava/lang/String;)Z");
        return false;
    }
    protected org.bukkit.conversations.Prompt acceptValidatedInput(org.bukkit.conversations.ConversationContext arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.conversations.PlayerNamePrompt.acceptValidatedInput(Lorg/bukkit/conversations/ConversationContext;Ljava/lang/String;)Lorg/bukkit/conversations/Prompt;");
        return null;
    }
    protected abstract org.bukkit.conversations.Prompt acceptValidatedInput(org.bukkit.conversations.ConversationContext arg0, org.bukkit.entity.Player arg1);
}
