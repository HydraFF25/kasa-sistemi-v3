package com.cratesystem.crate;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import java.util.List;

/**
 * Bir kasa tanimini (id, gorunum, animasyon, odul havuzu) temsil eder.
 */
public class Crate {

    private final String id;
    private final String displayName;
    private final List<String> description;
    private final CrateAnimationType animationType;
    private final Material keyMaterial;
    private final String keyName;
    private final List<String> keyLore;
    private final NamespacedKey keyItemModel;
    private final boolean keyGlow;
    private final Material previewMaterial;
    private final boolean broadcastRare;
    private final double rareThreshold;
    private final List<CrateReward> rewards;

    public Crate(String id, String displayName, List<String> description, CrateAnimationType animationType,
                 Material keyMaterial, String keyName, List<String> keyLore, NamespacedKey keyItemModel, boolean keyGlow,
                 Material previewMaterial, boolean broadcastRare, double rareThreshold, List<CrateReward> rewards) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.animationType = animationType;
        this.keyMaterial = keyMaterial;
        this.keyName = keyName;
        this.keyLore = keyLore;
        this.keyItemModel = keyItemModel;
        this.keyGlow = keyGlow;
        this.previewMaterial = previewMaterial;
        this.broadcastRare = broadcastRare;
        this.rareThreshold = rareThreshold;
        this.rewards = rewards;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public List<String> getDescription() { return description; }
    public CrateAnimationType getAnimationType() { return animationType; }
    public Material getKeyMaterial() { return keyMaterial; }
    public String getKeyName() { return keyName; }
    public List<String> getKeyLore() { return keyLore; }
    public NamespacedKey getKeyItemModel() { return keyItemModel; }
    public boolean isKeyGlow() { return keyGlow; }
    public Material getPreviewMaterial() { return previewMaterial; }
    public boolean isBroadcastRare() { return broadcastRare; }
    public double getRareThreshold() { return rareThreshold; }
    public List<CrateReward> getRewards() { return rewards; }
}
