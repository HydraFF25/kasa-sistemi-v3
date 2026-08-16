package com.cratesystem.command;

import com.cratesystem.CratePlugin;
import com.cratesystem.crate.Crate;
import com.cratesystem.crate.CrateReward;
import com.cratesystem.util.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * /crate ana komutu ve tum alt komutlari.
 */
public class CrateCommand implements CommandExecutor, TabCompleter {

    private final CratePlugin plugin;

    public CrateCommand(CratePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help" -> sendHelp(sender);
            case "reload" -> handleReload(sender);
            case "list" -> handleList(sender);
            case "give" -> handleGive(sender, args);
            case "setlocation" -> handleSetLocation(sender, args);
            case "removelocation" -> handleRemoveLocation(sender);
            case "open" -> handleOpen(sender, args);
            case "forceopen" -> handleForceOpen(sender, args);
            case "preview" -> handlePreview(sender, args);
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtils.color("&8&m----------&r &6&lKasa Sistemi &8&m----------"));
        sender.sendMessage(ColorUtils.color("&e/crate list &7- Kasalari listele"));
        sender.sendMessage(ColorUtils.color("&e/crate open <kasa> &7- Elindeki anahtar ile kasa ac"));
        sender.sendMessage(ColorUtils.color("&e/crate preview <kasa> &7- Kasayi acmadan odulleri gor"));
        if (sender.hasPermission("cratesystem.admin")) {
            sender.sendMessage(ColorUtils.color("&e/crate reload &7- Konfigurasyonu yeniden yukle"));
            sender.sendMessage(ColorUtils.color("&e/crate give <oyuncu> <kasa> <miktar> &7- Anahtar ver"));
            sender.sendMessage(ColorUtils.color("&e/crate setlocation <kasa> &7- Baktigin blogu kasa yap"));
            sender.sendMessage(ColorUtils.color("&e/crate removelocation &7- Baktigin bloktaki kasayi kaldir"));
            sender.sendMessage(ColorUtils.color("&e/crate forceopen <oyuncu> <kasa> &7- Zorla kasa ac"));
        }
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("cratesystem.admin")) { plugin.send(sender, "no-permission"); return; }
        plugin.reloadAll();
        plugin.send(sender, "crate-reloaded");
    }

    private void handleList(CommandSender sender) {
        if (plugin.getCrateManager().getCrates().isEmpty()) {
            sender.sendMessage(ColorUtils.color("&cHenuz kasa tanimlanmamis."));
            return;
        }
        String list = plugin.getCrateManager().getCrates().values().stream()
                .map(Crate::getId).collect(Collectors.joining("&7, &e"));
        sender.sendMessage(ColorUtils.color("&6Kasalar: &e" + list));
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("cratesystem.admin")) { plugin.send(sender, "no-permission"); return; }
        if (args.length < 4) { sender.sendMessage(ColorUtils.color("&cKullanim: /crate give <oyuncu> <kasa> <miktar>")); return; }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { sender.sendMessage(ColorUtils.color("&cOyuncu bulunamadi.")); return; }

        Crate crate = plugin.getCrateManager().getCrate(args[2]);
        if (crate == null) { plugin.send(sender, "crate-not-found", Map.of("%crate%", args[2])); return; }

        int amount;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException ex) {
            sender.sendMessage(ColorUtils.color("&cGecersiz miktar."));
            return;
        }

        plugin.getKeyManager().giveKeys(target, crate, amount);
        plugin.send(sender, "key-given", Map.of("%player%", target.getName(), "%amount%", String.valueOf(amount), "%crate%", crate.getDisplayName()));
        plugin.send(target, "key-received", Map.of("%amount%", String.valueOf(amount), "%crate%", crate.getDisplayName()));
    }

    private void handleSetLocation(CommandSender sender, String[] args) {
        if (!sender.hasPermission("cratesystem.admin")) { plugin.send(sender, "no-permission"); return; }
        if (!(sender instanceof Player player)) { plugin.send(sender, "player-only"); return; }
        if (args.length < 2) { sender.sendMessage(ColorUtils.color("&cKullanim: /crate setlocation <kasa>")); return; }

        Crate crate = plugin.getCrateManager().getCrate(args[1]);
        if (crate == null) { plugin.send(sender, "crate-not-found", Map.of("%crate%", args[1])); return; }

        Block target = player.getTargetBlockExact(6);
        if (target == null || target.getType().isAir()) {
            sender.sendMessage(ColorUtils.color("&cBir bloga bakmalisin."));
            return;
        }

        plugin.getLocationManager().addLocation(target.getLocation(), crate.getId());
        plugin.getHologramManager().reloadAll();
        plugin.send(sender, "location-set", Map.of("%crate%", crate.getDisplayName()));
    }

    private void handleRemoveLocation(CommandSender sender) {
        if (!sender.hasPermission("cratesystem.admin")) { plugin.send(sender, "no-permission"); return; }
        if (!(sender instanceof Player player)) { plugin.send(sender, "player-only"); return; }

        Block target = player.getTargetBlockExact(6);
        if (target == null) {
            sender.sendMessage(ColorUtils.color("&cBir bloga bakmalisin."));
            return;
        }

        plugin.getLocationManager().removeLocation(target.getLocation());
        plugin.getHologramManager().reloadAll();
        plugin.send(sender, "location-removed");
    }

    private void handlePreview(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) { plugin.send(sender, "player-only"); return; }
        if (args.length < 2) { sender.sendMessage(ColorUtils.color("&cKullanim: /crate preview <kasa>")); return; }

        Crate crate = plugin.getCrateManager().getCrate(args[1]);
        if (crate == null) { plugin.send(sender, "crate-not-found", Map.of("%crate%", args[1])); return; }

        plugin.getPreviewGui().open(player, crate);
    }

    private void handleOpen(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) { plugin.send(sender, "player-only"); return; }
        if (args.length < 2) { sender.sendMessage(ColorUtils.color("&cKullanim: /crate open <kasa>")); return; }

        Crate crate = plugin.getCrateManager().getCrate(args[1]);
        if (crate == null) { plugin.send(sender, "crate-not-found", Map.of("%crate%", args[1])); return; }

        if (plugin.getAnimatingPlayers().contains(player.getUniqueId())) { plugin.send(player, "animation-in-progress"); return; }
        if (!plugin.getKeyManager().hasKey(player, crate.getId(), 1)) { plugin.send(player, "no-key", Map.of("%crate%", crate.getDisplayName())); return; }

        plugin.getKeyManager().takeKey(player, crate.getId(), 1);
        plugin.getAnimatingPlayers().add(player.getUniqueId());
        CrateReward reward = plugin.getCrateManager().rollReward(crate);
        plugin.send(player, "opening-crate", Map.of("%crate%", crate.getDisplayName()));
        plugin.getAnimations().get(crate.getAnimationType()).play(plugin, player, crate, reward);
    }

    private void handleForceOpen(CommandSender sender, String[] args) {
        if (!sender.hasPermission("cratesystem.admin")) { plugin.send(sender, "no-permission"); return; }
        if (args.length < 3) { sender.sendMessage(ColorUtils.color("&cKullanim: /crate forceopen <oyuncu> <kasa>")); return; }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { sender.sendMessage(ColorUtils.color("&cOyuncu bulunamadi.")); return; }

        Crate crate = plugin.getCrateManager().getCrate(args[2]);
        if (crate == null) { plugin.send(sender, "crate-not-found", Map.of("%crate%", args[2])); return; }

        if (plugin.getAnimatingPlayers().contains(target.getUniqueId())) {
            sender.sendMessage(ColorUtils.color("&cOyuncu zaten bir kasa aciyor."));
            return;
        }

        plugin.getAnimatingPlayers().add(target.getUniqueId());
        CrateReward reward = plugin.getCrateManager().rollReward(crate);
        plugin.send(target, "opening-crate", Map.of("%crate%", crate.getDisplayName()));
        plugin.getAnimations().get(crate.getAnimationType()).play(plugin, target, crate, reward);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            options.addAll(List.of("help", "list", "open", "preview"));
            if (sender.hasPermission("cratesystem.admin")) {
                options.addAll(List.of("reload", "give", "setlocation", "removelocation", "forceopen"));
            }
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "give", "forceopen" -> Bukkit.getOnlinePlayers().forEach(p -> options.add(p.getName()));
                case "open", "setlocation", "preview" -> options.addAll(plugin.getCrateManager().getCrates().keySet());
            }
        } else if (args.length == 3 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("forceopen"))) {
            options.addAll(plugin.getCrateManager().getCrates().keySet());
        }
        String cur = args[args.length - 1].toLowerCase();
        return options.stream().filter(o -> o.toLowerCase().startsWith(cur)).collect(Collectors.toList());
    }
}
