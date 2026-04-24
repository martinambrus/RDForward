package net.kyori.adventure.audience;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Audience extends net.kyori.adventure.pointer.Pointered {
    static net.kyori.adventure.audience.Audience empty() {
        return null;
    }
    static net.kyori.adventure.audience.Audience audience(net.kyori.adventure.audience.Audience[] arg0) {
        return null;
    }
    static net.kyori.adventure.audience.ForwardingAudience audience(java.lang.Iterable arg0) {
        return null;
    }
    static java.util.stream.Collector toAudience() {
        return null;
    }
    default net.kyori.adventure.audience.Audience filterAudience(java.util.function.Predicate arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.filterAudience(Ljava/util/function/Predicate;)Lnet/kyori/adventure/audience/Audience;");
        return this;
    }
    default void forEachAudience(java.util.function.Consumer arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.forEachAudience(Ljava/util/function/Consumer;)V");
    }
    default void sendMessage(net.kyori.adventure.text.ComponentLike arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendMessage(Lnet/kyori/adventure/text/ComponentLike;)V");
    }
    default void sendMessage(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendMessage(Lnet/kyori/adventure/text/Component;)V");
    }
    default void sendMessage(net.kyori.adventure.text.ComponentLike arg0, net.kyori.adventure.audience.MessageType arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendMessage(Lnet/kyori/adventure/text/ComponentLike;Lnet/kyori/adventure/audience/MessageType;)V");
    }
    default void sendMessage(net.kyori.adventure.text.Component arg0, net.kyori.adventure.audience.MessageType arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendMessage(Lnet/kyori/adventure/text/Component;Lnet/kyori/adventure/audience/MessageType;)V");
    }
    default void sendMessage(net.kyori.adventure.identity.Identified arg0, net.kyori.adventure.text.ComponentLike arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendMessage(Lnet/kyori/adventure/identity/Identified;Lnet/kyori/adventure/text/ComponentLike;)V");
    }
    default void sendMessage(net.kyori.adventure.identity.Identity arg0, net.kyori.adventure.text.ComponentLike arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendMessage(Lnet/kyori/adventure/identity/Identity;Lnet/kyori/adventure/text/ComponentLike;)V");
    }
    default void sendMessage(net.kyori.adventure.identity.Identified arg0, net.kyori.adventure.text.Component arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendMessage(Lnet/kyori/adventure/identity/Identified;Lnet/kyori/adventure/text/Component;)V");
    }
    default void sendMessage(net.kyori.adventure.identity.Identity arg0, net.kyori.adventure.text.Component arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendMessage(Lnet/kyori/adventure/identity/Identity;Lnet/kyori/adventure/text/Component;)V");
    }
    default void sendMessage(net.kyori.adventure.identity.Identified arg0, net.kyori.adventure.text.ComponentLike arg1, net.kyori.adventure.audience.MessageType arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendMessage(Lnet/kyori/adventure/identity/Identified;Lnet/kyori/adventure/text/ComponentLike;Lnet/kyori/adventure/audience/MessageType;)V");
    }
    default void sendMessage(net.kyori.adventure.identity.Identity arg0, net.kyori.adventure.text.ComponentLike arg1, net.kyori.adventure.audience.MessageType arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendMessage(Lnet/kyori/adventure/identity/Identity;Lnet/kyori/adventure/text/ComponentLike;Lnet/kyori/adventure/audience/MessageType;)V");
    }
    default void sendMessage(net.kyori.adventure.identity.Identified arg0, net.kyori.adventure.text.Component arg1, net.kyori.adventure.audience.MessageType arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendMessage(Lnet/kyori/adventure/identity/Identified;Lnet/kyori/adventure/text/Component;Lnet/kyori/adventure/audience/MessageType;)V");
    }
    default void sendMessage(net.kyori.adventure.identity.Identity arg0, net.kyori.adventure.text.Component arg1, net.kyori.adventure.audience.MessageType arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendMessage(Lnet/kyori/adventure/identity/Identity;Lnet/kyori/adventure/text/Component;Lnet/kyori/adventure/audience/MessageType;)V");
    }
    default void sendMessage(net.kyori.adventure.text.Component arg0, net.kyori.adventure.chat.ChatType$Bound arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendMessage(Lnet/kyori/adventure/text/Component;Lnet/kyori/adventure/chat/ChatType$Bound;)V");
    }
    default void sendMessage(net.kyori.adventure.text.ComponentLike arg0, net.kyori.adventure.chat.ChatType$Bound arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendMessage(Lnet/kyori/adventure/text/ComponentLike;Lnet/kyori/adventure/chat/ChatType$Bound;)V");
    }
    default void sendMessage(net.kyori.adventure.chat.SignedMessage arg0, net.kyori.adventure.chat.ChatType$Bound arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendMessage(Lnet/kyori/adventure/chat/SignedMessage;Lnet/kyori/adventure/chat/ChatType$Bound;)V");
    }
    default void deleteMessage(net.kyori.adventure.chat.SignedMessage arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.deleteMessage(Lnet/kyori/adventure/chat/SignedMessage;)V");
    }
    default void deleteMessage(net.kyori.adventure.chat.SignedMessage$Signature arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.deleteMessage(Lnet/kyori/adventure/chat/SignedMessage$Signature;)V");
    }
    default void sendActionBar(net.kyori.adventure.text.ComponentLike arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendActionBar(Lnet/kyori/adventure/text/ComponentLike;)V");
    }
    default void sendActionBar(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendActionBar(Lnet/kyori/adventure/text/Component;)V");
    }
    default void sendPlayerListHeader(net.kyori.adventure.text.ComponentLike arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendPlayerListHeader(Lnet/kyori/adventure/text/ComponentLike;)V");
    }
    default void sendPlayerListHeader(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendPlayerListHeader(Lnet/kyori/adventure/text/Component;)V");
    }
    default void sendPlayerListFooter(net.kyori.adventure.text.ComponentLike arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendPlayerListFooter(Lnet/kyori/adventure/text/ComponentLike;)V");
    }
    default void sendPlayerListFooter(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendPlayerListFooter(Lnet/kyori/adventure/text/Component;)V");
    }
    default void sendPlayerListHeaderAndFooter(net.kyori.adventure.text.ComponentLike arg0, net.kyori.adventure.text.ComponentLike arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendPlayerListHeaderAndFooter(Lnet/kyori/adventure/text/ComponentLike;Lnet/kyori/adventure/text/ComponentLike;)V");
    }
    default void sendPlayerListHeaderAndFooter(net.kyori.adventure.text.Component arg0, net.kyori.adventure.text.Component arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendPlayerListHeaderAndFooter(Lnet/kyori/adventure/text/Component;Lnet/kyori/adventure/text/Component;)V");
    }
    default void showTitle(net.kyori.adventure.title.Title arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.showTitle(Lnet/kyori/adventure/title/Title;)V");
    }
    default void sendTitlePart(net.kyori.adventure.title.TitlePart arg0, java.lang.Object arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendTitlePart(Lnet/kyori/adventure/title/TitlePart;Ljava/lang/Object;)V");
    }
    default void clearTitle() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.clearTitle()V");
    }
    default void resetTitle() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.resetTitle()V");
    }
    default void showBossBar(net.kyori.adventure.bossbar.BossBar arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.showBossBar(Lnet/kyori/adventure/bossbar/BossBar;)V");
    }
    default void hideBossBar(net.kyori.adventure.bossbar.BossBar arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.hideBossBar(Lnet/kyori/adventure/bossbar/BossBar;)V");
    }
    default void playSound(net.kyori.adventure.sound.Sound arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.playSound(Lnet/kyori/adventure/sound/Sound;)V");
    }
    default void playSound(net.kyori.adventure.sound.Sound arg0, double arg1, double arg2, double arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.playSound(Lnet/kyori/adventure/sound/Sound;DDD)V");
    }
    default void playSound(net.kyori.adventure.sound.Sound arg0, net.kyori.adventure.sound.Sound$Emitter arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.playSound(Lnet/kyori/adventure/sound/Sound;Lnet/kyori/adventure/sound/Sound$Emitter;)V");
    }
    default void stopSound(net.kyori.adventure.sound.Sound arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.stopSound(Lnet/kyori/adventure/sound/Sound;)V");
    }
    default void stopSound(net.kyori.adventure.sound.SoundStop arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.stopSound(Lnet/kyori/adventure/sound/SoundStop;)V");
    }
    default void openBook(net.kyori.adventure.inventory.Book$Builder arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.openBook(Lnet/kyori/adventure/inventory/Book$Builder;)V");
    }
    default void openBook(net.kyori.adventure.inventory.Book arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.openBook(Lnet/kyori/adventure/inventory/Book;)V");
    }
    default void sendResourcePacks(net.kyori.adventure.resource.ResourcePackInfoLike arg0, net.kyori.adventure.resource.ResourcePackInfoLike[] arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendResourcePacks(Lnet/kyori/adventure/resource/ResourcePackInfoLike;[Lnet/kyori/adventure/resource/ResourcePackInfoLike;)V");
    }
    default void sendResourcePacks(net.kyori.adventure.resource.ResourcePackRequestLike arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendResourcePacks(Lnet/kyori/adventure/resource/ResourcePackRequestLike;)V");
    }
    default void sendResourcePacks(net.kyori.adventure.resource.ResourcePackRequest arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.sendResourcePacks(Lnet/kyori/adventure/resource/ResourcePackRequest;)V");
    }
    default void removeResourcePacks(net.kyori.adventure.resource.ResourcePackRequestLike arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.removeResourcePacks(Lnet/kyori/adventure/resource/ResourcePackRequestLike;)V");
    }
    default void removeResourcePacks(net.kyori.adventure.resource.ResourcePackRequest arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.removeResourcePacks(Lnet/kyori/adventure/resource/ResourcePackRequest;)V");
    }
    default void removeResourcePacks(net.kyori.adventure.resource.ResourcePackInfoLike arg0, net.kyori.adventure.resource.ResourcePackInfoLike[] arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.removeResourcePacks(Lnet/kyori/adventure/resource/ResourcePackInfoLike;[Lnet/kyori/adventure/resource/ResourcePackInfoLike;)V");
    }
    default void removeResourcePacks(java.lang.Iterable arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.removeResourcePacks(Ljava/lang/Iterable;)V");
    }
    default void removeResourcePacks(java.util.UUID arg0, java.util.UUID[] arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.removeResourcePacks(Ljava/util/UUID;[Ljava/util/UUID;)V");
    }
    default void clearResourcePacks() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.clearResourcePacks()V");
    }
    default void showDialog(net.kyori.adventure.dialog.DialogLike arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.showDialog(Lnet/kyori/adventure/dialog/DialogLike;)V");
    }
    default void closeDialog() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.Audience.closeDialog()V");
    }
}
