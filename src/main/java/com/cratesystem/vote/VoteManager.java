package com.cratesystem.vote;

import com.cratesystem.CratePlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Oyuncu oy verdiginde ama sunucuda offline oldugunda odulu "beklemeye" alir.
 * Oyuncu tekrar girdiginde PlayerJoinListener bu bekleyen odulleri verir.
 * pending_votes.yml uzerinden kalicidir, sunucu yeniden baslasa bile kaybolmaz.
 */
public class VoteManager {

    public record PendingVote(String crateId, int amount) {}

    private final CratePlugin plugin;
    private final File file;
    private final Map<String, List<PendingVote>> pending = new HashMap<>();

    public VoteManager(CratePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "pending_votes.yml");
    }

    public void load() {
        pending.clear();
        if (!file.exists()) return;

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        for (String username : cfg.getKeys(false)) {
            List<PendingVote> list = new ArrayList<>();
            for (Map<?, ?> raw : cfg.getMapList(username)) {
                String crateId = String.valueOf(raw.get("crate"));
                int amount = (raw.get("amount") instanceof Number n) ? n.intValue() : 1;
                list.add(new PendingVote(crateId, amount));
            }
            if (!list.isEmpty()) pending.put(username.toLowerCase(), list);
        }
    }

    public void save() {
        YamlConfiguration cfg = new YamlConfiguration();
        for (var entry : pending.entrySet()) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (PendingVote pv : entry.getValue()) {
                Map<String, Object> m = new HashMap<>();
                m.put("crate", pv.crateId());
                m.put("amount", pv.amount());
                list.add(m);
            }
            cfg.set(entry.getKey(), list);
        }
        try {
            cfg.save(file);
        } catch (IOException ex) {
            plugin.getLogger().warning("pending_votes.yml kaydedilemedi: " + ex.getMessage());
        }
    }

    public void addPending(String username, String crateId, int amount) {
        pending.computeIfAbsent(username.toLowerCase(), k -> new ArrayList<>()).add(new PendingVote(crateId, amount));
        save();
    }

    public boolean hasPending(String username) {
        List<PendingVote> list = pending.get(username.toLowerCase());
        return list != null && !list.isEmpty();
    }

    public List<PendingVote> takePending(String username) {
        List<PendingVote> list = pending.remove(username.toLowerCase());
        save();
        return list;
    }
}
