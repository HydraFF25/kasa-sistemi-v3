package com.cratesystem.listener;

import com.cratesystem.CratePlugin;
import com.cratesystem.crate.Crate;
import com.cratesystem.vote.VoteManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.Map;

/**
 * Oyuncu offlineyken kazandigi (bekleyen) oy odullerini giris yaptiginda verir.
 */
public class PlayerJoinListener implements Listener {

    private final CratePlugin plugin;

    public PlayerJoinListener(CratePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getVoteManager().hasPending(player.getName())) return;

        List<VoteManager.PendingVote> pendingList = plugin.getVoteManager().takePending(player.getName());
        if (pendingList == null || pendingList.isEmpty()) return;

        int totalGiven = 0;
        for (VoteManager.PendingVote pv : pendingList) {
            Crate crate = plugin.getCrateManager().getCrate(pv.crateId());
            if (crate == null) continue;
            plugin.getKeyManager().giveKeys(player, crate, pv.amount());
            totalGiven += pv.amount();
        }

        if (totalGiven > 0) {
            plugin.send(player, "vote-pending-received", Map.of("%amount%", String.valueOf(totalGiven)));
        }
    }
}
