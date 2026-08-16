package com.cratesystem.crate;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.Map;

/**
 * Bir kasadan cikabilecek tek bir odulu temsil eder.
 */
public class CrateReward {

    private final String id;
    private final Material material;
    private final int amount;
    private final String displayName;
    private final List<String> lore;
    private final double chance;
    private final boolean glow;
    private final List<String> commands;
    private final boolean displayOnly;
    private final Map<Enchantment, Integer> enchants;
    private final boolean unbreakable;
    private final EntityType spawnerEntity;

    public CrateReward(String id, Material material, int amount, String displayName, List<String> lore,
                        double chance, boolean glow, List<String> commands, boolean displayOnly,
                        Map<Enchantment, Integer> enchants, boolean unbreakable, EntityType spawnerEntity) {
        this.id = id;
        this.material = material;
        this.amount = amount;
        this.displayName = displayName;
        this.lore = lore;
        this.chance = chance;
        this.glow = glow;
        this.commands = commands;
        this.displayOnly = displayOnly;
        this.enchants = enchants;
        this.unbreakable = unbreakable;
        this.spawnerEntity = spawnerEntity;
    }

    public String getId() { return id; }
    public Material getMaterial() { return material; }
    public int getAmount() { return amount; }
    public String getDisplayName() { return displayName; }
    public List<String> getLore() { return lore; }
    public double getChance() { return chance; }
    public boolean isGlow() { return glow; }
    public List<String> getCommands() { return commands; }
    public boolean isDisplayOnly() { return displayOnly; }
    public Map<Enchantment, Integer> getEnchants() { return enchants; }
    public boolean isUnbreakable() { return unbreakable; }
    public EntityType getSpawnerEntity() { return spawnerEntity; }
}
