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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * CSGO / rulet tarzi kayan-serit acilis animasyonlarinin ortak temeli.
 * Bir "serit" (strip) rastgele odullerle doldurulur, kazanilan odul seride
 * yerlestirilir ve gorunum yavaslayarak (ease-out) o odule kilitlenir.
 *
 * ONEMLI: Oyuncu animasyon bitmeden envanteri kapatirsa (ESC/E vb.), odul
 * KAYBOLMAZ - CrateInventoryHolder + CrateCloseListener sayesinde odul aninda
 * verilir. Buradaki dongu sadece kapanisi algilayip kendini durdurur.
 */
public abstract class AbstractScrollAnimation implements CrateAnimation {

    private static final int SIZE = 9;
    private static final int CENTER = 4;
    private static final int STRIP_LENGTH = 70;
    private static final int TRAVEL = 42;
    private static final int TOTAL_FRAMES = 40;

    protected abstract Material getPointerMaterial();
    protected abstract Sound getTickSound();
    protected abstract Sound getWinSound();

    @Override
    public void play(CratePlugin plugin, Player player, Crate crate, CrateReward reward) {
        CrateInventoryHolder holder = new CrateInventoryHolder(player, crate);
        holder.setReward(reward);
        Inventory inv = Bukkit.createInventory(holder, SIZE, ColorUtils.color("&8» " + crate.getDisplayName()));
        holder.setInventory(inv);

        List<CrateReward> rewards = crate.getRewards();
        int winIndex = STRIP_LENGTH - TRAVEL - 2;
        List<CrateReward> strip = new ArrayList<>(STRIP_LENGTH);
        for (int i = 0; i < STRIP_LENGTH; i++) {
            if (i == winIndex) strip.add(reward);
            else strip.add(rewards.get(ThreadLocalRandom.current().nextInt(rewards.size())));
        }

        inv.setItem(0, new ItemBuilder(getPointerMaterial()).name("&e&l▼ KAZANAN").build());

        player.openInventory(inv);
        render(inv, strip, winIndex - TRAVEL);

        runFrame(plugin, player, inv, holder, strip, 0, TOTAL_FRAMES, winIndex - TRAVEL, winIndex, crate, reward);
    }

    private void runFrame(CratePlugin plugin, Player player, Inventory inv, CrateInventoryHolder holder,
                           List<CrateReward> strip, int frame, int totalFrames, int startIndex, int winIndex,
                           Crate crate, CrateReward reward) {

        // Oyuncu kapattiysa (CrateCloseListener zaten odulu verip holder.cancelled=true yapti),
        // burada sadece dongueyu sessizce durduruyoruz - odul ikinci kez verilmez.
        if (!player.isOnline() || holder.isCancelled()) {
            plugin.getAnimatingPlayers().remove(player.getUniqueId());
            return;
        }

        if (frame > totalFrames) {
            finish(plugin, player, inv, holder, crate, reward);
            return;
        }

        double t = frame / (double) totalFrames;
        double eased = 1 - Math.pow(1 - t, 3);
        int pos = (int) Math.round(startIndex + eased * (winIndex - startIndex));
        render(inv, strip, pos);

        float pitch = 0.7f + (float) t * 1.0f;
        player.playSound(player.getLocation(), getTickSound(), 0.5f, pitch);

        if (frame == totalFrames) {
            player.playSound(player.getLocation(), getWinSound(), 1.0f, 1.0f);
            player.spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1.2, 0), 20, 0.3, 0.3, 0.3, 0.05);
        }

        int delay = 1 + (int) Math.round(t * t * 6);
        int nextFrame = frame + 1;
        Bukkit.getScheduler().runTaskLater(plugin, () ->
                runFrame(plugin, player, inv, holder, strip, nextFrame, totalFrames, startIndex, winIndex, crate, reward), delay);
    }

    private void render(Inventory inv, List<CrateReward> strip, int centerIndex) {
        for (int slot = 1; slot < SIZE; slot++) {
            int offset = slot - CENTER;
            int idx = Math.floorMod(centerIndex + offset, strip.size());
            CrateReward r = strip.get(idx);
            ItemBuilder b = new ItemBuilder(r.getMaterial())
                    .amount(Math.max(1, r.getAmount()))
                    .name(r.getDisplayName())
                    .enchants(r.getEnchants())
                    .unbreakable(r.isUnbreakable())
                    .spawnerEntity(r.getSpawnerEntity());
            if (r.isGlow()) b.glow(true);
            if (slot == CENTER) b.lore(r.getLore());
            inv.setItem(slot, b.build());
        }
    }

    private void finish(CratePlugin plugin, Player player, Inventory inv, CrateInventoryHolder holder,
                         Crate crate, CrateReward reward) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Bu noktaya gelene kadar oyuncu envanteri kapatmis olabilir - o zaman
            // CrateCloseListener odulu zaten vermistir, tekrar vermiyoruz.
            if (!holder.isRewardGiven()) {
                holder.setRewardGiven(true);
                plugin.getCrateManager().giveReward(player, crate, reward);
            }
            if (player.isOnline()) player.closeInventory();
            plugin.getAnimatingPlayers().remove(player.getUniqueId());
        }, 40L);
    }
}
