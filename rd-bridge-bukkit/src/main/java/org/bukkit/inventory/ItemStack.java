// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar, with
 *  hand-tuned legacy fields/ctors for plugins compiled against
 *  pre-Flattening Bukkit (WorldEdit 5.6.1 etc.). See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class ItemStack implements java.lang.Cloneable, org.bukkit.configuration.serialization.ConfigurationSerializable, org.bukkit.Translatable, net.kyori.adventure.text.event.HoverEventSource, net.kyori.adventure.translation.Translatable, io.papermc.paper.persistence.PersistentDataViewHolder, io.papermc.paper.datacomponent.DataComponentHolder {

    private org.bukkit.Material material;
    private int amount;
    private short durability;
    /** Pre-Flattening Notch numeric ID. Stored alongside {@link #material}
     *  so {@code getTypeId()} round-trips even when the id is outside
     *  RDForward's surfaced {@link org.bukkit.Material} enum (which only
     *  exposes ~21 blocks; items like sandstone/pumpkin/ladders are not
     *  modelled but plugins still need {@code getTypeId()} to return the
     *  original wire id for save/restore). InventoryPresets serialises
     *  {@code id:count:damage} to disk — without this fallback the saved
     *  id is 0 and the recall is lossy. */
    private int legacyId;
    /** Lazily-allocated meta backing — created on first {@link #getItemMeta()}
     *  call so an unread stack stays meta-free. {@link #setItemMeta} replaces
     *  it; {@link #getItemMeta} returns the same instance on subsequent
     *  reads so a plugin's mutate-without-setItemMeta pattern still sticks
     *  (Essentials's /lore relies on this). */
    private org.bukkit.inventory.meta.ItemMeta meta;

    protected ItemStack() {}
    public ItemStack(org.bukkit.Material arg0) { setType(arg0); this.amount = 1; }
    public ItemStack(org.bukkit.Material arg0, int arg1) { setType(arg0); this.amount = arg1; }
    public ItemStack(org.bukkit.Material arg0, int arg1, short arg2) { setType(arg0); this.amount = arg1; this.durability = arg2; }
    public ItemStack(org.bukkit.Material arg0, int arg1, short arg2, java.lang.Byte arg3) { setType(arg0); this.amount = arg1; this.durability = arg2; }
    public ItemStack(org.bukkit.inventory.ItemStack arg0) throws java.lang.IllegalArgumentException {
        if (arg0 != null) {
            this.material = arg0.material;
            this.legacyId = arg0.legacyId;
            this.amount = arg0.amount;
            this.durability = arg0.durability;
            this.meta = arg0.meta == null ? null : arg0.meta.clone();
        }
    }

    /** Pre-Flattening legacy ctor. WorldEdit 5.6.1's
     *  {@code BukkitPlayer.giveItem} calls {@code new ItemStack(271, 1)}
     *  to mint the wooden axe wand — without this signature {@code //wand}
     *  raises {@link NoSuchMethodError}. {@code typeId} is resolved
     *  through {@link org.bukkit.Material#getMaterial(int)}; ids outside
     *  RDForward's surfaced Material set leave {@code material} null but
     *  preserve the numeric id via {@link #legacyId}. */
    public ItemStack(int typeId, int amount) {
        this.material = org.bukkit.Material.getMaterial(typeId);
        this.legacyId = typeId;
        this.amount = amount;
    }

    /** Same legacy ctor with damage value. */
    public ItemStack(int typeId, int amount, short damage) {
        this.material = org.bukkit.Material.getMaterial(typeId);
        this.legacyId = typeId;
        this.amount = amount;
        this.durability = damage;
    }

    /** Pre-Flattening numeric type id. WorldEdit's wand-detection path
     *  (in {@code WorldEditListener.onPlayerInteract}) checks
     *  {@code item.getTypeId()} against the configured wand id. */
    public int getTypeId() {
        if (material != null) return material.getId();
        return legacyId;
    }

    public void setTypeId(int typeId) {
        this.material = org.bukkit.Material.getMaterial(typeId);
        this.legacyId = typeId;
    }
    public static org.bukkit.inventory.ItemStack of(org.bukkit.Material arg0) {
        return null;
    }
    public static org.bukkit.inventory.ItemStack of(org.bukkit.Material arg0, int arg1) {
        return null;
    }
    public io.papermc.paper.persistence.PersistentDataContainerView getPersistentDataContainer() {
        return null;
    }
    public boolean editPersistentDataContainer(java.util.function.Consumer arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.editPersistentDataContainer(Ljava/util/function/Consumer;)Z");
        return false;
    }
    public org.bukkit.Material getType() {
        return material;
    }
    public void setType(org.bukkit.Material arg0) {
        this.material = arg0;
        this.legacyId = arg0 == null ? 0 : arg0.getId();
    }
    public org.bukkit.inventory.ItemStack withType(org.bukkit.Material arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.withType(Lorg/bukkit/Material;)Lorg/bukkit/inventory/ItemStack;");
        return this;
    }
    public int getAmount() {
        return amount;
    }
    public void setAmount(int arg0) {
        this.amount = arg0;
    }
    public org.bukkit.material.MaterialData getData() {
        return null;
    }
    public void setData(org.bukkit.material.MaterialData arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.setData(Lorg/bukkit/material/MaterialData;)V");
    }
    public void setDurability(short arg0) {
        this.durability = arg0;
    }
    public short getDurability() {
        return durability;
    }
    public int getMaxStackSize() {
        return 0;
    }
    public java.lang.String toString() {
        return null;
    }
    public boolean equals(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.equals(Ljava/lang/Object;)Z");
        return false;
    }
    public boolean isSimilar(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.isSimilar(Lorg/bukkit/inventory/ItemStack;)Z");
        return false;
    }
    public org.bukkit.inventory.ItemStack clone() {
        ItemStack copy = new ItemStack();
        copy.material = this.material;
        copy.legacyId = this.legacyId;
        copy.amount = this.amount;
        copy.durability = this.durability;
        copy.meta = this.meta == null ? null : this.meta.clone();
        return copy;
    }
    public int hashCode() {
        return 0;
    }
    public boolean containsEnchantment(org.bukkit.enchantments.Enchantment arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.containsEnchantment(Lorg/bukkit/enchantments/Enchantment;)Z");
        return false;
    }
    public int getEnchantmentLevel(org.bukkit.enchantments.Enchantment arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.getEnchantmentLevel(Lorg/bukkit/enchantments/Enchantment;)I");
        return 0;
    }
    public java.util.Map getEnchantments() {
        return java.util.Collections.emptyMap();
    }
    public void addEnchantments(java.util.Map arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.addEnchantments(Ljava/util/Map;)V");
    }
    public void addEnchantment(org.bukkit.enchantments.Enchantment arg0, int arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.addEnchantment(Lorg/bukkit/enchantments/Enchantment;I)V");
    }
    public void addUnsafeEnchantments(java.util.Map arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.addUnsafeEnchantments(Ljava/util/Map;)V");
    }
    public void addUnsafeEnchantment(org.bukkit.enchantments.Enchantment arg0, int arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.addUnsafeEnchantment(Lorg/bukkit/enchantments/Enchantment;I)V");
    }
    public int removeEnchantment(org.bukkit.enchantments.Enchantment arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.removeEnchantment(Lorg/bukkit/enchantments/Enchantment;)I");
        return 0;
    }
    public void removeEnchantments() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.removeEnchantments()V");
    }
    public java.util.Map serialize() {
        return java.util.Collections.emptyMap();
    }
    public static org.bukkit.inventory.ItemStack deserialize(java.util.Map arg0) {
        return null;
    }
    public boolean editMeta(java.util.function.Consumer arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.editMeta(Ljava/util/function/Consumer;)Z");
        return false;
    }
    public boolean editMeta(java.lang.Class arg0, java.util.function.Consumer arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.editMeta(Ljava/lang/Class;Ljava/util/function/Consumer;)Z");
        return false;
    }
    public org.bukkit.inventory.meta.ItemMeta getItemMeta() {
        if (this.meta == null) {
            this.meta = createDefaultMeta();
        }
        return this.meta;
    }
    public boolean hasItemMeta() {
        return this.meta != null;
    }
    public boolean setItemMeta(org.bukkit.inventory.meta.ItemMeta arg0) {
        this.meta = arg0;
        return true;
    }

    /** Pick the meta subtype that matches the stack's material so a
     *  {@code (SkullMeta) item.getItemMeta()} cast in plugin code succeeds.
     *  Legacy {@code SKULL_ITEM} (id 397) covers every skull subtype on
     *  pre-flattening; durability 3 narrows it to the player-head variant
     *  Essentials uses, but we return SkullMeta for any durability since
     *  some plugins read meta before setting durability. {@code PLAYER_HEAD}
     *  is the modern flattened id. Other materials fall back to the
     *  generic ItemMeta backing. */
    private org.bukkit.inventory.meta.ItemMeta createDefaultMeta() {
        if (this.material == org.bukkit.Material.SKULL_ITEM
                || this.material == org.bukkit.Material.PLAYER_HEAD) {
            return new com.github.martinambrus.rdforward.bridge.bukkit.meta.BridgeSkullMeta();
        }
        if (this.material == org.bukkit.Material.WRITTEN_BOOK
                || this.material == org.bukkit.Material.WRITABLE_BOOK
                || this.material == org.bukkit.Material.BOOK) {
            return new com.github.martinambrus.rdforward.bridge.bukkit.meta.BridgeBookMeta();
        }
        return new com.github.martinambrus.rdforward.bridge.bukkit.meta.BridgeItemMeta();
    }
    public java.lang.String getTranslationKey() {
        return null;
    }
    public org.bukkit.inventory.ItemStack enchantWithLevels(int arg0, boolean arg1, java.util.Random arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.enchantWithLevels(IZLjava/util/Random;)Lorg/bukkit/inventory/ItemStack;");
        return this;
    }
    public org.bukkit.inventory.ItemStack enchantWithLevels(int arg0, io.papermc.paper.registry.set.RegistryKeySet arg1, java.util.Random arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.enchantWithLevels(ILio/papermc/paper/registry/set/RegistryKeySet;Ljava/util/Random;)Lorg/bukkit/inventory/ItemStack;");
        return this;
    }
    public net.kyori.adventure.text.event.HoverEvent asHoverEvent(java.util.function.UnaryOperator arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.asHoverEvent(Ljava/util/function/UnaryOperator;)Lnet/kyori/adventure/text/event/HoverEvent;");
        return null;
    }
    public net.kyori.adventure.text.Component displayName() {
        return null;
    }
    public net.kyori.adventure.text.Component effectiveName() {
        return null;
    }
    public org.bukkit.inventory.ItemStack ensureServerConversions() {
        return null;
    }
    public static org.bukkit.inventory.ItemStack deserializeBytes(byte[] arg0) {
        return null;
    }
    public byte[] serializeAsBytes() {
        return new byte[0];
    }
    public static byte[] serializeItemsAsBytes(java.util.Collection arg0) {
        return new byte[0];
    }
    public static byte[] serializeItemsAsBytes(org.bukkit.inventory.ItemStack[] arg0) {
        return new byte[0];
    }
    public static org.bukkit.inventory.ItemStack[] deserializeItemsFromBytes(byte[] arg0) {
        return new org.bukkit.inventory.ItemStack[0];
    }
    public java.lang.String getI18NDisplayName() {
        return null;
    }
    public int getMaxItemUseDuration() {
        return 0;
    }
    public int getMaxItemUseDuration(org.bukkit.entity.LivingEntity arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.getMaxItemUseDuration(Lorg/bukkit/entity/LivingEntity;)I");
        return 0;
    }
    public org.bukkit.inventory.ItemStack asOne() {
        return null;
    }
    public org.bukkit.inventory.ItemStack asQuantity(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.asQuantity(I)Lorg/bukkit/inventory/ItemStack;");
        return this;
    }
    public org.bukkit.inventory.ItemStack add() {
        return null;
    }
    public org.bukkit.inventory.ItemStack add(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.add(I)Lorg/bukkit/inventory/ItemStack;");
        return this;
    }
    public org.bukkit.inventory.ItemStack subtract() {
        return null;
    }
    public org.bukkit.inventory.ItemStack subtract(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.subtract(I)Lorg/bukkit/inventory/ItemStack;");
        return this;
    }
    public java.util.List getLore() {
        return java.util.Collections.emptyList();
    }
    public java.util.List lore() {
        return java.util.Collections.emptyList();
    }
    public void setLore(java.util.List arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.setLore(Ljava/util/List;)V");
    }
    public void lore(java.util.List arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.lore(Ljava/util/List;)V");
    }
    public void addItemFlags(org.bukkit.inventory.ItemFlag[] arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.addItemFlags([Lorg/bukkit/inventory/ItemFlag;)V");
    }
    public void removeItemFlags(org.bukkit.inventory.ItemFlag[] arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.removeItemFlags([Lorg/bukkit/inventory/ItemFlag;)V");
    }
    public java.util.Set getItemFlags() {
        return java.util.Collections.emptySet();
    }
    public boolean hasItemFlag(org.bukkit.inventory.ItemFlag arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.hasItemFlag(Lorg/bukkit/inventory/ItemFlag;)Z");
        return false;
    }
    public java.lang.String translationKey() {
        return null;
    }
    public io.papermc.paper.inventory.ItemRarity getRarity() {
        return null;
    }
    public boolean isRepairableBy(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.isRepairableBy(Lorg/bukkit/inventory/ItemStack;)Z");
        return false;
    }
    public boolean canRepair(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.canRepair(Lorg/bukkit/inventory/ItemStack;)Z");
        return false;
    }
    public org.bukkit.inventory.ItemStack damage(int arg0, org.bukkit.entity.LivingEntity arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.damage(ILorg/bukkit/entity/LivingEntity;)Lorg/bukkit/inventory/ItemStack;");
        return this;
    }
    public static org.bukkit.inventory.ItemStack empty() {
        return null;
    }
    public boolean isEmpty() {
        return false;
    }
    public java.util.List computeTooltipLines(io.papermc.paper.inventory.tooltip.TooltipContext arg0, org.bukkit.entity.Player arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.computeTooltipLines(Lio/papermc/paper/inventory/tooltip/TooltipContext;Lorg/bukkit/entity/Player;)Ljava/util/List;");
        return java.util.Collections.emptyList();
    }
    public java.lang.Object getData(io.papermc.paper.datacomponent.DataComponentType$Valued arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.getData(Lio/papermc/paper/datacomponent/DataComponentType$Valued;)Ljava/lang/Object;");
        return null;
    }
    public java.lang.Object getDataOrDefault(io.papermc.paper.datacomponent.DataComponentType$Valued arg0, java.lang.Object arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.getDataOrDefault(Lio/papermc/paper/datacomponent/DataComponentType$Valued;Ljava/lang/Object;)Ljava/lang/Object;");
        return null;
    }
    public boolean hasData(io.papermc.paper.datacomponent.DataComponentType arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.hasData(Lio/papermc/paper/datacomponent/DataComponentType;)Z");
        return false;
    }
    public java.util.Set getDataTypes() {
        return java.util.Collections.emptySet();
    }
    public void setData(io.papermc.paper.datacomponent.DataComponentType$Valued arg0, io.papermc.paper.datacomponent.DataComponentBuilder arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.setData(Lio/papermc/paper/datacomponent/DataComponentType$Valued;Lio/papermc/paper/datacomponent/DataComponentBuilder;)V");
    }
    public void setData(io.papermc.paper.datacomponent.DataComponentType$Valued arg0, java.lang.Object arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.setData(Lio/papermc/paper/datacomponent/DataComponentType$Valued;Ljava/lang/Object;)V");
    }
    public void setData(io.papermc.paper.datacomponent.DataComponentType$NonValued arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.setData(Lio/papermc/paper/datacomponent/DataComponentType$NonValued;)V");
    }
    public void unsetData(io.papermc.paper.datacomponent.DataComponentType arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.unsetData(Lio/papermc/paper/datacomponent/DataComponentType;)V");
    }
    public void resetData(io.papermc.paper.datacomponent.DataComponentType arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.resetData(Lio/papermc/paper/datacomponent/DataComponentType;)V");
    }
    public void copyDataFrom(org.bukkit.inventory.ItemStack arg0, java.util.function.Predicate arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.copyDataFrom(Lorg/bukkit/inventory/ItemStack;Ljava/util/function/Predicate;)V");
    }
    public boolean isDataOverridden(io.papermc.paper.datacomponent.DataComponentType arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.isDataOverridden(Lio/papermc/paper/datacomponent/DataComponentType;)Z");
        return false;
    }
    public boolean matchesWithoutData(org.bukkit.inventory.ItemStack arg0, java.util.Set arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.matchesWithoutData(Lorg/bukkit/inventory/ItemStack;Ljava/util/Set;)Z");
        return false;
    }
    public boolean matchesWithoutData(org.bukkit.inventory.ItemStack arg0, java.util.Set arg1, boolean arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.ItemStack.matchesWithoutData(Lorg/bukkit/inventory/ItemStack;Ljava/util/Set;Z)Z");
        return false;
    }
}
