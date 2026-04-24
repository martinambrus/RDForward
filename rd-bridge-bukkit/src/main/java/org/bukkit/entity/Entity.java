package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Entity extends org.bukkit.metadata.Metadatable, org.bukkit.command.CommandSender, org.bukkit.Nameable, org.bukkit.persistence.PersistentDataHolder, net.kyori.adventure.text.event.HoverEventSource, net.kyori.adventure.sound.Sound$Emitter, io.papermc.paper.datacomponent.DataComponentView {
    org.bukkit.Location getLocation();
    org.bukkit.Location getLocation(org.bukkit.Location arg0);
    void setVelocity(org.bukkit.util.Vector arg0);
    org.bukkit.util.Vector getVelocity();
    double getHeight();
    double getWidth();
    org.bukkit.util.BoundingBox getBoundingBox();
    boolean isOnGround();
    boolean isInWater();
    org.bukkit.World getWorld();
    void setRotation(float arg0, float arg1);
    default boolean teleport(org.bukkit.Location arg0, io.papermc.paper.entity.TeleportFlag[] arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Entity.teleport(Lorg/bukkit/Location;[Lio/papermc/paper/entity/TeleportFlag;)Z");
        return false;
    }
    boolean teleport(org.bukkit.Location arg0, org.bukkit.event.player.PlayerTeleportEvent$TeleportCause arg1, io.papermc.paper.entity.TeleportFlag[] arg2);
    void lookAt(double arg0, double arg1, double arg2, io.papermc.paper.entity.LookAnchor arg3);
    default void lookAt(io.papermc.paper.math.Position arg0, io.papermc.paper.entity.LookAnchor arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Entity.lookAt(Lio/papermc/paper/math/Position;Lio/papermc/paper/entity/LookAnchor;)V");
    }
    boolean teleport(org.bukkit.Location arg0);
    boolean teleport(org.bukkit.Location arg0, org.bukkit.event.player.PlayerTeleportEvent$TeleportCause arg1);
    boolean teleport(org.bukkit.entity.Entity arg0);
    boolean teleport(org.bukkit.entity.Entity arg0, org.bukkit.event.player.PlayerTeleportEvent$TeleportCause arg1);
    default java.util.concurrent.CompletableFuture teleportAsync(org.bukkit.Location arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Entity.teleportAsync(Lorg/bukkit/Location;)Ljava/util/concurrent/CompletableFuture;");
        return null;
    }
    default java.util.concurrent.CompletableFuture teleportAsync(org.bukkit.Location arg0, org.bukkit.event.player.PlayerTeleportEvent$TeleportCause arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Entity.teleportAsync(Lorg/bukkit/Location;Lorg/bukkit/event/player/PlayerTeleportEvent$TeleportCause;)Ljava/util/concurrent/CompletableFuture;");
        return null;
    }
    default java.util.concurrent.CompletableFuture teleportAsync(org.bukkit.Location arg0, io.papermc.paper.entity.TeleportFlag[] arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Entity.teleportAsync(Lorg/bukkit/Location;[Lio/papermc/paper/entity/TeleportFlag;)Ljava/util/concurrent/CompletableFuture;");
        return null;
    }
    java.util.concurrent.CompletableFuture teleportAsync(org.bukkit.Location arg0, org.bukkit.event.player.PlayerTeleportEvent$TeleportCause arg1, io.papermc.paper.entity.TeleportFlag[] arg2);
    java.util.List getNearbyEntities(double arg0, double arg1, double arg2);
    int getEntityId();
    int getFireTicks();
    int getMaxFireTicks();
    void setFireTicks(int arg0);
    void setVisualFire(boolean arg0);
    void setVisualFire(net.kyori.adventure.util.TriState arg0);
    boolean isVisualFire();
    net.kyori.adventure.util.TriState getVisualFire();
    int getFreezeTicks();
    int getMaxFreezeTicks();
    void setFreezeTicks(int arg0);
    boolean isFrozen();
    void setInvisible(boolean arg0);
    boolean isInvisible();
    void setNoPhysics(boolean arg0);
    boolean hasNoPhysics();
    boolean isFreezeTickingLocked();
    void lockFreezeTicks(boolean arg0);
    void remove();
    boolean isDead();
    boolean isValid();
    org.bukkit.Server getServer();
    boolean isPersistent();
    void setPersistent(boolean arg0);
    org.bukkit.entity.Entity getPassenger();
    boolean setPassenger(org.bukkit.entity.Entity arg0);
    java.util.List getPassengers();
    boolean addPassenger(org.bukkit.entity.Entity arg0);
    boolean removePassenger(org.bukkit.entity.Entity arg0);
    boolean isEmpty();
    boolean eject();
    org.bukkit.inventory.ItemStack getPickItemStack();
    float getFallDistance();
    void setFallDistance(float arg0);
    void setLastDamageCause(org.bukkit.event.entity.EntityDamageEvent arg0);
    org.bukkit.event.entity.EntityDamageEvent getLastDamageCause();
    java.util.UUID getUniqueId();
    int getTicksLived();
    void setTicksLived(int arg0);
    void playEffect(org.bukkit.EntityEffect arg0);
    org.bukkit.entity.EntityType getType();
    org.bukkit.Sound getSwimSound();
    org.bukkit.Sound getSwimSplashSound();
    org.bukkit.Sound getSwimHighSpeedSplashSound();
    boolean isInsideVehicle();
    boolean leaveVehicle();
    org.bukkit.entity.Entity getVehicle();
    void setCustomNameVisible(boolean arg0);
    boolean isCustomNameVisible();
    void setVisibleByDefault(boolean arg0);
    boolean isVisibleByDefault();
    java.util.Set getTrackedBy();
    boolean isTrackedBy(org.bukkit.entity.Player arg0);
    void setGlowing(boolean arg0);
    boolean isGlowing();
    void setInvulnerable(boolean arg0);
    boolean isInvulnerable();
    boolean isSilent();
    void setSilent(boolean arg0);
    boolean hasGravity();
    void setGravity(boolean arg0);
    int getPortalCooldown();
    void setPortalCooldown(int arg0);
    java.util.Set getScoreboardTags();
    boolean addScoreboardTag(java.lang.String arg0);
    boolean removeScoreboardTag(java.lang.String arg0);
    org.bukkit.block.PistonMoveReaction getPistonMoveReaction();
    org.bukkit.block.BlockFace getFacing();
    org.bukkit.entity.Pose getPose();
    boolean isSneaking();
    void setSneaking(boolean arg0);
    default void setPose(org.bukkit.entity.Pose arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Entity.setPose(Lorg/bukkit/entity/Pose;)V");
    }
    void setPose(org.bukkit.entity.Pose arg0, boolean arg1);
    boolean hasFixedPose();
    org.bukkit.entity.SpawnCategory getSpawnCategory();
    boolean isInWorld();
    java.lang.String getAsString();
    org.bukkit.entity.EntitySnapshot createSnapshot();
    org.bukkit.entity.Entity copy();
    org.bukkit.entity.Entity copy(org.bukkit.Location arg0);
    org.bukkit.entity.Entity$Spigot spigot();
    net.kyori.adventure.text.Component teamDisplayName();
    default net.kyori.adventure.text.event.HoverEvent asHoverEvent(java.util.function.UnaryOperator arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Entity.asHoverEvent(Ljava/util/function/UnaryOperator;)Lnet/kyori/adventure/text/event/HoverEvent;");
        return null;
    }
    org.bukkit.Location getOrigin();
    boolean fromMobSpawner();
    default org.bukkit.Chunk getChunk() {
        return null;
    }
    org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason getEntitySpawnReason();
    boolean isUnderWater();
    boolean isInRain();
    default boolean isInBubbleColumn() {
        return false;
    }
    default boolean isInWaterOrRain() {
        return false;
    }
    default boolean isInWaterOrBubbleColumn() {
        return false;
    }
    default boolean isInWaterOrRainOrBubbleColumn() {
        return false;
    }
    boolean isInLava();
    boolean isTicking();
    java.util.Set getTrackedPlayers();
    default boolean spawnAt(org.bukkit.Location arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Entity.spawnAt(Lorg/bukkit/Location;)Z");
        return false;
    }
    boolean spawnAt(org.bukkit.Location arg0, org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason arg1);
    boolean isInPowderedSnow();
    double getX();
    double getY();
    double getZ();
    float getPitch();
    float getYaw();
    boolean collidesAt(org.bukkit.Location arg0);
    boolean wouldCollideUsing(org.bukkit.util.BoundingBox arg0);
    io.papermc.paper.threadedregions.scheduler.EntityScheduler getScheduler();
    java.lang.String getScoreboardEntryName();
    void broadcastHurtAnimation(java.util.Collection arg0);
}
