package com.cratesystem.animation;

import org.bukkit.Material;
import org.bukkit.Sound;

/**
 * CSGO tarzi yatay kayan serit animasyonu.
 */
public class CSGOAnimation extends AbstractScrollAnimation {
    @Override protected Material getPointerMaterial() { return Material.YELLOW_STAINED_GLASS_PANE; }
    @Override protected Sound getTickSound() { return Sound.UI_BUTTON_CLICK; }
    @Override protected Sound getWinSound() { return Sound.ENTITY_PLAYER_LEVELUP; }
}
