package io.papermc.paper.command.brigadier;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface CommandSourceStack {
    org.bukkit.Location getLocation();
    org.bukkit.command.CommandSender getSender();
    org.bukkit.entity.Entity getExecutor();
    io.papermc.paper.command.brigadier.CommandSourceStack withLocation(org.bukkit.Location arg0);
    io.papermc.paper.command.brigadier.CommandSourceStack withExecutor(org.bukkit.entity.Entity arg0);
}
