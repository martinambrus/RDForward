package com.github.martinambrus.rdforward.bridge.bukkit.fixtures;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class TestBukkitListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        System.setProperty(TestBukkitPlugin.PROP_FIRED,
                event.getPlayer().getName() + "," + event.getX() + "," + event.getY()
                        + "," + event.getZ() + "," + event.getNewBlockType());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        System.setProperty(TestBukkitPlugin.PROP_MOVE,
                event.getPlayer().getName()
                        + "," + event.getTo().getX()
                        + "," + event.getTo().getY()
                        + "," + event.getTo().getZ()
                        + "," + event.getTo().getYaw()
                        + "," + event.getTo().getPitch());
    }
}
