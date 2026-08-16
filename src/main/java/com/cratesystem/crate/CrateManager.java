package com.cratesystem.crate;

import com.cratesystem.CratePlugin;
import com.cratesystem.util.ItemBuilder;
import com.cratesystem.util.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

/**
 * Kasa tanimlarini crates/ klasorunden yukler, odul secimini ve verilmesini yonetir.
 */
public class CrateManager {

    private final CratePlugin plugin;
    private final File cratesFolder;
    private final Map<String, Crate> crates = new LinkedHashMap<>();

    public CrateManager(CratePlugin plugin) {
        this.plugin = plugin;
        this.cratesFolder = new File(plugin.getDataFolder(), "crates");
    }

    public void loadCrates() {
        crates.clear();
        if (!cratesFolder.exists()) {
            cratesFolder.mkdirs();
            plugin.saveResource("crates/example.yml", false);
        }

        File[] files = cratesFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if (files == null) return;

        for (File f : files) {
            try {
                YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
                Crate crate = parseCrate(cfg, f.getName().replace(".yml", ""));
                if (crate != null) crates.put(crate.getId().toLowerCase(), crate);
            } catch (Exception ex) {
                plugin.getLogger().log(Level.WARNING, f.getName() + " kasa dosyasi yuklenemedi: " + ex.getMessage(), ex);
            }
        }
        plugin.getLogger().info(crates.size() + " kasa yuklendi.");
    }

    private Crate parseCrate(YamlConfiguration cfg, String fileId) {
        String id = cfg.getString("id", fileId);
        String displayName = cfg.getString("name", "&6" + id);
        List<String> description = cfg.getStringList("description");

        CrateAnimationType type;
        try {
            type = CrateAnimationType.valueOf(cfg.getString("animation", "CHEST").toUpperCase());
        } catch (IllegalArgumentException ex) {
            type = CrateAnimationType.CHEST;
        }

        Material keyMaterial = parseMaterial(cfg.getString("key-item", "TRIPWIRE_HOOK"), Material.TRIPWIRE_HOOK);
        String keyName = cfg.getString("key-name", "&6" + displayName + " &6Anahtari");
        List<String> keyLore = cfg.getStringList("key-lore");
        NamespacedKey keyItemModel = parseNamespacedKey(cfg.getString("key-item-model"));
        boolean keyGlow = cfg.getBoolean("key-glow", false);
        Material previewMaterial = parseMaterial(cfg.getString("preview-item", "CHEST"), Material.CHEST);
        boolean broadcastRare = cfg.getBoolean("broadcast-rare", false);
        double rareThreshold = cfg.getDouble("rare-chance-threshold", 5.0);

        List<CrateReward> rewards = new ArrayList<>();
        List<Map<?, ?>> itemsList = cfg.getMapList("items");
        for (Map<?, ?> raw : itemsList) {
            ConfigurationSection dummy = memorySection(raw);
            CrateReward reward = parseReward(dummy);
            if (reward != null) rewards.add(reward);
        }

        if (rewards.isEmpty()) {
            plugin.getLogger().warning(id + " kasasinda hic odul tanimli degil, kasa atlandi.");
            return null;
        }

        return new Crate(id, displayName, description, type, keyMaterial, keyName, keyLore, keyItemModel, keyGlow,
                previewMaterial, broadcastRare, rareThreshold, rewards);
    }

    /**
     * "cratesystem:vote_key" gibi bir metni NamespacedKey'e cevirir.
     * Bos/hatali ise null doner (esya normal materyal dokusuyla gorunmeye devam eder).
     */
    private NamespacedKey parseNamespacedKey(String value) {
        if (value == null || value.isBlank()) return null;
        NamespacedKey key = NamespacedKey.fromString(value.trim().toLowerCase());
        if (key == null) {
            plugin.getLogger().warning("Gecersiz key-item-model: '" + value + "' (ornek: cratesystem:vote_key)");
        }
        return key;
    }

    private ConfigurationSection memorySection(Map<?, ?> raw) {
        YamlConfiguration temp = new YamlConfiguration();
        for (var e : raw.entrySet()) temp.set(String.valueOf(e.getKey()), e.getValue());
        return temp;
    }

