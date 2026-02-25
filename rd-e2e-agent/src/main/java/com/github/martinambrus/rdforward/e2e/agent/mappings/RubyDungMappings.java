package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for the RubyDung prototype (rd-132211).
 * RubyDung is NOT obfuscated — all field/method names are human-readable.
 *
 * Server connection is handled by the rd-client Fabric Mixin via CLI args
 * (--server=host:port), not by setting fields on the game class. Therefore
 * serverHostFieldName() and serverPortFieldName() return null.
 *
 * Note: Player position fields are float (not double like Alpha).
 * GameState.getPlayerPosition() handles this via Field.getDouble() widening.
 */
public class RubyDungMappings implements FieldMappings {

    @Override
    public String minecraftClassName() {
        return "com.mojang.rubydung.RubyDung";
    }

    @Override
    public String runMethodName() {
        return "run";
    }

    @Override
    public String tickMethodName() {
        return "tick";
    }

    // Player field on RubyDung class
    @Override
    public String playerFieldName() {
        return "player";
    }

    // Level field on RubyDung class
    @Override
    public String worldFieldName() {
        return "level";
    }

    // Server connection handled by Fabric Mixin via CLI args, not by reflection
    @Override
    public String serverHostFieldName() {
        return null;
    }

    @Override
    public String serverPortFieldName() {
        return null;
    }

    @Override
    public String displayWidthFieldName() {
        return "width";
    }

    @Override
    public String displayHeightFieldName() {
        return "height";
    }

    // Player position fields (float, not double)
    @Override
    public String posXFieldName() {
        return "x";
    }

    @Override
    public String posYFieldName() {
        return "y";
    }

    @Override
    public String posZFieldName() {
        return "z";
    }

    // Phase 2: rotation, ground, mouse, block fields
    @Override
    public String yawFieldName() {
        return "yRot";
    }

    @Override
    public String pitchFieldName() {
        return "xRot";
    }

    @Override
    public String onGroundFieldName() {
        return "onGround";
    }

    @Override
    public String mouseGrabbedFieldName() {
        return "mouseGrabbed";
    }

    @Override
    public String getBlockIdMethodName() {
        return "isTile";
    }

    // No Session object in RubyDung
    @Override
    public String sessionFieldName() {
        return null;
    }

    // Block interaction via Level.setTile() and RubyDung.blockChangeQueue
    @Override
    public String setTileMethodName() {
        return "setTile";
    }

    @Override
    public String blockChangeQueueFieldName() {
        return "blockChangeQueue";
    }
}
