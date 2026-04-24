package org.bukkit.conversations;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ConversationCanceller extends java.lang.Cloneable {
    void setConversation(org.bukkit.conversations.Conversation arg0);
    boolean cancelBasedOnInput(org.bukkit.conversations.ConversationContext arg0, java.lang.String arg1);
    org.bukkit.conversations.ConversationCanceller clone();
}