    private CrateReward parseReward(ConfigurationSection sec) {
        String matName = sec.getString("material", "STONE");
        Material material = parseMaterial(matName, Material.STONE);
        int amount = sec.getInt("amount", 1);
        String name = sec.getString("name", material.name());
        List<String> lore = sec.getStringList("lore");
        double chance = sec.getDouble("chance", 1.0);
        boolean glow = sec.getBoolean("glow", false);
        boolean displayOnly = sec.getBoolean("display-only", false);
        boolean unbreakable = sec.getBoolean("unbreakable", false);
        List<String> commands = sec.getStringList("commands");
        String id = sec.getString("id", material.name().toLowerCase());
        Map<Enchantment, Integer> enchants = parseEnchants(sec.getStringList("enchants"));
        EntityType spawnerEntity = parseSpawnerEntity(sec.getString("spawner-entity"));
        return new CrateReward(id, material, amount, name, lore, chance, glow, commands, displayOnly,
                enchants, unbreakable, spawnerEntity);
    }

    /**
     * "ZOMBIE" gibi bir metni EntityType'a cevirir. SPAWNER materyalli oduller icin kullanilir,
     * boylece verilen spawner itemi icinde hangi mobun cikacagi dogrudan gomulu olur
     * (komut calistirmaya gerek kalmadan calisir).
     */
    private EntityType parseSpawnerEntity(String name) {
        if (name == null || name.isBlank()) return null;
        try {
            return EntityType.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("Gecersiz spawner-entity: '" + name + "'");
            return null;
        }
    }

    /**
     * "SHARPNESS:6" gibi metinleri Enchantment + seviye ciftlerine cevirir.
     * Vanilla seviye siniri uygulanmaz, yani 6, 7 gibi ozel seviyeler desteklenir.
     */
    private Map<Enchantment, Integer> parseEnchants(List<String> list) {
        Map<Enchantment, Integer> map = new LinkedHashMap<>();
        if (list == null) return map;

        for (String entry : list) {
            String[] parts = entry.split(":");
            if (parts.length != 2) {
                plugin.getLogger().warning("Gecersiz enchant formati: '" + entry + "' (ornek: SHARPNESS:6)");
                continue;
            }

            NamespacedKey key = NamespacedKey.minecraft(parts[0].trim().toLowerCase());
            Enchantment enchant = Registry.ENCHANTMENT.get(key);
            if (enchant == null) {
                plugin.getLogger().warning("Bilinmeyen enchant: '" + parts[0] + "'");
                continue;
            }

            try {
                int level = Integer.parseInt(parts[1].trim());
                map.put(enchant, level);
            } catch (NumberFormatException ex) {
                plugin.getLogger().warning("Gecersiz enchant seviyesi: '" + entry + "'");
            }
        }
        return map;
    }

    private Material parseMaterial(String name, Material fallback) {
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (Exception ex) {
            plugin.getLogger().warning("Gecersiz materyal: " + name + " -> " + fallback + " kullanilacak.");
            return fallback;
        }
    }

    public Crate getCrate(String id) {
        if (id == null) return null;
        return crates.get(id.toLowerCase());
    }

    public Map<String, Crate> getCrates() {
        return crates;
    }

    public CrateReward rollReward(Crate crate) {
        List<CrateReward> rewards = crate.getRewards();
        double total = rewards.stream().mapToDouble(CrateReward::getChance).sum();
        if (total <= 0) return rewards.get(ThreadLocalRandom.current().nextInt(rewards.size()));

        double roll = ThreadLocalRandom.current().nextDouble() * total;
        double cumulative = 0;
        for (CrateReward r : rewards) {
            cumulative += r.getChance();
            if (roll <= cumulative) return r;
        }
        return rewards.get(rewards.size() - 1);
    }

    public void giveReward(Player player, Crate crate, CrateReward reward) {
        if (!reward.isDisplayOnly()) {
            ItemBuilder builder = new ItemBuilder(reward.getMaterial())
                    .amount(reward.getAmount())
                    .name(reward.getDisplayName())
                    .lore(reward.getLore())
                    .glow(reward.isGlow())
                    .enchants(reward.getEnchants())
                    .unbreakable(reward.isUnbreakable())
                    .spawnerEntity(reward.getSpawnerEntity());

            var leftover = player.getInventory().addItem(builder.build());
            for (var extra : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), extra);
            }
        }

        for (String cmd : reward.getCommands()) {
            String parsed = cmd.replace("%player%", player.getName()).replace("%crate%", crate.getId());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
        }

        plugin.send(player, "crate-opened", Map.of(
                "%crate%", crate.getDisplayName(),
                "%reward%", reward.getDisplayName()
        ));

        if (crate.isBroadcastRare() && reward.getChance() <= crate.getRareThreshold()) {
            String raw = plugin.raw("broadcast-rare")
                    .replace("%player%", player.getName())
                    .replace("%crate%", crate.getDisplayName())
                    .replace("%reward%", reward.getDisplayName());
            Bukkit.broadcast(ColorUtils.color(raw));
        }
    }
}
