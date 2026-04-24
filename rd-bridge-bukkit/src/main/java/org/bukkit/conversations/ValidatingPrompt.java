package org.bukkit.conversations;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class ValidatingPrompt implements org.bukkit.conversations.Prompt {
    public ValidatingPrompt() {}
    public org.bukkit.conversations.Prompt acceptInput(org.bukkit.conversations.ConversationContext arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.conversations.ValidatingPrompt.acceptInput(Lorg/bukkit/conversations/ConversationContext;Ljava/lang/String;)Lorg/bukkit/conversations/Prompt;");
        return null;
    }
    public boolean blocksForInput(org.bukkit.conversations.ConversationContext arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.conversations.ValidatingPrompt.blocksForInput(Lorg/bukkit/conversations/ConversationContext;)Z");
        return false;
    }
    protected abstract boolean isInputValid(org.bukkit.conversations.ConversationContext arg0, java.lang.String arg1);
    protected abstract org.bukkit.conversations.Prompt acceptValidatedInput(org.bukkit.conversations.ConversationContext arg0, java.lang.String arg1);
    protected java.lang.String getFailedValidationText(org.bukkit.conversations.ConversationContext arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.conversations.ValidatingPrompt.getFailedValidationText(Lorg/bukkit/conversations/ConversationContext;Ljava/lang/String;)Ljava/lang/String;");
        return null;
    }
}
