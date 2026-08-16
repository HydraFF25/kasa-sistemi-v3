package com.cratesystem.animation;

import org.bukkit.Material;
import org.bukkit.Sound;

/**
 * Rulet temali kayan serit animasyonu (farkli ses/gorsel tema).
 */
public class RouletteAnimation extends AbstractScrollAnimation {
    @Override protected Material getPointerMaterial() { return Material.REDSTONE_TORCH; }
    @Override protected Sound getTickSound() { return Sound.BLOCK_NOTE_BLOCK_HAT; }
    @Override protected Sound getWinSound() { return Sound.ENTITY_EXPERIENCE_ORB_PICKUP; }
}
