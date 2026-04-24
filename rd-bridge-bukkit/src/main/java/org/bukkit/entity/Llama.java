package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Llama extends org.bukkit.entity.ChestedHorse, com.destroystokyo.paper.entity.RangedEntity {
    org.bukkit.entity.Llama$Color getColor();
    void setColor(org.bukkit.entity.Llama$Color arg0);
    int getStrength();
    void setStrength(int arg0);
    org.bukkit.inventory.LlamaInventory getInventory();
    boolean inCaravan();
    void joinCaravan(org.bukkit.entity.Llama arg0);
    void leaveCaravan();
    org.bukkit.entity.Llama getCaravanHead();
    boolean hasCaravanTail();
    org.bukkit.entity.Llama getCaravanTail();
}
