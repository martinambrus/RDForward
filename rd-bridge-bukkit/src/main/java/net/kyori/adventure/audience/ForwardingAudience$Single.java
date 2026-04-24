package net.kyori.adventure.audience;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ForwardingAudience$Single extends net.kyori.adventure.audience.ForwardingAudience {
    net.kyori.adventure.audience.Audience audience();
    default java.lang.Iterable audiences() {
        return java.util.Collections.emptyList();
    }
    default java.util.Optional get(net.kyori.adventure.pointer.Pointer arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.get(Lnet/kyori/adventure/pointer/Pointer;)Ljava/util/Optional;");
        return java.util.Optional.empty();
    }
    default java.lang.Object getOrDefault(net.kyori.adventure.pointer.Pointer arg0, java.lang.Object arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.getOrDefault(Lnet/kyori/adventure/pointer/Pointer;Ljava/lang/Object;)Ljava/lang/Object;");
        return null;
    }
    default java.lang.Object getOrDefaultFrom(net.kyori.adventure.pointer.Pointer arg0, java.util.function.Supplier arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.getOrDefaultFrom(Lnet/kyori/adventure/pointer/Pointer;Ljava/util/function/Supplier;)Ljava/lang/Object;");
        return null;
    }
    default net.kyori.adventure.audience.Audience filterAudience(java.util.function.Predicate arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.filterAudience(Ljava/util/function/Predicate;)Lnet/kyori/adventure/audience/Audience;");
        return null;
    }
    default void forEachAudience(java.util.function.Consumer arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.forEachAudience(Ljava/util/function/Consumer;)V");
    }
    default net.kyori.adventure.pointer.Pointers pointers() {
        return null;
    }
    default void sendMessage(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.sendMessage(Lnet/kyori/adventure/text/Component;)V");
    }
    default void sendMessage(net.kyori.adventure.text.Component arg0, net.kyori.adventure.chat.ChatType$Bound arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.sendMessage(Lnet/kyori/adventure/text/Component;Lnet/kyori/adventure/chat/ChatType$Bound;)V");
    }
    default void sendMessage(net.kyori.adventure.chat.SignedMessage arg0, net.kyori.adventure.chat.ChatType$Bound arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.sendMessage(Lnet/kyori/adventure/chat/SignedMessage;Lnet/kyori/adventure/chat/ChatType$Bound;)V");
    }
    default void deleteMessage(net.kyori.adventure.chat.SignedMessage$Signature arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.deleteMessage(Lnet/kyori/adventure/chat/SignedMessage$Signature;)V");
    }
    default void sendMessage(net.kyori.adventure.identity.Identified arg0, net.kyori.adventure.text.Component arg1, net.kyori.adventure.audience.MessageType arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.sendMessage(Lnet/kyori/adventure/identity/Identified;Lnet/kyori/adventure/text/Component;Lnet/kyori/adventure/audience/MessageType;)V");
    }
    default void sendMessage(net.kyori.adventure.identity.Identity arg0, net.kyori.adventure.text.Component arg1, net.kyori.adventure.audience.MessageType arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.sendMessage(Lnet/kyori/adventure/identity/Identity;Lnet/kyori/adventure/text/Component;Lnet/kyori/adventure/audience/MessageType;)V");
    }
    default void sendActionBar(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.sendActionBar(Lnet/kyori/adventure/text/Component;)V");
    }
    default void sendPlayerListHeader(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.sendPlayerListHeader(Lnet/kyori/adventure/text/Component;)V");
    }
    default void sendPlayerListFooter(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.sendPlayerListFooter(Lnet/kyori/adventure/text/Component;)V");
    }
    default void sendPlayerListHeaderAndFooter(net.kyori.adventure.text.Component arg0, net.kyori.adventure.text.Component arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.sendPlayerListHeaderAndFooter(Lnet/kyori/adventure/text/Component;Lnet/kyori/adventure/text/Component;)V");
    }
    default void sendTitlePart(net.kyori.adventure.title.TitlePart arg0, java.lang.Object arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.sendTitlePart(Lnet/kyori/adventure/title/TitlePart;Ljava/lang/Object;)V");
    }
    default void clearTitle() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.clearTitle()V");
    }
    default void resetTitle() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.resetTitle()V");
    }
    default void showBossBar(net.kyori.adventure.bossbar.BossBar arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.showBossBar(Lnet/kyori/adventure/bossbar/BossBar;)V");
    }
    default void hideBossBar(net.kyori.adventure.bossbar.BossBar arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.hideBossBar(Lnet/kyori/adventure/bossbar/BossBar;)V");
    }
    default void playSound(net.kyori.adventure.sound.Sound arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.playSound(Lnet/kyori/adventure/sound/Sound;)V");
    }
    default void playSound(net.kyori.adventure.sound.Sound arg0, double arg1, double arg2, double arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.playSound(Lnet/kyori/adventure/sound/Sound;DDD)V");
    }
    default void playSound(net.kyori.adventure.sound.Sound arg0, net.kyori.adventure.sound.Sound$Emitter arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.playSound(Lnet/kyori/adventure/sound/Sound;Lnet/kyori/adventure/sound/Sound$Emitter;)V");
    }
    default void stopSound(net.kyori.adventure.sound.SoundStop arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.stopSound(Lnet/kyori/adventure/sound/SoundStop;)V");
    }
    default void openBook(net.kyori.adventure.inventory.Book arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.openBook(Lnet/kyori/adventure/inventory/Book;)V");
    }
    default void sendResourcePacks(net.kyori.adventure.resource.ResourcePackRequest arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.sendResourcePacks(Lnet/kyori/adventure/resource/ResourcePackRequest;)V");
    }
    default void removeResourcePacks(java.lang.Iterable arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.removeResourcePacks(Ljava/lang/Iterable;)V");
    }
    default void removeResourcePacks(java.util.UUID arg0, java.util.UUID[] arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.removeResourcePacks(Ljava/util/UUID;[Ljava/util/UUID;)V");
    }
    default void clearResourcePacks() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.clearResourcePacks()V");
    }
    default void showDialog(net.kyori.adventure.dialog.DialogLike arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.showDialog(Lnet/kyori/adventure/dialog/DialogLike;)V");
    }
    default void closeDialog() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.ForwardingAudience$Single.closeDialog()V");
    }
}
