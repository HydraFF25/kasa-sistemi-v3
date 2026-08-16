package com.cratesystem.hologram;

import com.cratesystem.CratePlugin;
import com.cratesystem.crate.Crate;
import com.cratesystem.util.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

/**
 * Fiziksel kasalarin ustunde iki gorsel unsuru yonetir:
 * - Kasa ismini gosteren bir isim etiketi (ArmorStand tabanli hologram)
 * - Yavasca donen, kasanin onizleme esyasini gosteren bir 3D item gorseli (ItemDisplay)
 * Ayrica kasa etrafinda hafif bir parcacik efekti calisir.
 * Hepsi harici bir eklenti gerekmeden, Paper'in kendi Display entity API'siyle calisir.
 */
public class HologramManager {

    private final CratePlugin plugin;
    private final NamespacedKey markerKey;
    private final Map<String, ArmorStand> nameStands = new HashMap<>();
    private final Map<String, ItemDisplay> itemDisplays = new HashMap<>();

    private BukkitTask visualTask;
    private float spinAngle = 0f;
    private int particleTickCounter = 0;

    public HologramManager(CratePlugin plugin) {
        this.plugin = plugin;
        this.markerKey = new NamespacedKey(plugin, "crate_hologram");
    }

    /**
     * Tum gorselleri siler ve kayitli kasa konumlarina gore yeniden olusturur.
     * /crate reload, /crate setlocation, /crate removelocation ve sunucu acilisinda cagrilir.
     */
    public void reloadAll() {
        removeAll();
        cleanupOrphans();

        for (var entry : plugin.getLocationManager().getAllLocations().entrySet()) {
            Location loc = entry.getKey();
            Crate crate = plugin.getCrateManager().getCrate(entry.getValue());
            String name = (crate != null) ? crate.getDisplayName() : ("&c" + entry.getValue());

            spawnNameStand(loc, name);
            if (crate != null) {
                spawnItemDisplay(loc, crate);
            }
        }

        startVisualTask();
    }

    private void spawnNameStand(Location blockLoc, String displayName) {
        Location holoLoc = blockLoc.clone().add(0.5, 1.6, 0.5);

        ArmorStand stand = holoLoc.getWorld().spawn(holoLoc, ArmorStand.class, as -> {
            as.setVisible(false);
            as.setMarker(true);
            as.setGravity(false);
            as.setInvulnerable(true);
            as.setSmall(true);
            as.setBasePlate(false);
            as.setCanTick(false);
            as.setCustomNameVisible(true);
            as.customName(ColorUtils.color(displayName));
            as.setPersistent(true);
            as.getPersistentDataContainer().set(markerKey, PersistentDataType.BYTE, (byte) 1);
        });

        nameStands.put(key(blockLoc), stand);
    }

    private void spawnItemDisplay(Location blockLoc, Crate crate) {
        Location itemLoc = blockLoc.clone().add(0.5, 1.15, 0.5);
        ItemStack icon = new ItemStack(crate.getPreviewMaterial());

        ItemDisplay display = itemLoc.getWorld().spawn(itemLoc, ItemDisplay.class, d -> {
            d.setItemStack(icon);
            d.setBillboard(Display.Billboard.FIXED);
            d.setPersistent(true);
            d.getPersistentDataContainer().set(markerKey, PersistentDataType.BYTE, (byte) 1);
        });

        itemDisplays.put(key(blockLoc), display);
    }

    /**
     * Tum item gorsellerini yavasca dondurur ve periyodik olarak hafif bir
     * parcacik efekti yayar. Tek bir zamanlanmis gorev tum kasalari kapsar,
     * boylece kasa sayisi arttikca performans sorunlari yaratmaz.
     */
    private void startVisualTask() {
        if (visualTask != null) {
            visualTask.cancel();
            visualTask = null;
        }
        if (itemDisplays.isEmpty()) return;

        visualTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            spinAngle += 0.06f;
            if (spinAngle > Math.PI * 2) {
                spinAngle -= (float) (Math.PI * 2);
            }

            Transformation transform = new Transformation(
                    new Vector3f(0f, 0f, 0f),
                    new Quaternionf().rotateY(spinAngle),
                    new Vector3f(0.9f, 0.9f, 0.9f),
                    new Quaternionf()
            );

            for (ItemDisplay display : itemDisplays.values()) {
                if (display != null && !display.isDead()) {
                    display.setTransformation(transform);
                }
            }

            particleTickCounter++;
            if (particleTickCounter >= 10) {
                particleTickCounter = 0;
                for (ItemDisplay display : itemDisplays.values()) {
                    if (display == null || display.isDead()) continue;
                    Location loc = display.getLocation();
                    if (loc.getWorld() == null) continue;
                    loc.getWorld().spawnParticle(Particle.END_ROD, loc, 2, 0.2, 0.15, 0.2, 0.01);
                }
            }
        }, 0L, 2L);
    }

    private String key(Location loc) {
        return loc.getWorld().getName() + ";" + loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ();
    }

    public void removeAll() {
        for (ArmorStand stand : nameStands.values()) {
            if (stand != null && !stand.isDead()) stand.remove();
        }
        nameStands.clear();

        for (ItemDisplay display : itemDisplays.values()) {
            if (display != null && !display.isDead()) display.remove();
        }
        itemDisplays.clear();

        if (visualTask != null) {
            visualTask.cancel();
            visualTask = null;
        }
    }

    /**
     * Onceki oturumdan (ornegin sunucu crash sonrasi duzgun kapanmadiysa) kalmis
     * olabilecek isim etiketi/item gorsellerini, ozel PDC etiketine bakarak temizler.
     * Boylece sunucu her acildiginda ust uste binen gorsel birikmesi engellenir.
     */
    private void cleanupOrphans() {
        for (var world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof ArmorStand) && !(entity instanceof ItemDisplay)) continue;
                if (entity.getPersistentDataContainer().has(markerKey, PersistentDataType.BYTE)) {
                    entity.remove();
                }
            }
        }
    }
}
