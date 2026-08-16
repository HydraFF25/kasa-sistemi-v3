package com.cratesystem.animation;

import com.cratesystem.CratePlugin;
import com.cratesystem.crate.Crate;
import com.cratesystem.crate.CrateReward;
import com.cratesystem.util.ColorUtils;
import com.cratesystem.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Klasik sandik tarzi animasyon: orta slotta hizli odul yanip sonup, sonunda kazanilan oduldе durur.
 */
public class ChestAnimation implements CrateAnimation {

    private static final int SIZE = 27;
    private static final int CENTER = 13;
    private static final int FLICKER_FRAMES = 34;

    @Override
    public void play(CratePlugin plugin, Player player, Crate crate, CrateReward reward) {
        CrateInventoryHolder holder = new CrateInventoryHolder(player, crate);
        holder.setReward(reward);
        Inventory inv = Bukkit.createInventory(holder, SIZE, ColorUtils.color("&8» " + crate.getDisplayName()));
        holder.setInventory(inv);

        ItemStack border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < SIZE; i++) inv.setItem(i, border);
        inv.setItem(CENTER, new ItemBuilder(Material.PAPER).name("&e&l?").build());

        player.openInventory(inv);
        List<CrateReward> rewards = crate.getRewards();

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                // Oyuncu animasyon bitmeden envanteri kapattiysa (ESC/E vb.), CrateCloseListener
                // odulu zaten verdi ve holder.cancelled=true yapti. Burada sadece dongueyu durduruyoruz.
                if (!player.isOnline() || holder.isCancelled()) {
                    plugin.getAnimatingPlayers().remove(player.getUniqueId());
                    cancel();
                    return;
                }

                ticks++;
                if (ticks < FLICKER_FRAMES) {
                    CrateReward random = rewards.get(ThreadLocalRandom.current().nextInt(rewards.size()));
                    inv.setItem(CENTER, buildIcon(random));
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.0f + (ticks * 0.01f));
                } else if (ticks == FLICKER_FRAMES) {
                    inv.setItem(CENTER, buildIcon(reward));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    player.spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1.2, 0), 30, 0.4, 0.4, 0.4, 0.15);
                } else if (ticks >= FLICKER_FRAMES + 25) {
                    // Odulu kapatmadan ONCE veriyoruz, boylece kapanma olayi tetiklense bile
                    // CrateCloseListener "zaten verildi" gorup ikinci kez vermez.
                    if (!holder.isRewardGiven()) {
                        holder.setRewardGiven(true);
                        plugin.getCrateManager().giveReward(player, crate, reward);
                    }
                    player.closeInventory();
                    plugin.getAnimatingPlayers().remove(player.getUniqueId());
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private ItemStack buildIcon(CrateReward r) {
        ItemBuilder b = new ItemBuilder(r.getMaterial())
                .amount(Math.max(1, r.getAmount()))
                .name(r.getDisplayName())
                .lore(r.getLore())
                .enchants(r.getEnchants())
                .unbreakable(r.isUnbreakable())
                .spawnerEntity(r.getSpawnerEntity());
        if (r.isGlow()) b.glow(true);
        return b.build();
    }
}
