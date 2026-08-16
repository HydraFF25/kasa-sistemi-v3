package com.cratesystem.listener;

import com.cratesystem.CratePlugin;
import com.cratesystem.animation.CrateInventoryHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * Oyuncu kasa acilis animasyonunu (ESC, E tusu, baska bir envanter acma vb. ile)
 * animasyon bitmeden kapatirsa, kazandigi odulun kaybolmamasi icin aninda verir.
 * Animasyon dogal olarak biterse zaten odul kendi akisinda verilir; bu durumda
 * "rewardGiven" bayragi sayesinde odul iki kere verilmez.
 */
public class CrateCloseListener implements Listener {

    private final CratePlugin plugin;

    public CrateCloseListener(CratePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof CrateInventoryHolder holder)) return;

        holder.setCancelled(true);

        if (!(event.getPlayer() instanceof Player player)) return;

        if (!holder.isRewardGiven() && holder.getReward() != null) {
            holder.setRewardGiven(true);
            plugin.getCrateManager().giveReward(player, holder.getCrate(), holder.getReward());
        }

        plugin.getAnimatingPlayers().remove(player.getUniqueId());
    }
}
