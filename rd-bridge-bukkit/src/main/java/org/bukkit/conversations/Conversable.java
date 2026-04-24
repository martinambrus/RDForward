package org.bukkit.conversations;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Conversable {
    boolean isConversing();
    void acceptConversationInput(java.lang.String arg0);
    boolean beginConversation(org.bukkit.conversations.Conversation arg0);
    void abandonConversation(org.bukkit.conversations.Conversation arg0);
    void abandonConversation(org.bukkit.conversations.Conversation arg0, org.bukkit.conversations.ConversationAbandonedEvent arg1);
    void sendRawMessage(java.lang.String arg0);
    void sendRawMessage(java.util.UUID arg0, java.lang.String arg1);
}
