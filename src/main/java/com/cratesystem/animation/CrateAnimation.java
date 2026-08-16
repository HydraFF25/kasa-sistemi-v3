package com.cratesystem.animation;

import com.cratesystem.CratePlugin;
import com.cratesystem.crate.Crate;
import com.cratesystem.crate.CrateReward;
import org.bukkit.entity.Player;

/**
 * Bir kasa acilis animasyonunun ortak arayuzu.
 */
public interface CrateAnimation {
    void play(CratePlugin plugin, Player player, Crate crate, CrateReward reward);
}
