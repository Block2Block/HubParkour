package me.block2block.hubparkour;

import me.block2block.hubparkour.api.plates.PressurePlate;
import me.block2block.hubparkour.commands.CommandParkour;
import me.block2block.hubparkour.commands.ParkourTabComplete;
import me.block2block.hubparkour.entities.LeaderboardHologram;
import me.block2block.hubparkour.entities.Parkour;
import me.block2block.hubparkour.listeners.*;
import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.managers.DatabaseManager;
import me.block2block.hubparkour.utils.HubParkourExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.URL;
import java.util.Scanner;

public class Main extends JavaPlugin {

    private static Main instance;

    private static FileConfiguration config;

    private static boolean holograms;
    private static DatabaseManager dbManager;

    private static boolean pre1_13 = false;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onEnable() {
        instance = this;

        //Generating/Loading Config File
        if (!getDataFolder().exists()) getDataFolder().mkdir();
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            copy(getResource("config.yml"), configFile);
        }

        config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        config.options().copyHeader(true);

        if (!loadTypes()) {
            return;
        }

        holograms = Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");

        if (holograms) {
            getLogger().info("HolographicDisplays detected.");
        }

        dbManager = new DatabaseManager();

        try {
            dbManager.setup(getConfig().getString("Settings.Database.Type").toLowerCase().equals("mysql"));
        } catch (Exception e) {
            getLogger().severe("There has been an error connecting to the database. The plugin will now be disabled.  Stack Trace:\n");
            e.printStackTrace();
        }

        Bukkit.getPluginManager().registerEvents(new SetupListener(), this);

        Bukkit.getPluginManager().registerEvents(new PressurePlateListener(), this);
        Bukkit.getPluginManager().registerEvents(new BreakListener(), this);
        Bukkit.getPluginManager().registerEvents(new FlyListener(), this);
        Bukkit.getPluginManager().registerEvents(new FallListener(), this);

        getCommand("parkour").setExecutor(new CommandParkour());
        getCommand("parkour").setTabCompleter(new ParkourTabComplete());

        for (Parkour parkour : CacheManager.getParkours()) {
            for (PressurePlate pp : parkour.getAllPoints()) {
                pp.placeMaterial();
                if (pp.getType() != 2) {
                    CacheManager.addPoint(pp);
                }
            }

            if (getConfig().getBoolean("Settings.Holograms") && isHolograms()) {
                getLogger().info("Generating holograms for parkour " + parkour.getName() + "...");
                parkour.generateHolograms();
            }
        }

        for (LeaderboardHologram hologram : CacheManager.getLeaderboards()) {
            if (getConfig().getBoolean("Settings.Holograms") && isHolograms()) {
                getLogger().info("Generating leaderboard hologram for parkour " + hologram.getParkour().getName() + "...");
                hologram.generate();
            }
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new HubParkourExpansion(this).register();
        }

        switch (Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]) {
            case "v1_13_R1":
            case "v1_13_R2":
            case "v1_14_R1":
            case "v1_15_R1":
            case "v1_16_R1":
                pre1_13 = false;
                getLogger().info("1.13+ server version detected.");
                break;
            default:
                getLogger().info("Pre-1.13 server version detected.");
                pre1_13 = true;
        }

        getLogger().info("Plugin successfully enabled!");

        if (getConfig().getBoolean("Settings.Version-Checker.Enabled")) {
            String version = newVersionCheck();
            if (version != null) {
                getLogger().info("HubParkour v" + version + " is out now! I highly recommend you download the new version!");
            } else {
                getLogger().info("Your HubParkour version is up to date!");
            }
        }
    }

    @Override
    public void onDisable() {
        if (getConfig().getBoolean("Settings.Holograms") && isHolograms()) {
            for (Parkour parkour : CacheManager.getParkours()) {
                parkour.removeHolograms();
            }
        }
        if (dbManager != null) {
            dbManager.closeConnection();
        }
    }

    public static Main getInstance() {
        return instance;
    }

    public DatabaseManager getDbManager() {return dbManager;}

    private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public static String c(boolean prefix, String message) {
        return ChatColor.translateAlternateColorCodes('&',((prefix)?Main.getInstance().getConfig().getString("Messages.Prefix"):"&r") + message);
    }

    public static boolean isHolograms(){return holograms;}

    private boolean loadTypes() {
        Material start = ((getConfig().getString("Settings.Pressure-Plates.Start").toLowerCase().contains("plate"))?Material.matchMaterial(getConfig().getString("Settings.Pressure-Plates.Start")):null);
        Material checkpoint = ((getConfig().getString("Settings.Pressure-Plates.Checkpoint").toLowerCase().contains("plate"))?Material.matchMaterial(getConfig().getString("Settings.Pressure-Plates.Checkpoint")):null);
        Material end = ((getConfig().getString("Settings.Pressure-Plates.End").toLowerCase().contains("plate"))?Material.matchMaterial(getConfig().getString("Settings.Pressure-Plates.End")):null);

        if (start == null || checkpoint == null || end == null || start == checkpoint || start == end || checkpoint == end) {
            getLogger().info("There are invalid values in your config.yml for the pressure plate types. Please correct the error and restart your server. The plugin will now be disabled.");
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        }

        CacheManager.setType(0, start);
        CacheManager.setType(1, end);
        CacheManager.setType(2, Material.AIR);
        CacheManager.setType(3, checkpoint);
        return true;
    }

    public static String newVersionCheck() {
        try {
            String oldVersion = getInstance().getDescription().getVersion();
            String newVersion = fetchSpigotVersion();
            if (newVersion != null) {
                if(!newVersion.equals(oldVersion)) {
                    return newVersion;
                }
            }
            return null;
        }
        catch(Exception e) {
            getInstance().getLogger().info("Unable to check for new versions.");
        }
        return null;
    }

    private static String fetchSpigotVersion() {
        String resourceId = "47713";

        try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId).openStream(); Scanner scanner = new Scanner(inputStream)) {
            if (scanner.hasNext()) {
                return scanner.next();
            }
        } catch (IOException e) {
            getInstance().getLogger().info("Unable to connect to the Spigot resource API.");
            return null;
        }

        return null;
    }

    public static boolean isPre1_13() {
        return pre1_13;
    }

}
