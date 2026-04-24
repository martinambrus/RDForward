package org.bukkit.conversations;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class ConversationAbandonedEvent extends java.util.EventObject {
    public ConversationAbandonedEvent(org.bukkit.conversations.Conversation arg0) { super((java.lang.Object) null); }
    public ConversationAbandonedEvent(org.bukkit.conversations.Conversation arg0, org.bukkit.conversations.ConversationCanceller arg1) { super((java.lang.Object) null); }
    public ConversationAbandonedEvent() { super((java.lang.Object) null); }
    public org.bukkit.conversations.ConversationCanceller getCanceller() {
        return null;
    }
    public org.bukkit.conversations.ConversationContext getContext() {
        return null;
    }
    public boolean gracefulExit() {
        return false;
    }
}
