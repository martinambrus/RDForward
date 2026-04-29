package com.github.martinambrus.rdforward.server.abilities;

import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import com.github.martinambrus.rdforward.server.api.ServerProperties;
import com.github.martinambrus.rdforward.server.bedrock.BedrockSessionWrapper;
import org.cloudburstmc.protocol.bedrock.data.Ability;
import org.cloudburstmc.protocol.bedrock.data.AbilityLayer;
import org.cloudburstmc.protocol.bedrock.data.command.CommandPermission;
import org.cloudburstmc.protocol.bedrock.data.PlayerPermission;
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket;

/**
 * Pushes Bedrock {@link UpdateAbilitiesPacket} with the BASE layer's
 * fly/walk speed fields set to the player's currently stored values.
 * Mirrors the gamemode-aware ability set used during initial join in
 * BedrockGameplayHandler.
 */
public final class BedrockAbilitiesDispatcher implements AbilitiesSpeedDispatcher {

    public static final BedrockAbilitiesDispatcher INSTANCE = new BedrockAbilitiesDispatcher();

    private BedrockAbilitiesDispatcher() {}

    @Override
    public void push(ConnectedPlayer player) {
        BedrockSessionWrapper session = player.getBedrockSession();
        if (session == null) return;

        long entityId = player.getPlayerId() + 1;

        UpdateAbilitiesPacket pkt = new UpdateAbilitiesPacket();
        pkt.setUniqueEntityId(entityId);
        pkt.setPlayerPermission(PlayerPermission.OPERATOR);
        pkt.setCommandPermission(CommandPermission.ANY);

        AbilityLayer layer = new AbilityLayer();
        layer.setLayerType(AbilityLayer.Type.BASE);
        layer.getAbilitiesSet().add(Ability.BUILD);
        layer.getAbilitiesSet().add(Ability.MINE);
        layer.getAbilitiesSet().add(Ability.DOORS_AND_SWITCHES);
        layer.getAbilitiesSet().add(Ability.OPEN_CONTAINERS);
        layer.getAbilitiesSet().add(Ability.ATTACK_PLAYERS);
        layer.getAbilitiesSet().add(Ability.ATTACK_MOBS);
        layer.getAbilitiesSet().add(Ability.OPERATOR_COMMANDS);
        layer.getAbilitiesSet().add(Ability.TELEPORT);
        layer.getAbilitiesSet().add(Ability.INVULNERABLE);
        layer.getAbilitiesSet().add(Ability.FLYING);
        layer.getAbilitiesSet().add(Ability.MAY_FLY);
        layer.getAbilitiesSet().add(Ability.INSTABUILD);
        layer.getAbilitiesSet().add(Ability.LIGHTNING);
        layer.getAbilitiesSet().add(Ability.FLY_SPEED);
        layer.getAbilitiesSet().add(Ability.WALK_SPEED);
        layer.getAbilitiesSet().add(Ability.MUTED);
        layer.getAbilitiesSet().add(Ability.WORLD_BUILDER);
        layer.getAbilitiesSet().add(Ability.NO_CLIP);

        int gm = ServerProperties.getGameMode();
        layer.getAbilityValues().add(Ability.BUILD);
        layer.getAbilityValues().add(Ability.MINE);
        layer.getAbilityValues().add(Ability.DOORS_AND_SWITCHES);
        layer.getAbilityValues().add(Ability.OPEN_CONTAINERS);
        layer.getAbilityValues().add(Ability.ATTACK_PLAYERS);
        layer.getAbilityValues().add(Ability.ATTACK_MOBS);
        layer.getAbilityValues().add(Ability.OPERATOR_COMMANDS);
        layer.getAbilityValues().add(Ability.TELEPORT);
        if (gm == 1) {
            layer.getAbilityValues().add(Ability.INVULNERABLE);
            layer.getAbilityValues().add(Ability.MAY_FLY);
            layer.getAbilityValues().add(Ability.INSTABUILD);
        } else if (gm == 3) {
            layer.getAbilityValues().add(Ability.MAY_FLY);
            layer.getAbilityValues().add(Ability.FLYING);
        }

        layer.setFlySpeed(player.getFlySpeed());
        layer.setWalkSpeed(player.getWalkSpeed());

        pkt.getAbilityLayers().add(layer);
        session.sendDirect(pkt);
    }
}
