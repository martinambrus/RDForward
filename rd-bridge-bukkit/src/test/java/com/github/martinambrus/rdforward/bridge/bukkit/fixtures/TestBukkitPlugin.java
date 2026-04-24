package com.github.martinambrus.rdforward.bridge.bukkit.fixtures;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class TestBukkitPlugin extends JavaPlugin {

    public static final String PROP_LOAD = "rdforward.test.bukkit.load";
    public static final String PROP_ENABLE = "rdforward.test.bukkit.enable";
    public static final String PROP_DISABLE = "rdforward.test.bukkit.disable";
    public static final String PROP_FIRED = "rdforward.test.bukkit.fired";
    public static final String PROP_MOVE = "rdforward.test.bukkit.move";
    public static final String PROP_CMD_FIRED = "rdforward.test.bukkit.cmd_fired";

    @Override
    public void onLoad() {
        System.setProperty(PROP_LOAD, "true");
    }

    @Override
    public void onEnable() {
        System.setProperty(PROP_ENABLE, "true");
        registerListener(new TestBukkitListener());

        PluginCommand hello = getCommand("hello");
        if (hello != null) {
            hello.setExecutor((sender, command, label, args) -> {
                StringBuilder sb = new StringBuilder();
                sb.append(sender.getName()).append("|").append(label).append("|");
                for (int i = 0; i < args.length; i++) {
                    if (i > 0) sb.append(",");
                    sb.append(args[i]);
                }
                System.setProperty(PROP_CMD_FIRED, sb.toString());
                sender.sendMessage("hello ack");
                return true;
            });
        }
    }

    @Override
    public void onDisable() {
        System.setProperty(PROP_DISABLE, "true");
    }
}
