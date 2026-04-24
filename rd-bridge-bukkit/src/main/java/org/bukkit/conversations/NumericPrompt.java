package org.bukkit.conversations;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class NumericPrompt extends org.bukkit.conversations.ValidatingPrompt {
    public NumericPrompt() {}
    protected boolean isInputValid(org.bukkit.conversations.ConversationContext arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.conversations.NumericPrompt.isInputValid(Lorg/bukkit/conversations/ConversationContext;Ljava/lang/String;)Z");
        return false;
    }
    protected boolean isNumberValid(org.bukkit.conversations.ConversationContext arg0, java.lang.Number arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.conversations.NumericPrompt.isNumberValid(Lorg/bukkit/conversations/ConversationContext;Ljava/lang/Number;)Z");
        return false;
    }
    protected org.bukkit.conversations.Prompt acceptValidatedInput(org.bukkit.conversations.ConversationContext arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.conversations.NumericPrompt.acceptValidatedInput(Lorg/bukkit/conversations/ConversationContext;Ljava/lang/String;)Lorg/bukkit/conversations/Prompt;");
        return null;
    }
    protected abstract org.bukkit.conversations.Prompt acceptValidatedInput(org.bukkit.conversations.ConversationContext arg0, java.lang.Number arg1);
    protected java.lang.String getFailedValidationText(org.bukkit.conversations.ConversationContext arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.conversations.NumericPrompt.getFailedValidationText(Lorg/bukkit/conversations/ConversationContext;Ljava/lang/String;)Ljava/lang/String;");
        return null;
    }
    protected java.lang.String getInputNotNumericText(org.bukkit.conversations.ConversationContext arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.conversations.NumericPrompt.getInputNotNumericText(Lorg/bukkit/conversations/ConversationContext;Ljava/lang/String;)Ljava/lang/String;");
        return null;
    }
    protected java.lang.String getFailedValidationText(org.bukkit.conversations.ConversationContext arg0, java.lang.Number arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.conversations.NumericPrompt.getFailedValidationText(Lorg/bukkit/conversations/ConversationContext;Ljava/lang/Number;)Ljava/lang/String;");
        return null;
    }
}
