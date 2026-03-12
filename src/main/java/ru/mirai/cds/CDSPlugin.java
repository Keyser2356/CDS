package ru.mirai.cds;

import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class CDSPlugin extends JavaPlugin {
    private File dataFile;
    private YamlConfiguration data;
    // statistics
    private volatile int statAdds = 0;
    private volatile int statRemoves = 0;
    private volatile int statClicks = 0;
    private Metrics metrics;

    @Override
    public void onEnable() {
        this.dataFile = new File(getDataFolder(), "data.yml");
        loadData();
        // load config
        saveDefaultConfig();

        // create and register listener
        this.interactListener = new InteractListener(this);
        getServer().getPluginManager().registerEvents(this.interactListener, this);

        Commands commands = new Commands(this);
        if (getCommand("cds") != null) {
            getCommand("cds").setExecutor(commands);
            getCommand("cds").setTabCompleter(commands);
        }

        // initialize bStats if configured
        if (getConfig().getBoolean("metrics.enabled", false)) {
            int id = getConfig().getInt("metrics.bstats-id", 0);
            if (id > 0) {
                try {
                    this.metrics = new Metrics(this, id);
                } catch (Throwable ex) {
                    getLogger().warning("Не удалось инициализировать bStats: " + ex.getMessage());
                }
            }
        }
    }

    private InteractListener interactListener;

    public InteractListener getInteractListener() {
        return interactListener;
    }

    public int getCooldownSeconds() {
        return getConfig().getInt("player_hider.cooldown", 3);
    }

    public boolean isPlayerHiderEnabled() {
        return getConfig().getBoolean("player_hider.enabled", false);
    }

    public int getPlayerHiderSlot() {
        return getConfig().getInt("player_hider.slot", 4);
    }

    public boolean isDisableInventoryMovement() {
        return getConfig().getBoolean("player_hider.disable_inventory_movement", true);
    }

    @Override
    public void onDisable() {
        saveData();
    }

    public void loadData() {
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        try {
            if (!dataFile.exists()) dataFile.createNewFile();
        } catch (IOException e) {
            getLogger().severe("Не удалось создать data.yml: " + e.getMessage());
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveData() {
        try {
            if (data != null) data.save(dataFile);
        } catch (IOException e) {
            getLogger().severe("Не удалось сохранить data.yml: " + e.getMessage());
        }
    }

    public YamlConfiguration getData() {
        return data;
    }

    public boolean isCdsAdminOnly() {
        return getConfig().getBoolean("commands.cds_admin_only", true);
    }

    // statistic helpers
    public synchronized void incrementAdds() { statAdds++; }
    public synchronized void incrementRemoves() { statRemoves++; }
    public synchronized void incrementClicks() { statClicks++; }

    public int getStatAdds() { return statAdds; }
    public int getStatRemoves() { return statRemoves; }
    public int getStatClicks() { return statClicks; }
}
