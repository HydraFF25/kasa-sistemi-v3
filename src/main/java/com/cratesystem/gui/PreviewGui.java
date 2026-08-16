package com.cratesystem.gui;

import com.cratesystem.crate.Crate;
import com.cratesystem.crate.CrateReward;
import com.cratesystem.util.ColorUtils;
import com.cratesystem.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * /crate preview <kasa> ile acilan, kasayi hic acmadan icindeki tum odulleri
 * ve gercek kazanma yuzdelerini gosteren bilgi ekrani.
 */
public class PreviewGui {

    public void open(Player player, Crate crate) {
        List<CrateReward> rewards = crate.getRewards();
        if (rewards.isEmpty()) return;

        int size = ((rewards.size() - 1) / 9 + 1) * 9;
        size = Math.max(9, Math.min(size, 54));

        PreviewInventoryHolder holder = new PreviewInventoryHolder();
        Inventory inv = Bukkit.createInventory(holder, size, ColorUtils.color("&8Onizleme » " + crate.getDisplayName()));
        holder.setInventory(inv);

        double total = rewards.stream().mapToDouble(CrateReward::getChance).sum();

        for (int i = 0; i < rewards.size() && i < size; i++) {
            CrateReward r = rewards.get(i);
            double percent = (total > 0) ? (r.getChance() / total) * 100.0 : 0.0;

            List<String> lore = new ArrayList<>();
            if (r.getLore() != null) lore.addAll(r.getLore());
            lore.add(" ");
            lore.add("&7Kazanma sansi: " + String.format(Locale.US, "&e%.2f%%", percent));

            ItemBuilder builder = new ItemBuilder(r.getMaterial())
                    .amount(Math.max(1, r.getAmount()))
                    .name(r.getDisplayName())
                    .lore(lore)
                    .enchants(r.getEnchants())
                    .unbreakable(r.isUnbreakable())
                    .spawnerEntity(r.getSpawnerEntity());
            if (r.isGlow()) builder.glow(true);

            inv.setItem(i, builder.build());
        }

        player.openInventory(inv);
    }
}
