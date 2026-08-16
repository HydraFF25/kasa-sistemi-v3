package com.cratesystem.listener;

import com.cratesystem.CratePlugin;
import com.cratesystem.crate.Crate;
import com.cratesystem.util.ColorUtils;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;

/**
 * NuVotifier (oy siteleri) uzerinden gelen oylari dinler ve config.yml'deki
 * vote-crate ayarlarina gore otomatik anahtar verir.
 *
 * Bu sinif sadece NuVotifier/Votifier sunucuda yukluyse kayit edilir
 * (CratePlugin.onEnable icinde kontrol edilir), aksi halde VotifierEvent
 * sinifi bulunamayacagi icin hic tetiklenmez.
 */
public class VotifierListener implements Listener {

    private final CratePlugin plugin;

    public VotifierListener(CratePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVote(VotifierEvent event) {
        if (!plugin.getConfig().getBoolean("vote-crate.enabled", true)) return;

        Vote vote = event.getVote();
        String username = vote.getUsername();

        String crateId = plugin.getConfig().getString("vote-crate.crate", "vote");
        int amount = Math.max(1, plugin.getConfig().getInt("vote-crate.amount-per-vote", 1));

        Crate crate = plugin.getCrateManager().getCrate(crateId);
        if (crate == null) {
            plugin.getLogger().warning("Oy odulu verilemedi: '" + crateId + "' isimli kasa bulunamadi. " +
                    "config.yml -> vote-crate.crate ayarini kontrol et.");
            return;
        }

        Player player = Bukkit.getPlayerExact(username);
        if (player != null && player.isOnline()) {
            plugin.getKeyManager().giveKeys(player, crate, amount);
            plugin.send(player, "vote-thanks", Map.of(
                    "%amount%", String.valueOf(amount),
                    "%crate%", crate.getDisplayName(),
                    "%service%", vote.getServiceName()
            ));
        } else {
            // Oyuncu offline, bir sonraki girisinde verilmek uzere beklemeye al.
            plugin.getVoteManager().addPending(username, crateId, amount);
        }

        if (plugin.getConfig().getBoolean("vote-crate.broadcast", true)) {
            String raw = plugin.raw("vote-broadcast")
                    .replace("%player%", username)
                    .replace("%service%", vote.getServiceName());
            Bukkit.broadcast(ColorUtils.color(raw));
        }
    }
}
