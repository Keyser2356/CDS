package ru.mirai.cds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InteractListener implements Listener {
    private final CDSPlugin plugin;
    private final NamespacedKey markerKey;
    private final Map<String, ArmorStand> spawned = new HashMap<>();

    public InteractListener(CDSPlugin plugin) {
        this.plugin = plugin;
        this.markerKey = new NamespacedKey(plugin, "cds_marker");
    }
    
    // spawn a marker at the specified location for the given entry name
    public void spawnMarker(String name, Location loc) {
        String world = loc.getWorld().getName();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        String key = world + ":" + x + ":" + y + ":" + z + ":" + name;
        ArmorStand as = spawned.get(key);
        if (as == null || as.isDead()) {
            Location spawnLoc = new Location(loc.getWorld(), x + 0.5, y + 0.5, z + 0.5);
            as = (ArmorStand) loc.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
            as.setCustomNameVisible(false);
            as.setGravity(false);
            as.setInvulnerable(true);
            as.setInvisible(true);
            as.getPersistentDataContainer().set(markerKey, PersistentDataType.STRING, name);
            spawned.put(key, as);
        }
    }

    public void removeMarker(String name) {
        // remove any markers with this name
        spawned.entrySet().removeIf(ent -> {
            ArmorStand as = ent.getValue();
            if (as == null) return true;
            if (as.getPersistentDataContainer().has(markerKey, PersistentDataType.STRING)) {
                String n = as.getPersistentDataContainer().get(markerKey, PersistentDataType.STRING);
                if (n != null && n.equals(name)) {
                    if (!as.isDead()) as.remove();
                    return true;
                }
            }
            return false;
        });
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        if (!(e.getRightClicked() instanceof ArmorStand)) return;
        ArmorStand as = (ArmorStand) e.getRightClicked();
        if (!as.getPersistentDataContainer().has(markerKey, PersistentDataType.STRING)) return;
        String name = as.getPersistentDataContainer().get(markerKey, PersistentDataType.STRING);
        if (name == null) return;
        ConfigurationSection ent = plugin.getData().getConfigurationSection("entries." + name);
        if (ent == null) return;
        sendDiscordMessage((Player) e.getPlayer(), ent);
        e.setCancelled(true);
    }

    private void sendDiscordMessage(Player p, ConfigurationSection ent) {
        String link = ent.getString("link", "");
        if (link == null) link = "";

        String configuredType = plugin.getConfig().getString("link.type", "short");
        String displayLink;
        String lower = link.toLowerCase();
        int idx = lower.indexOf("discord.gg");
        if ("short".equalsIgnoreCase(configuredType) && idx >= 0) {
            displayLink = link.substring(idx);
        } else {
            // remove protocol for nicer display
            displayLink = link.replaceFirst("(?i)^https?://", "");
        }

        String template = plugin.getConfig().getString("message.template", "{link}");
        LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();

        int phIndex = template.indexOf("{link}");
        String before = phIndex >= 0 ? template.substring(0, phIndex) : template;
        String after = phIndex >= 0 ? template.substring(phIndex + "{link}".length()) : "";

        // detect trailing ampersand color/style codes immediately before the placeholder
        String prefixCodes = "";
        if (!before.isEmpty()) {
            Pattern pattern = Pattern.compile("(?i)(?:&[0-9A-FK-OR])+$");
            Matcher m = pattern.matcher(before);
            if (m.find()) prefixCodes = m.group(0);
        }

        // prepare link display text with prefix color codes if any
        String linkDisplayRaw = displayLink;
        if (!prefixCodes.isEmpty()) linkDisplayRaw = prefixCodes + linkDisplayRaw;

        Component result = Component.empty();
        if (!before.isEmpty()) result = result.append(legacy.deserialize(before));

        Component linkComp = legacy.deserialize(linkDisplayRaw).clickEvent(ClickEvent.openUrl(link.isEmpty() ? displayLink : link));
        result = result.append(linkComp);

        if (!after.isEmpty()) result = result.append(legacy.deserialize(after));

        p.sendMessage(result);
        plugin.incrementClicks();
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("cds.admin") || p.isOp()) return;
        e.setCancelled(true);
        p.sendMessage("§cВы не можете выбросить предмет.");
    }
}
