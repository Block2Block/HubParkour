package me.block2block.hubparkour;

import me.block2block.hubparkour.api.plates.PressurePlate;
import me.block2block.hubparkour.commands.CommandParkour;
import me.block2block.hubparkour.commands.ParkourTabComplete;
import me.block2block.hubparkour.entities.HubParkourPlayer;
import me.block2block.hubparkour.entities.LeaderboardHologram;
import me.block2block.hubparkour.entities.Parkour;
import me.block2block.hubparkour.listeners.*;
import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.managers.DatabaseManager;
import me.block2block.hubparkour.utils.HubParkourExpansion;
import me.block2block.hubparkour.utils.ItemUtil;
import me.block2block.hubparkour.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
    private static boolean post1_8 = true;
    private static boolean post1_9 = false;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onEnable() {
        instance = this;

        LoggerUtil.log(LoggerUtil.LogLevel.OUTLINE, "*************************************************************");
        LoggerUtil.log(LoggerUtil.LogLevel.PLUGIN_NAME, " _    _       _       _____           _                      ");
        LoggerUtil.log(LoggerUtil.LogLevel.PLUGIN_NAME, "| |  | |     | |     |  __ \\         | |                    ");
        LoggerUtil.log(LoggerUtil.LogLevel.PLUGIN_NAME, "| |__| |_   _| |__   | |__) |_ _ _ __| | _____  _   _ _ __   ");
        LoggerUtil.log(LoggerUtil.LogLevel.PLUGIN_NAME, "|  __  | | | | '_ \\  |  ___/ _` | '__| |/ / _ \\| | | | '__|");
        LoggerUtil.log(LoggerUtil.LogLevel.PLUGIN_NAME2, "| |  | | |_| | |_) | | |  | (_| | |  |   < (_) | |_| | |     ");
        LoggerUtil.log(LoggerUtil.LogLevel.PLUGIN_NAME2, "|_|  |_|\\__,_|_.__/  |_|   \\__,_|_|  |_|\\_\\___/ \\__,_|_|");
        LoggerUtil.log(LoggerUtil.LogLevel.PLUGIN_NAME, "                                                             ");
        LoggerUtil.log(LoggerUtil.LogLevel.OUTLINE, "*************************************************************");

        switch (Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]) {
            case "v1_13_R1":
            case "v1_13_R2":
            case "v1_14_R1":
            case "v1_15_R1":
            case "v1_16_R1":
            case "v1_16_R2":
            case "v1_16_R3":
            case "v1_16_R4":
            case "v1_17_R1":
                pre1_13 = false;
                post1_8 = true;
                post1_9 = true;
                LoggerUtil.log(LoggerUtil.LogLevel.INFO, "1.13+ server version was detected.");
                //Elytras are present in this version, register Elytra listener.
                Bukkit.getPluginManager().registerEvents(new ElytraListener(), this);
                Bukkit.getPluginManager().registerEvents(new PotionListener(), this);
                break;
            case "v1_12_R1":
            case "v1_11_R1":
            case "v1_10_R1":
                post1_9 = true;
            case "v1_9_R1":
            case "v1_9_R2":
                //Elytras are present in this version, register Elytra listener.
                Bukkit.getPluginManager().registerEvents(new ElytraListener(), this);
                LoggerUtil.log(LoggerUtil.LogLevel.INFO, "Pre-1.13 (Legacy) server version was detected.");
                pre1_13 = true;
                post1_8 = true;
                break;
            default:
                LoggerUtil.log(LoggerUtil.LogLevel.INFO, "Pre-1.13 (Legacy) server version was detected.");
                pre1_13 = true;
                post1_8 = false;
        }

        //Generating/Loading Config File
        if (!getDataFolder().exists()) getDataFolder().mkdir();
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            if (pre1_13) {
                copy(getResource("config1_8.yml"), configFile);
            } else {
                copy(getResource("config1_13.yml"), configFile);
            }

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
            LoggerUtil.log(LoggerUtil.LogLevel.INFO, "HolographicDisplays was detected.");
        }

        dbManager = new DatabaseManager();

        try {
            dbManager.setup(getConfig().getString("Settings.Database.Type").equalsIgnoreCase("mysql"));
        } catch (Exception e) {
            LoggerUtil.log(LoggerUtil.LogLevel.ERROR, "There has been an error connecting to the database. HubParkour will be disabled. Stack Trace:\n");
            e.printStackTrace();
        }

        Bukkit.getPluginManager().registerEvents(new SetupListener(), this);

        Bukkit.getPluginManager().registerEvents(new PressurePlateListener(), this);
        Bukkit.getPluginManager().registerEvents(new BreakListener(), this);
        Bukkit.getPluginManager().registerEvents(new FlyListener(), this);
        Bukkit.getPluginManager().registerEvents(new FallListener(), this);
        Bukkit.getPluginManager().registerEvents(new ItemClickListener(), this);
        Bukkit.getPluginManager().registerEvents(new DropListener(), this);
        Bukkit.getPluginManager().registerEvents(new LeaveListener(), this);

        getCommand("parkour").setExecutor(new CommandParkour());
        getCommand("parkour").setTabCompleter(new ParkourTabComplete());

        for (Parkour parkour : CacheManager.getParkours()) {
            for (PressurePlate pp : parkour.getAllPoints()) {
                pp.placeMaterial();
                if (pp.getType() != 2) {
                    CacheManager.addPoint(pp);
                } else {
                    CacheManager.addRestartPoint(pp);
                }
            }

            if (getConfig().getBoolean("Settings.Holograms") && isHolograms()) {

                LoggerUtil.log(LoggerUtil.LogLevel.INFO, "Generating holograms for parkour " + parkour.getName() + "...");

                parkour.generateHolograms();
            }
        }

        for (LeaderboardHologram hologram : CacheManager.getLeaderboards()) {
            if (getConfig().getBoolean("Settings.Holograms") && isHolograms()) {
                LoggerUtil.log(LoggerUtil.LogLevel.INFO, "Generating leaderboard hologram for parkour " + hologram.getParkour().getName() + "...");
                hologram.generate();
            }
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new HubParkourExpansion(this).register();
        }

        if (!loadItems()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        LoggerUtil.log(LoggerUtil.LogLevel.SUCCESS, "HubParkour has successfully enabled!");

        if (getConfig().getBoolean("Settings.Version-Checker.Enabled")) {
            String version = newVersionCheck();
            if (version != null) {
                LoggerUtil.log(LoggerUtil.LogLevel.WARN, "HubParkour v" + version + " has been released! It is highly recommend that you download the new version!");
            } else {
                LoggerUtil.log(LoggerUtil.LogLevel.SUCCESS, "HubParkour is up-to-date!");
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

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (CacheManager.isParkour(player)) {
                HubParkourPlayer pl = CacheManager.getPlayer(player);
                pl.removeItems();
                pl.setToPrevState();
            }
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

        if (start == null || checkpoint == null || end == null) {
            LoggerUtil.log(LoggerUtil.LogLevel.ERROR, "There are invalid values in your config.yml for pressure plate types. Please fix the error and restart your server. HubParkour will now be disabled.");
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        }

        CacheManager.setType(0, start);
        CacheManager.setType(1, end);
        CacheManager.setType(2, Material.AIR);
        CacheManager.setType(3, checkpoint);
        return true;
    }

    private boolean loadItems() {
        Material reset = Material.matchMaterial(getConfig().getString("Settings.Parkour-Items.Reset.Item"));
        Material checkpoint = Material.matchMaterial(getConfig().getString("Settings.Parkour-Items.Checkpoint.Item"));
        Material cancel = Material.matchMaterial(getConfig().getString("Settings.Parkour-Items.Cancel.Item"));

        if (reset == null || checkpoint == null || cancel == null) {
            LoggerUtil.log(LoggerUtil.LogLevel.ERROR, "There are invalid values in your config.yml for the parkour items. Please fix the error and restart your server. HubParkour will now be disabled.");
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        }

        if (getConfig().getInt("Settings.Parkour-Items.Cancel.Slot") != -1) {
            ItemStack item = ItemUtil.ci(cancel, getConfig().getString("Settings.Parkour-Items.Cancel.Name"), 1, "", (short) getConfig().getInt("Settings.Parkour-Items.Cancel.Item-Data"));
            CacheManager.setItem(2, item);
        }

        if (getConfig().getInt("Settings.Parkour-Items.Reset.Slot") != -1) {
            ItemStack item = ItemUtil.ci(reset, getConfig().getString("Settings.Parkour-Items.Reset.Name"), 1, "", (short) getConfig().getInt("Settings.Parkour-Items.Reset.Item-Data"));
            CacheManager.setItem(0, item);
        }

        if (getConfig().getInt("Settings.Parkour-Items.Checkpoint.Slot") != -1) {
            ItemStack item = ItemUtil.ci(checkpoint, getConfig().getString("Settings.Parkour-Items.Checkpoint.Name"), 1, "", (short) getConfig().getInt("Settings.Parkour-Items.Checkpoint.Item-Data"));
            CacheManager.setItem(1, item);
        }

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
            LoggerUtil.log(LoggerUtil.LogLevel.INFO, "Unable to check for new versions of HubParkour/");
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
            LoggerUtil.log(LoggerUtil.LogLevel.INFO, "Unable to connect to the Spigot resource API.");
            return null;
        }

        return null;
    }

    public static boolean isPre1_13() {
        return pre1_13;
    }

    public static boolean isPost1_8() {
        return post1_8;
    }

    public static boolean isPost1_9() {
        return post1_9;
    }
}
