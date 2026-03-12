package ru.mirai.cds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class Commands implements CommandExecutor, TabCompleter {
    private final CDSPlugin plugin;

    public Commands(CDSPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (plugin.isCdsAdminOnly() && sender instanceof org.bukkit.entity.Player) {
            if (!sender.hasPermission("cds.admin") && !sender.isOp()) {
                sender.sendMessage("§cКоманда /cds доступна только администраторам.");
                return true;
            }
        }
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("add")) {
            if (!sender.hasPermission("cds.admin") && !sender.isOp()) {
                sender.sendMessage("§cНет прав");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage("Использование: /cds add <name> <link> — добавляет координаты вашей позиции");
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cТолько игрок может добавить координату (используйте игрока для позиции)");
                return true;
            }
            String name = args[1];
            String link = args[2];
            Player p = (Player) sender;
            Location loc = p.getLocation().getBlock().getLocation();
            ConfigurationSection sec = plugin.getData().getConfigurationSection("entries");
            if (sec == null) sec = plugin.getData().createSection("entries");
            ConfigurationSection e = sec.createSection(name);
            e.set("world", loc.getWorld().getName());
            e.set("x", loc.getBlockX());
            e.set("y", loc.getBlockY());
            e.set("z", loc.getBlockZ());
            e.set("link", link);
            plugin.saveData();
            if (plugin.getInteractListener() != null) {
                plugin.getInteractListener().spawnMarker(name, loc);
            }
            plugin.incrementAdds();
            sender.sendMessage("§aДобавлено: " + name + " -> " + link + " @ " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
            return true;
        }

        if (sub.equals("remove")) {
            if (!sender.hasPermission("cds.admin") && !sender.isOp()) {
                sender.sendMessage("§cНет прав");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("Использование: /cds remove <name>");
                return true;
            }
            String name = args[1];
            if (plugin.getData().contains("entries." + name)) {
                plugin.getData().set("entries." + name, null);
                plugin.saveData();
                if (plugin.getInteractListener() != null) plugin.getInteractListener().removeMarker(name);
                plugin.incrementRemoves();
                sender.sendMessage("§aУдалено: " + name);
            } else {
                sender.sendMessage("§cНе найдено: " + name);
            }
            return true;
        }

        if (sub.equals("list")) {
            ConfigurationSection sec = plugin.getData().getConfigurationSection("entries");
            if (sec == null || sec.getKeys(false).isEmpty()) {
                sender.sendMessage("§eСписок пуст");
                return true;
            }

            for (String name : sec.getKeys(false)) {
                ConfigurationSection e = sec.getConfigurationSection(name);
                if (e == null) continue;
                String link = e.getString("link", "");
                int x = e.getInt("x");
                int y = e.getInt("y");
                int z = e.getInt("z");
                String coords = x + "," + y + "," + z;
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    Component line = Component.empty()
                            .append(Component.text(coords + " | ", NamedTextColor.GREEN))
                            .append(Component.text(name, NamedTextColor.AQUA)
                                    .clickEvent(ClickEvent.runCommand("/cds tp " + name))
                                    .hoverEvent(HoverEvent.showText(Component.text("Телепортироваться", NamedTextColor.YELLOW))));
                    p.sendMessage(line);
                } else {
                    sender.sendMessage(coords + " | " + name + " -> " + link);
                }
            }
            return true;
        }

        if (sub.equals("tp")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Только игрок может телепортироваться");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("Использование: /cds tp <name>");
                return true;
            }
            String name = args[1];
            ConfigurationSection e = plugin.getData().getConfigurationSection("entries." + name);
            if (e == null) {
                sender.sendMessage("§cНе найдено: " + name);
                return true;
            }
            Player p = (Player) sender;
            String world = e.getString("world");
            int x = e.getInt("x");
            int y = e.getInt("y");
            int z = e.getInt("z");
            try {
                p.teleport(new Location(p.getServer().getWorld(world), x + 0.5, y, z + 0.5));
                p.sendMessage("§aТелепортировано: " + name + " (" + x + "," + y + "," + z + ")");
            } catch (Exception ex) {
                sender.sendMessage("§cНе удалось телепортироваться: мир не найден");
            }
            return true;
        }

        if (sub.equals("reload")) {
            if (!sender.hasPermission("cds.admin") && !sender.isOp()) {
                sender.sendMessage("§cНет прав");
                return true;
            }
            plugin.reloadConfig();
            plugin.loadData();
            ConfigurationSection sec = plugin.getData().getConfigurationSection("entries");
            if (sec != null) {
                for (String name : sec.getKeys(false)) {
                    if (plugin.getInteractListener() != null) plugin.getInteractListener().removeMarker(name);
                }
                for (String name : sec.getKeys(false)) {
                    ConfigurationSection e = sec.getConfigurationSection(name);
                    if (e == null) continue;
                    String world = e.getString("world");
                    int x = e.getInt("x");
                    int y = e.getInt("y");
                    int z = e.getInt("z");
                    if (plugin.getServer().getWorld(world) == null) continue;
                    org.bukkit.Location loc = new org.bukkit.Location(plugin.getServer().getWorld(world), x + 0.5, y + 0.5, z + 0.5);
                    if (plugin.getInteractListener() != null) plugin.getInteractListener().spawnMarker(name, loc);
                }
            }
            sender.sendMessage("§aКонфигурация и данные перезагружены.");
            return true;
        }

        sendUsage(sender);
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§6/cds add <name> <link> §7— добавить запись (admin)");
        sender.sendMessage("§6/cds remove <name> §7— удалить запись (admin)");
        sender.sendMessage("§6/cds list §7— показать список");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (plugin.isCdsAdminOnly() && sender instanceof org.bukkit.entity.Player) {
            if (!sender.hasPermission("cds.admin") && !sender.isOp()) return Collections.emptyList();
        }
        if (args.length == 1) {
            List<String> sub = Arrays.asList("add", "remove", "list", "reload");
            List<String> out = new ArrayList<>();
            String a = args[0].toLowerCase(Locale.ROOT);
            for (String s : sub) if (s.startsWith(a)) out.add(s);
            return out;
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("remove")) {
                ConfigurationSection sec = plugin.getData().getConfigurationSection("entries");
                if (sec == null) return Collections.emptyList();
                List<String> keys = new ArrayList<>(sec.getKeys(false));
                String a = args[1].toLowerCase(Locale.ROOT);
                keys.removeIf(k -> !k.toLowerCase(Locale.ROOT).startsWith(a));
                return keys;
            }
        }

        return Collections.emptyList();
    }
}
