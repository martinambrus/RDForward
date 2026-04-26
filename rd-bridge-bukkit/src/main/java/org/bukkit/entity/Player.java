package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Player extends org.bukkit.entity.HumanEntity, org.bukkit.conversations.Conversable, org.bukkit.OfflinePlayer, org.bukkit.plugin.messaging.PluginMessageRecipient, net.kyori.adventure.identity.Identified, net.kyori.adventure.bossbar.BossBarViewer, com.destroystokyo.paper.network.NetworkClient {
    default net.kyori.adventure.identity.Identity identity() {
        return null;
    }
    java.lang.Iterable activeBossBars();
    net.kyori.adventure.text.Component displayName();
    void displayName(net.kyori.adventure.text.Component arg0);
    java.lang.String getName();
    java.lang.String getDisplayName();
    void setDisplayName(java.lang.String arg0);
    void playerListName(net.kyori.adventure.text.Component arg0);
    net.kyori.adventure.text.Component playerListName();
    net.kyori.adventure.text.Component playerListHeader();
    net.kyori.adventure.text.Component playerListFooter();
    java.lang.String getPlayerListName();
    void setPlayerListName(java.lang.String arg0);
    int getPlayerListOrder();
    void setPlayerListOrder(int arg0);
    java.lang.String getPlayerListHeader();
    java.lang.String getPlayerListFooter();
    void setPlayerListHeader(java.lang.String arg0);
    void setPlayerListFooter(java.lang.String arg0);
    void setPlayerListHeaderFooter(java.lang.String arg0, java.lang.String arg1);
    void setCompassTarget(org.bukkit.Location arg0);
    org.bukkit.Location getCompassTarget();
    java.net.InetSocketAddress getAddress();
    java.net.InetSocketAddress getHAProxyAddress();
    boolean isTransferred();
    java.util.concurrent.CompletableFuture retrieveCookie(org.bukkit.NamespacedKey arg0);
    void storeCookie(org.bukkit.NamespacedKey arg0, byte[] arg1);
    void transfer(java.lang.String arg0, int arg1);
    void sendRawMessage(java.lang.String arg0);
    void kickPlayer(java.lang.String arg0);
    default void kick() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.kick()V");
    }
    default void kick(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.kick(Lnet/kyori/adventure/text/Component;)V");
    }
    void kick(net.kyori.adventure.text.Component arg0, org.bukkit.event.player.PlayerKickEvent$Cause arg1);
    org.bukkit.BanEntry ban(java.lang.String arg0, java.util.Date arg1, java.lang.String arg2, boolean arg3);
    org.bukkit.BanEntry ban(java.lang.String arg0, java.time.Instant arg1, java.lang.String arg2, boolean arg3);
    org.bukkit.BanEntry ban(java.lang.String arg0, java.time.Duration arg1, java.lang.String arg2, boolean arg3);
    org.bukkit.BanEntry banIp(java.lang.String arg0, java.util.Date arg1, java.lang.String arg2, boolean arg3);
    org.bukkit.BanEntry banIp(java.lang.String arg0, java.time.Instant arg1, java.lang.String arg2, boolean arg3);
    org.bukkit.BanEntry banIp(java.lang.String arg0, java.time.Duration arg1, java.lang.String arg2, boolean arg3);
    void chat(java.lang.String arg0);
    boolean performCommand(java.lang.String arg0) throws org.bukkit.command.CommandException;
    boolean isOnGround();
    boolean isSneaking();
    void setSneaking(boolean arg0);
    boolean isSprinting();
    void setSprinting(boolean arg0);
    void saveData();
    void loadData();
    void setSleepingIgnored(boolean arg0);
    boolean isSleepingIgnored();
    default org.bukkit.Location getRespawnLocation() {
        return null;
    }
    default void setBedSpawnLocation(org.bukkit.Location arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.setBedSpawnLocation(Lorg/bukkit/Location;)V");
    }
    default void setRespawnLocation(org.bukkit.Location arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.setRespawnLocation(Lorg/bukkit/Location;)V");
    }
    default void setBedSpawnLocation(org.bukkit.Location arg0, boolean arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.setBedSpawnLocation(Lorg/bukkit/Location;Z)V");
    }
    void setRespawnLocation(org.bukkit.Location arg0, boolean arg1);
    java.util.Collection getEnderPearls();
    org.bukkit.Input getCurrentInput();
    default void playNote(org.bukkit.Location arg0, byte arg1, byte arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.playNote(Lorg/bukkit/Location;BB)V");
    }
    void playNote(org.bukkit.Location arg0, org.bukkit.Instrument arg1, org.bukkit.Note arg2);
    default void playSound(org.bukkit.Location arg0, org.bukkit.Sound arg1, float arg2, float arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.playSound(Lorg/bukkit/Location;Lorg/bukkit/Sound;FF)V");
    }
    default void playSound(org.bukkit.Location arg0, java.lang.String arg1, float arg2, float arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.playSound(Lorg/bukkit/Location;Ljava/lang/String;FF)V");
    }
    void playSound(org.bukkit.Location arg0, org.bukkit.Sound arg1, org.bukkit.SoundCategory arg2, float arg3, float arg4);
    void playSound(org.bukkit.Location arg0, java.lang.String arg1, org.bukkit.SoundCategory arg2, float arg3, float arg4);
    void playSound(org.bukkit.Location arg0, org.bukkit.Sound arg1, org.bukkit.SoundCategory arg2, float arg3, float arg4, long arg5);
    void playSound(org.bukkit.Location arg0, java.lang.String arg1, org.bukkit.SoundCategory arg2, float arg3, float arg4, long arg5);
    default void playSound(org.bukkit.entity.Entity arg0, org.bukkit.Sound arg1, float arg2, float arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.playSound(Lorg/bukkit/entity/Entity;Lorg/bukkit/Sound;FF)V");
    }
    default void playSound(org.bukkit.entity.Entity arg0, java.lang.String arg1, float arg2, float arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.playSound(Lorg/bukkit/entity/Entity;Ljava/lang/String;FF)V");
    }
    void playSound(org.bukkit.entity.Entity arg0, org.bukkit.Sound arg1, org.bukkit.SoundCategory arg2, float arg3, float arg4);
    void playSound(org.bukkit.entity.Entity arg0, java.lang.String arg1, org.bukkit.SoundCategory arg2, float arg3, float arg4);
    void playSound(org.bukkit.entity.Entity arg0, org.bukkit.Sound arg1, org.bukkit.SoundCategory arg2, float arg3, float arg4, long arg5);
    void playSound(org.bukkit.entity.Entity arg0, java.lang.String arg1, org.bukkit.SoundCategory arg2, float arg3, float arg4, long arg5);
    default void stopSound(org.bukkit.Sound arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.stopSound(Lorg/bukkit/Sound;)V");
    }
    default void stopSound(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.stopSound(Ljava/lang/String;)V");
    }
    default void stopSound(org.bukkit.Sound arg0, org.bukkit.SoundCategory arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.stopSound(Lorg/bukkit/Sound;Lorg/bukkit/SoundCategory;)V");
    }
    void stopSound(java.lang.String arg0, org.bukkit.SoundCategory arg1);
    void stopSound(org.bukkit.SoundCategory arg0);
    void stopAllSounds();
    void playEffect(org.bukkit.Location arg0, org.bukkit.Effect arg1, int arg2);
    void playEffect(org.bukkit.Location arg0, org.bukkit.Effect arg1, java.lang.Object arg2);
    boolean breakBlock(org.bukkit.block.Block arg0);
    void sendBlockChange(org.bukkit.Location arg0, org.bukkit.Material arg1, byte arg2);
    void sendBlockChange(org.bukkit.Location arg0, org.bukkit.block.data.BlockData arg1);
    void sendBlockChanges(java.util.Collection arg0);
    default void sendBlockChanges(java.util.Collection arg0, boolean arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.sendBlockChanges(Ljava/util/Collection;Z)V");
    }
    default void sendBlockDamage(org.bukkit.Location arg0, float arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.sendBlockDamage(Lorg/bukkit/Location;F)V");
    }
    void sendMultiBlockChange(java.util.Map arg0);
    default void sendMultiBlockChange(java.util.Map arg0, boolean arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.sendMultiBlockChange(Ljava/util/Map;Z)V");
    }
    void sendBlockDamage(org.bukkit.Location arg0, float arg1, org.bukkit.entity.Entity arg2);
    void sendBlockDamage(org.bukkit.Location arg0, float arg1, int arg2);
    void sendEquipmentChange(org.bukkit.entity.LivingEntity arg0, org.bukkit.inventory.EquipmentSlot arg1, org.bukkit.inventory.ItemStack arg2);
    void sendEquipmentChange(org.bukkit.entity.LivingEntity arg0, java.util.Map arg1);
    default void sendSignChange(org.bukkit.Location arg0, java.util.List arg1) throws java.lang.IllegalArgumentException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.sendSignChange(Lorg/bukkit/Location;Ljava/util/List;)V");
    }
    default void sendSignChange(org.bukkit.Location arg0, java.util.List arg1, org.bukkit.DyeColor arg2) throws java.lang.IllegalArgumentException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.sendSignChange(Lorg/bukkit/Location;Ljava/util/List;Lorg/bukkit/DyeColor;)V");
    }
    default void sendSignChange(org.bukkit.Location arg0, java.util.List arg1, boolean arg2) throws java.lang.IllegalArgumentException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.sendSignChange(Lorg/bukkit/Location;Ljava/util/List;Z)V");
    }
    void sendSignChange(org.bukkit.Location arg0, java.util.List arg1, org.bukkit.DyeColor arg2, boolean arg3) throws java.lang.IllegalArgumentException;
    void sendSignChange(org.bukkit.Location arg0, java.lang.String[] arg1) throws java.lang.IllegalArgumentException;
    void sendSignChange(org.bukkit.Location arg0, java.lang.String[] arg1, org.bukkit.DyeColor arg2) throws java.lang.IllegalArgumentException;
    void sendSignChange(org.bukkit.Location arg0, java.lang.String[] arg1, org.bukkit.DyeColor arg2, boolean arg3) throws java.lang.IllegalArgumentException;
    void sendBlockUpdate(org.bukkit.Location arg0, org.bukkit.block.TileState arg1) throws java.lang.IllegalArgumentException;
    void sendPotionEffectChange(org.bukkit.entity.LivingEntity arg0, org.bukkit.potion.PotionEffect arg1);
    void sendPotionEffectChangeRemove(org.bukkit.entity.LivingEntity arg0, org.bukkit.potion.PotionEffectType arg1);
    void sendMap(org.bukkit.map.MapView arg0);
    void showWinScreen();
    boolean hasSeenWinScreen();
    void setHasSeenWinScreen(boolean arg0);
    default org.bukkit.BanEntry banPlayerFull(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.banPlayerFull(Ljava/lang/String;)Lorg/bukkit/BanEntry;");
        return null;
    }
    default org.bukkit.BanEntry banPlayerFull(java.lang.String arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.banPlayerFull(Ljava/lang/String;Ljava/lang/String;)Lorg/bukkit/BanEntry;");
        return null;
    }
    default org.bukkit.BanEntry banPlayerFull(java.lang.String arg0, java.util.Date arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.banPlayerFull(Ljava/lang/String;Ljava/util/Date;)Lorg/bukkit/BanEntry;");
        return null;
    }
    default org.bukkit.BanEntry banPlayerFull(java.lang.String arg0, java.util.Date arg1, java.lang.String arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.banPlayerFull(Ljava/lang/String;Ljava/util/Date;Ljava/lang/String;)Lorg/bukkit/BanEntry;");
        return null;
    }
    default org.bukkit.BanEntry banPlayerIP(java.lang.String arg0, boolean arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.banPlayerIP(Ljava/lang/String;Z)Lorg/bukkit/BanEntry;");
        return null;
    }
    default org.bukkit.BanEntry banPlayerIP(java.lang.String arg0, java.lang.String arg1, boolean arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.banPlayerIP(Ljava/lang/String;Ljava/lang/String;Z)Lorg/bukkit/BanEntry;");
        return null;
    }
    default org.bukkit.BanEntry banPlayerIP(java.lang.String arg0, java.util.Date arg1, boolean arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.banPlayerIP(Ljava/lang/String;Ljava/util/Date;Z)Lorg/bukkit/BanEntry;");
        return null;
    }
    default org.bukkit.BanEntry banPlayerIP(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.banPlayerIP(Ljava/lang/String;)Lorg/bukkit/BanEntry;");
        return null;
    }
    default org.bukkit.BanEntry banPlayerIP(java.lang.String arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.banPlayerIP(Ljava/lang/String;Ljava/lang/String;)Lorg/bukkit/BanEntry;");
        return null;
    }
    default org.bukkit.BanEntry banPlayerIP(java.lang.String arg0, java.util.Date arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.banPlayerIP(Ljava/lang/String;Ljava/util/Date;)Lorg/bukkit/BanEntry;");
        return null;
    }
    default org.bukkit.BanEntry banPlayerIP(java.lang.String arg0, java.util.Date arg1, java.lang.String arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.banPlayerIP(Ljava/lang/String;Ljava/util/Date;Ljava/lang/String;)Lorg/bukkit/BanEntry;");
        return null;
    }
    default org.bukkit.BanEntry banPlayerIP(java.lang.String arg0, java.util.Date arg1, java.lang.String arg2, boolean arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.banPlayerIP(Ljava/lang/String;Ljava/util/Date;Ljava/lang/String;Z)Lorg/bukkit/BanEntry;");
        return null;
    }
    void sendActionBar(java.lang.String arg0);
    void sendActionBar(char arg0, java.lang.String arg1);
    void sendActionBar(net.md_5.bungee.api.chat.BaseComponent[] arg0);
    default void sendMessage(net.md_5.bungee.api.chat.BaseComponent arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.sendMessage(Lnet/md_5/bungee/api/chat/BaseComponent;)V");
    }
    default void sendMessage(net.md_5.bungee.api.chat.BaseComponent[] arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.sendMessage([Lnet/md_5/bungee/api/chat/BaseComponent;)V");
    }
    default void sendMessage(net.md_5.bungee.api.ChatMessageType arg0, net.md_5.bungee.api.chat.BaseComponent[] arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.sendMessage(Lnet/md_5/bungee/api/ChatMessageType;[Lnet/md_5/bungee/api/chat/BaseComponent;)V");
    }
    void setPlayerListHeaderFooter(net.md_5.bungee.api.chat.BaseComponent[] arg0, net.md_5.bungee.api.chat.BaseComponent[] arg1);
    void setPlayerListHeaderFooter(net.md_5.bungee.api.chat.BaseComponent arg0, net.md_5.bungee.api.chat.BaseComponent arg1);
    void setTitleTimes(int arg0, int arg1, int arg2);
    void setSubtitle(net.md_5.bungee.api.chat.BaseComponent[] arg0);
    void setSubtitle(net.md_5.bungee.api.chat.BaseComponent arg0);
    void showTitle(net.md_5.bungee.api.chat.BaseComponent[] arg0);
    void showTitle(net.md_5.bungee.api.chat.BaseComponent arg0);
    void showTitle(net.md_5.bungee.api.chat.BaseComponent[] arg0, net.md_5.bungee.api.chat.BaseComponent[] arg1, int arg2, int arg3, int arg4);
    void showTitle(net.md_5.bungee.api.chat.BaseComponent arg0, net.md_5.bungee.api.chat.BaseComponent arg1, int arg2, int arg3, int arg4);
    void sendTitle(com.destroystokyo.paper.Title arg0);
    void updateTitle(com.destroystokyo.paper.Title arg0);
    void hideTitle();
    void sendHurtAnimation(float arg0);
    void sendLinks(org.bukkit.ServerLinks arg0);
    void addCustomChatCompletions(java.util.Collection arg0);
    void removeCustomChatCompletions(java.util.Collection arg0);
    void setCustomChatCompletions(java.util.Collection arg0);
    void updateInventory();
    org.bukkit.GameMode getPreviousGameMode();
    void setPlayerTime(long arg0, boolean arg1);
    long getPlayerTime();
    long getPlayerTimeOffset();
    boolean isPlayerTimeRelative();
    void resetPlayerTime();
    void setPlayerWeather(org.bukkit.WeatherType arg0);
    org.bukkit.WeatherType getPlayerWeather();
    void resetPlayerWeather();
    default void giveExp(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.giveExp(I)V");
    }
    int getExpCooldown();
    void setExpCooldown(int arg0);
    void giveExp(int arg0, boolean arg1);
    int applyMending(int arg0);
    void giveExpLevels(int arg0);
    float getExp();
    void setExp(float arg0);
    int getLevel();
    void setLevel(int arg0);
    int getTotalExperience();
    void setTotalExperience(int arg0);
    int calculateTotalExperiencePoints();
    void setExperienceLevelAndProgress(int arg0);
    int getExperiencePointsNeededForNextLevel();
    void sendExperienceChange(float arg0);
    void sendExperienceChange(float arg0, int arg1);
    boolean getAllowFlight();
    void setAllowFlight(boolean arg0);
    void setFlyingFallDamage(net.kyori.adventure.util.TriState arg0);
    net.kyori.adventure.util.TriState hasFlyingFallDamage();
    void hidePlayer(org.bukkit.entity.Player arg0);
    default void hidePlayer(org.bukkit.plugin.Plugin arg0, org.bukkit.entity.Player arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.hidePlayer(Lorg/bukkit/plugin/Plugin;Lorg/bukkit/entity/Player;)V");
    }
    void showPlayer(org.bukkit.entity.Player arg0);
    default void showPlayer(org.bukkit.plugin.Plugin arg0, org.bukkit.entity.Player arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.showPlayer(Lorg/bukkit/plugin/Plugin;Lorg/bukkit/entity/Player;)V");
    }
    boolean canSee(org.bukkit.entity.Player arg0);
    void hideEntity(org.bukkit.plugin.Plugin arg0, org.bukkit.entity.Entity arg1);
    void showEntity(org.bukkit.plugin.Plugin arg0, org.bukkit.entity.Entity arg1);
    boolean canSee(org.bukkit.entity.Entity arg0);
    boolean isListed(org.bukkit.entity.Player arg0);
    boolean unlistPlayer(org.bukkit.entity.Player arg0);
    boolean listPlayer(org.bukkit.entity.Player arg0);
    boolean isFlying();
    void setFlying(boolean arg0);
    void setFlySpeed(float arg0) throws java.lang.IllegalArgumentException;
    void setWalkSpeed(float arg0) throws java.lang.IllegalArgumentException;
    float getFlySpeed();
    float getWalkSpeed();
    default void setTexturePack(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.setTexturePack(Ljava/lang/String;)V");
    }
    default void setResourcePack(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.setResourcePack(Ljava/lang/String;)V");
    }
    default void setResourcePack(java.lang.String arg0, byte[] arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.setResourcePack(Ljava/lang/String;[B)V");
    }
    default void setResourcePack(java.lang.String arg0, byte[] arg1, java.lang.String arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.setResourcePack(Ljava/lang/String;[BLjava/lang/String;)V");
    }
    default void setResourcePack(java.lang.String arg0, byte[] arg1, net.kyori.adventure.text.Component arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.setResourcePack(Ljava/lang/String;[BLnet/kyori/adventure/text/Component;)V");
    }
    default void setResourcePack(java.lang.String arg0, byte[] arg1, boolean arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.setResourcePack(Ljava/lang/String;[BZ)V");
    }
    void setResourcePack(java.lang.String arg0, byte[] arg1, java.lang.String arg2, boolean arg3);
    default void setResourcePack(java.lang.String arg0, byte[] arg1, net.kyori.adventure.text.Component arg2, boolean arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.setResourcePack(Ljava/lang/String;[BLnet/kyori/adventure/text/Component;Z)V");
    }
    void setResourcePack(java.util.UUID arg0, java.lang.String arg1, byte[] arg2, java.lang.String arg3, boolean arg4);
    void setResourcePack(java.util.UUID arg0, java.lang.String arg1, byte[] arg2, net.kyori.adventure.text.Component arg3, boolean arg4);
    default void setResourcePack(java.lang.String arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.setResourcePack(Ljava/lang/String;Ljava/lang/String;)V");
    }
    default void setResourcePack(java.lang.String arg0, java.lang.String arg1, boolean arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.setResourcePack(Ljava/lang/String;Ljava/lang/String;Z)V");
    }
    default void setResourcePack(java.lang.String arg0, java.lang.String arg1, boolean arg2, net.kyori.adventure.text.Component arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.setResourcePack(Ljava/lang/String;Ljava/lang/String;ZLnet/kyori/adventure/text/Component;)V");
    }
    default void setResourcePack(java.util.UUID arg0, java.lang.String arg1, java.lang.String arg2, net.kyori.adventure.text.Component arg3, boolean arg4) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.setResourcePack(Ljava/util/UUID;Ljava/lang/String;Ljava/lang/String;Lnet/kyori/adventure/text/Component;Z)V");
    }
    org.bukkit.event.player.PlayerResourcePackStatusEvent$Status getResourcePackStatus();
    default java.lang.String getResourcePackHash() {
        return null;
    }
    default boolean hasResourcePack() {
        return false;
    }
    void addResourcePack(java.util.UUID arg0, java.lang.String arg1, byte[] arg2, java.lang.String arg3, boolean arg4);
    void removeResourcePack(java.util.UUID arg0);
    void removeResourcePacks();
    org.bukkit.scoreboard.Scoreboard getScoreboard();
    void setScoreboard(org.bukkit.scoreboard.Scoreboard arg0) throws java.lang.IllegalArgumentException, java.lang.IllegalStateException;
    org.bukkit.WorldBorder getWorldBorder();
    void setWorldBorder(org.bukkit.WorldBorder arg0);
    void sendHealthUpdate(double arg0, int arg1, float arg2);
    void sendHealthUpdate();
    boolean isHealthScaled();
    void setHealthScaled(boolean arg0);
    void setHealthScale(double arg0) throws java.lang.IllegalArgumentException;
    double getHealthScale();
    org.bukkit.entity.Entity getSpectatorTarget();
    void setSpectatorTarget(org.bukkit.entity.Entity arg0);
    void sendTitle(java.lang.String arg0, java.lang.String arg1);
    void sendTitle(java.lang.String arg0, java.lang.String arg1, int arg2, int arg3, int arg4);
    void resetTitle();
    default void spawnParticle(org.bukkit.Particle arg0, org.bukkit.Location arg1, int arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.spawnParticle(Lorg/bukkit/Particle;Lorg/bukkit/Location;I)V");
    }
    default void spawnParticle(org.bukkit.Particle arg0, double arg1, double arg2, double arg3, int arg4) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.spawnParticle(Lorg/bukkit/Particle;DDDI)V");
    }
    default void spawnParticle(org.bukkit.Particle arg0, org.bukkit.Location arg1, int arg2, java.lang.Object arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.spawnParticle(Lorg/bukkit/Particle;Lorg/bukkit/Location;ILjava/lang/Object;)V");
    }
    default void spawnParticle(org.bukkit.Particle arg0, double arg1, double arg2, double arg3, int arg4, java.lang.Object arg5) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.spawnParticle(Lorg/bukkit/Particle;DDDILjava/lang/Object;)V");
    }
    default void spawnParticle(org.bukkit.Particle arg0, org.bukkit.Location arg1, int arg2, double arg3, double arg4, double arg5) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.spawnParticle(Lorg/bukkit/Particle;Lorg/bukkit/Location;IDDD)V");
    }
    default void spawnParticle(org.bukkit.Particle arg0, double arg1, double arg2, double arg3, int arg4, double arg5, double arg6, double arg7) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.spawnParticle(Lorg/bukkit/Particle;DDDIDDD)V");
    }
    default void spawnParticle(org.bukkit.Particle arg0, org.bukkit.Location arg1, int arg2, double arg3, double arg4, double arg5, java.lang.Object arg6) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.spawnParticle(Lorg/bukkit/Particle;Lorg/bukkit/Location;IDDDLjava/lang/Object;)V");
    }
    default void spawnParticle(org.bukkit.Particle arg0, double arg1, double arg2, double arg3, int arg4, double arg5, double arg6, double arg7, java.lang.Object arg8) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.spawnParticle(Lorg/bukkit/Particle;DDDIDDDLjava/lang/Object;)V");
    }
    default void spawnParticle(org.bukkit.Particle arg0, org.bukkit.Location arg1, int arg2, double arg3, double arg4, double arg5, double arg6) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.spawnParticle(Lorg/bukkit/Particle;Lorg/bukkit/Location;IDDDD)V");
    }
    default void spawnParticle(org.bukkit.Particle arg0, double arg1, double arg2, double arg3, int arg4, double arg5, double arg6, double arg7, double arg8) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.spawnParticle(Lorg/bukkit/Particle;DDDIDDDD)V");
    }
    default void spawnParticle(org.bukkit.Particle arg0, org.bukkit.Location arg1, int arg2, double arg3, double arg4, double arg5, double arg6, java.lang.Object arg7) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.spawnParticle(Lorg/bukkit/Particle;Lorg/bukkit/Location;IDDDDLjava/lang/Object;)V");
    }
    default void spawnParticle(org.bukkit.Particle arg0, double arg1, double arg2, double arg3, int arg4, double arg5, double arg6, double arg7, double arg8, java.lang.Object arg9) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.spawnParticle(Lorg/bukkit/Particle;DDDIDDDDLjava/lang/Object;)V");
    }
    default void spawnParticle(org.bukkit.Particle arg0, org.bukkit.Location arg1, int arg2, double arg3, double arg4, double arg5, double arg6, java.lang.Object arg7, boolean arg8) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.spawnParticle(Lorg/bukkit/Particle;Lorg/bukkit/Location;IDDDDLjava/lang/Object;Z)V");
    }
    void spawnParticle(org.bukkit.Particle arg0, double arg1, double arg2, double arg3, int arg4, double arg5, double arg6, double arg7, double arg8, java.lang.Object arg9, boolean arg10);
    org.bukkit.advancement.AdvancementProgress getAdvancementProgress(org.bukkit.advancement.Advancement arg0);
    int getClientViewDistance();
    java.util.Locale locale();
    int getPing();
    java.lang.String getLocale();
    boolean getAffectsSpawning();
    void setAffectsSpawning(boolean arg0);
    int getViewDistance();
    void setViewDistance(int arg0);
    int getSimulationDistance();
    void setSimulationDistance(int arg0);
    default int getNoTickViewDistance() {
        return 0;
    }
    default void setNoTickViewDistance(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.setNoTickViewDistance(I)V");
    }
    int getSendViewDistance();
    void setSendViewDistance(int arg0);
    void updateCommands();
    void openBook(org.bukkit.inventory.ItemStack arg0);
    default void openSign(org.bukkit.block.Sign arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.openSign(Lorg/bukkit/block/Sign;)V");
    }
    void openSign(org.bukkit.block.Sign arg0, org.bukkit.block.sign.Side arg1);
    void openVirtualSign(io.papermc.paper.math.Position arg0, org.bukkit.block.sign.Side arg1);
    void showDemoScreen();
    boolean isAllowingServerListings();
    default net.kyori.adventure.text.event.HoverEvent asHoverEvent(java.util.function.UnaryOperator arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.asHoverEvent(Ljava/util/function/UnaryOperator;)Lnet/kyori/adventure/text/event/HoverEvent;");
        return null;
    }
    default void applySkinToPlayerHeadContents(net.kyori.adventure.text.object.PlayerHeadObjectContents$Builder arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.applySkinToPlayerHeadContents(Lnet/kyori/adventure/text/object/PlayerHeadObjectContents$Builder;)V");
    }
    com.destroystokyo.paper.profile.PlayerProfile getPlayerProfile();
    void setPlayerProfile(com.destroystokyo.paper.profile.PlayerProfile arg0);
    float getCooldownPeriod();
    float getCooledAttackStrength(float arg0);
    void resetCooldown();
    java.lang.Object getClientOption(com.destroystokyo.paper.ClientOption arg0);
    default org.bukkit.entity.Firework boostElytra(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.boostElytra(Lorg/bukkit/inventory/ItemStack;)Lorg/bukkit/entity/Firework;");
        return null;
    }
    void sendOpLevel(byte arg0);
    void addAdditionalChatCompletions(java.util.Collection arg0);
    void removeAdditionalChatCompletions(java.util.Collection arg0);
    java.lang.String getClientBrandName();
    void setRotation(float arg0, float arg1);
    void lookAt(org.bukkit.entity.Entity arg0, io.papermc.paper.entity.LookAnchor arg1, io.papermc.paper.entity.LookAnchor arg2);
    default void showElderGuardian() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.showElderGuardian()V");
    }
    void showElderGuardian(boolean arg0);
    int getWardenWarningCooldown();
    void setWardenWarningCooldown(int arg0);
    int getWardenTimeSinceLastWarning();
    void setWardenTimeSinceLastWarning(int arg0);
    int getWardenWarningLevel();
    void setWardenWarningLevel(int arg0);
    void increaseWardenWarningLevel();
    java.time.Duration getIdleDuration();
    void resetIdleDuration();
    java.util.Set getSentChunkKeys();
    java.util.Set getSentChunks();
    default boolean isChunkSent(org.bukkit.Chunk arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.isChunkSent(Lorg/bukkit/Chunk;)Z");
        return false;
    }
    boolean isChunkSent(long arg0);
    org.bukkit.entity.Player$Spigot spigot();
    void sendEntityEffect(org.bukkit.EntityEffect arg0, org.bukkit.entity.Entity arg1);
    default io.papermc.paper.entity.PlayerGiveResult give(org.bukkit.inventory.ItemStack[] arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.give([Lorg/bukkit/inventory/ItemStack;)Lio/papermc/paper/entity/PlayerGiveResult;");
        return null;
    }
    default io.papermc.paper.entity.PlayerGiveResult give(java.util.Collection arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Player.give(Ljava/util/Collection;)Lio/papermc/paper/entity/PlayerGiveResult;");
        return null;
    }
    io.papermc.paper.entity.PlayerGiveResult give(java.util.Collection arg0, boolean arg1);
    int getDeathScreenScore();
    void setDeathScreenScore(int arg0);
    io.papermc.paper.connection.PlayerGameConnection getConnection();
}
