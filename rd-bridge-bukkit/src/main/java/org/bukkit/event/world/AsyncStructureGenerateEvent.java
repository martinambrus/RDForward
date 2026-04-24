package org.bukkit.event.world;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class AsyncStructureGenerateEvent extends org.bukkit.event.world.WorldEvent {
    public AsyncStructureGenerateEvent(org.bukkit.World arg0, boolean arg1, org.bukkit.event.world.AsyncStructureGenerateEvent$Cause arg2, org.bukkit.generator.structure.Structure arg3, org.bukkit.util.BoundingBox arg4, int arg5, int arg6) { super((org.bukkit.World) null); }
    public AsyncStructureGenerateEvent() { super((org.bukkit.World) null); }
    public org.bukkit.event.world.AsyncStructureGenerateEvent$Cause getCause() {
        return null;
    }
    public org.bukkit.generator.structure.Structure getStructure() {
        return null;
    }
    public org.bukkit.util.BoundingBox getBoundingBox() {
        return null;
    }
    public int getChunkX() {
        return 0;
    }
    public int getChunkZ() {
        return 0;
    }
    public org.bukkit.util.BlockTransformer getBlockTransformer(org.bukkit.NamespacedKey arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.world.AsyncStructureGenerateEvent.getBlockTransformer(Lorg/bukkit/NamespacedKey;)Lorg/bukkit/util/BlockTransformer;");
        return null;
    }
    public void setBlockTransformer(org.bukkit.NamespacedKey arg0, org.bukkit.util.BlockTransformer arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.world.AsyncStructureGenerateEvent.setBlockTransformer(Lorg/bukkit/NamespacedKey;Lorg/bukkit/util/BlockTransformer;)V");
    }
    public void removeBlockTransformer(org.bukkit.NamespacedKey arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.world.AsyncStructureGenerateEvent.removeBlockTransformer(Lorg/bukkit/NamespacedKey;)V");
    }
    public void clearBlockTransformers() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.world.AsyncStructureGenerateEvent.clearBlockTransformers()V");
    }
    public java.util.Map getBlockTransformers() {
        return java.util.Collections.emptyMap();
    }
    public org.bukkit.util.EntityTransformer getEntityTransformer(org.bukkit.NamespacedKey arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.world.AsyncStructureGenerateEvent.getEntityTransformer(Lorg/bukkit/NamespacedKey;)Lorg/bukkit/util/EntityTransformer;");
        return null;
    }
    public void setEntityTransformer(org.bukkit.NamespacedKey arg0, org.bukkit.util.EntityTransformer arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.world.AsyncStructureGenerateEvent.setEntityTransformer(Lorg/bukkit/NamespacedKey;Lorg/bukkit/util/EntityTransformer;)V");
    }
    public void removeEntityTransformer(org.bukkit.NamespacedKey arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.world.AsyncStructureGenerateEvent.removeEntityTransformer(Lorg/bukkit/NamespacedKey;)V");
    }
    public void clearEntityTransformers() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.world.AsyncStructureGenerateEvent.clearEntityTransformers()V");
    }
    public java.util.Map getEntityTransformers() {
        return java.util.Collections.emptyMap();
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
