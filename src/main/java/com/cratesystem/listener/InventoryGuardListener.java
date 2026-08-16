package com.cratesystem.listener;

import com.cratesystem.animation.CrateInventoryHolder;
import com.cratesystem.gui.PreviewInventoryHolder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

/**
 * Kasa acilis animasyonu ve onizleme ekrani gosterilirken envanterden
 * esya alinmasini/suruklenmesini engeller (ikisi de sadece "gorsel/bilgi" amaclidir).
 */
public class InventoryGuardListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        var holder = event.getView().getTopInventory().getHolder();
        if (holder instanceof CrateInventoryHolder || holder instanceof PreviewInventoryHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        var holder = event.getView().getTopInventory().getHolder();
        if (holder instanceof CrateInventoryHolder || holder instanceof PreviewInventoryHolder) {
            event.setCancelled(true);
        }
    }
}
