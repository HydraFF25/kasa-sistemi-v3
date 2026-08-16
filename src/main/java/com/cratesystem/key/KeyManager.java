package com.cratesystem.key;

import com.cratesystem.CratePlugin;
import com.cratesystem.crate.Crate;
import com.cratesystem.util.ItemBuilder;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;

/**
 * Fiziksel anahtar itemlerinin uretimi, verilmesi ve tuketilmesini yonetir.
 * Anahtarlar PersistentDataContainer icinde hangi kasaya ait olduklarini tasir.
 */
public class KeyManager {

    private final NamespacedKey crateIdKey;

    public KeyManager(CratePlugin plugin) {
        this.crateIdKey = new NamespacedKey(plugin, "crate_key_id");
    }

    public ItemStack buildKeyItem(Crate crate) {
        return new ItemBuilder(crate.getKeyMaterial())
                .name(crate.getKeyName())
                .lore(crate.getKeyLore())
                .itemModel(crate.getKeyItemModel())
                .glow(crate.isKeyGlow())
                .pdc(crateIdKey, PersistentDataType.STRING, crate.getId())
                .build();
    }

    public void giveKeys(Player player, Crate crate, int amount) {
        ItemStack template = buildKeyItem(crate);
        int remaining = amount;
        while (remaining > 0) {
            int stackAmt = Math.min(remaining, template.getMaxStackSize());
            ItemStack stack = template.clone();
            stack.setAmount(stackAmt);
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(stack);
            for (ItemStack extra : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), extra);
            }
            remaining -= stackAmt;
        }
    }

    public boolean isKey(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(crateIdKey, PersistentDataType.STRING);
    }

    public String getKeyCrateId(ItemStack item) {
        if (!isKey(item)) return null;
        return item.getItemMeta().getPersistentDataContainer().get(crateIdKey, PersistentDataType.STRING);
    }

    public int countKeys(Player player, String crateId) {
        int total = 0;
        PlayerInventory inv = player.getInventory();
        for (ItemStack it : inv.getStorageContents()) {
            if (it == null) continue;
            if (isKey(it) && crateId.equalsIgnoreCase(getKeyCrateId(it))) total += it.getAmount();
        }
        return total;
    }

    public boolean hasKey(Player player, String crateId, int amount) {
        return countKeys(player, crateId) >= amount;
    }

    public boolean takeKey(Player player, String crateId, int amount) {
        int remaining = amount;
        PlayerInventory inv = player.getInventory();
        ItemStack[] contents = inv.getStorageContents();

        for (int i = 0; i < contents.length; i++) {
            ItemStack it = contents[i];
            if (it == null) continue;
            if (!isKey(it) || !crateId.equalsIgnoreCase(getKeyCrateId(it))) continue;

            int amt = it.getAmount();
            if (amt > remaining) {
                it.setAmount(amt - remaining);
                remaining = 0;
            } else {
                remaining -= amt;
                contents[i] = null;
            }
            if (remaining <= 0) break;
        }

        inv.setStorageContents(contents);
        return remaining <= 0;
    }
}
