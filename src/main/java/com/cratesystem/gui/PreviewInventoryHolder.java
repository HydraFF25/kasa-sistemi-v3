package com.cratesystem.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * /crate preview komutuyla acilan, sadece bilgi amacli (tiklanamaz) envanteri isaretler.
 */
public class PreviewInventoryHolder implements InventoryHolder {

    private Inventory inventory;

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    @NotNull
    public Inventory getInventory() {
        return inventory;
    }
}
