package com.cratesystem.listener;

import com.cratesystem.CratePlugin;
import com.cratesystem.crate.Crate;
import com.cratesystem.crate.CrateReward;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Map;

/**
 * Fiziksel kasalara (bloklara) sag tik ile etkilesimi yonetir.
 */
public class PlayerInteractListener implements Listener {

    private final CratePlugin plugin;

    public PlayerInteractListener(CratePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        String crateId = plugin.getLocationManager().getCrateId(block.getLocation());
        if (crateId == null) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        if (plugin.getAnimatingPlayers().contains(player.getUniqueId())) {
            plugin.send(player, "animation-in-progress");
            return;
        }

        Crate crate = plugin.getCrateManager().getCrate(crateId);
        if (crate == null) {
            plugin.send(player, "crate-not-found", Map.of("%crate%", crateId));
            return;
        }

        if (!plugin.getKeyManager().hasKey(player, crateId, 1)) {
            plugin.send(player, "no-key", Map.of("%crate%", crate.getDisplayName()));
            return;
        }

        plugin.getKeyManager().takeKey(player, crateId, 1);
        plugin.getAnimatingPlayers().add(player.getUniqueId());

        CrateReward reward = plugin.getCrateManager().rollReward(crate);
        plugin.send(player, "opening-crate", Map.of("%crate%", crate.getDisplayName()));
        plugin.getAnimations().get(crate.getAnimationType()).play(plugin, player, crate, reward);
    }
}
