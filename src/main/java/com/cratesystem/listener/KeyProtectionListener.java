package com.cratesystem.listener;

import com.cratesystem.CratePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Kasa anahtarlarinin blok olarak yere konulmasini engeller
 * (anahtar itemleri TRIPWIRE_HOOK gibi yerlestirilebilir bir materyal kullanabilir).
 */
public class KeyProtectionListener implements Listener {

    private final CratePlugin plugin;

    public KeyProtectionListener(CratePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (plugin.getKeyManager().isKey(event.getItemInHand())) {
            event.setCancelled(true);
        }
    }
}
