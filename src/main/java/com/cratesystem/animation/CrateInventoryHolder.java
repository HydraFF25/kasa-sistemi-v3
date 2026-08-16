package com.cratesystem.animation;

import com.cratesystem.crate.Crate;
import com.cratesystem.crate.CrateReward;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Kasa acilis animasyonu icin kullanilan envanterleri isaretler.
 * Boylece InventoryGuardListener bu envanterlerdeki tiklama/surukleme islemlerini engelleyebilir.
 *
 * Ayrica, oyuncu animasyon bitmeden envanteri kapatirsa (ESC/E tusu vb.) odulun
 * kaybolmamasi icin gerekli durumu (hangi odul, verildi mi, animasyon iptal mi) tasir.
 */
public class CrateInventoryHolder implements InventoryHolder {

    private Inventory inventory;
    private final Player player;
    private final Crate crate;
    private CrateReward reward;
    private boolean rewardGiven = false;
    private boolean cancelled = false;

    public CrateInventoryHolder(Player player, Crate crate) {
        this.player = player;
        this.crate = crate;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    @NotNull
    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() { return player; }
    public Crate getCrate() { return crate; }

    public CrateReward getReward() { return reward; }
    public void setReward(CrateReward reward) { this.reward = reward; }

    public boolean isRewardGiven() { return rewardGiven; }
    public void setRewardGiven(boolean rewardGiven) { this.rewardGiven = rewardGiven; }

    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
}
