package org.bukkit.conversations;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class Conversation {
    public Conversation(org.bukkit.plugin.Plugin arg0, org.bukkit.conversations.Conversable arg1, org.bukkit.conversations.Prompt arg2) {}
    public Conversation(org.bukkit.plugin.Plugin arg0, org.bukkit.conversations.Conversable arg1, org.bukkit.conversations.Prompt arg2, java.util.Map arg3) {}
    public Conversation() {}
    public org.bukkit.conversations.Conversable getForWhom() {
        return null;
    }
    public boolean isModal() {
        return false;
    }
    public boolean isLocalEchoEnabled() {
        return false;
    }
    public void setLocalEchoEnabled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.conversations.Conversation.setLocalEchoEnabled(Z)V");
    }
    public org.bukkit.conversations.ConversationPrefix getPrefix() {
        return null;
    }
    public java.util.List getCancellers() {
        return java.util.Collections.emptyList();
    }
    public org.bukkit.conversations.ConversationContext getContext() {
        return null;
    }
    public void begin() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.conversations.Conversation.begin()V");
    }
    public org.bukkit.conversations.Conversation$ConversationState getState() {
        return null;
    }
    public void acceptInput(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.conversations.Conversation.acceptInput(Ljava/lang/String;)V");
    }
    public void addConversationAbandonedListener(org.bukkit.conversations.ConversationAbandonedListener arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.conversations.Conversation.addConversationAbandonedListener(Lorg/bukkit/conversations/ConversationAbandonedListener;)V");
    }
    public void removeConversationAbandonedListener(org.bukkit.conversations.ConversationAbandonedListener arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.conversations.Conversation.removeConversationAbandonedListener(Lorg/bukkit/conversations/ConversationAbandonedListener;)V");
    }
    public void abandon() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.conversations.Conversation.abandon()V");
    }
    public void abandon(org.bukkit.conversations.ConversationAbandonedEvent arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.conversations.Conversation.abandon(Lorg/bukkit/conversations/ConversationAbandonedEvent;)V");
    }
    public void outputNextPrompt() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.conversations.Conversation.outputNextPrompt()V");
    }
}
