package me.block2block.hubparkour;

import me.block2block.hubparkour.api.BackendAPI;
import me.block2block.hubparkour.api.plates.PressurePlate;
import me.block2block.hubparkour.commands.CommandParkour;
import me.block2block.hubparkour.commands.ParkourTabComplete;
import me.block2block.hubparkour.entities.HubParkourPlayer;
import me.block2block.hubparkour.entities.LeaderboardHologram;
import me.block2block.hubparkour.entities.Parkour;
import me.block2block.hubparkour.listeners.*;
import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.managers.DatabaseManager;
import me.block2block.hubparkour.utils.ConfigUtil;
import me.block2block.hubparkour.utils.HubParkourExpansion;
import me.block2block.hubparkour.utils.ItemUtil;
import org.bstats.bukkit.Metrics;
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

public class HubParkour extends JavaPlugin {

    private static HubParkour instance;

    private static boolean holograms;
    private static boolean placeholders = false;
    private static DatabaseManager dbManager;

    private static boolean pre1_13 = false;
    private static boolean post1_8 = true;
    private static boolean post1_9 = false;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onEnable() {
        instance = this;

        BackendAPI.setImplementation(new HubParkourAPIImpl());

        new Metrics(this, 14109);

        switch (Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]) {
            case "v1_12_R1":
            case "v1_11_R1":
            case "v1_10_R1":
                post1_9 = true;
            case "v1_9_R1":
            case "v1_9_R2":
                //Elytras are present in this version, register Elytra listener.
                Bukkit.getPluginManager().registerEvents(new ElytraListener(), this);
                getLogger().info("Legacy server version detected (1.8-1.12).");
                pre1_13 = true;
                post1_8 = true;
                break;
            case "v1_8_R1":
            case "v1_8_R2":
            case "v1_8_R3":
                getLogger().info("Legacy server version detected (1.8-1.12).");
                pre1_13 = true;
                post1_8 = false;
                break;
            default:
                pre1_13 = false;
                post1_8 = true;
                post1_9 = true;
                getLogger().info("1.13+ server version detected.");
                //Elytras are present in this version, register Elytra listener.
                Bukkit.getPluginManager().registerEvents(new ElytraListener(), this);
                Bukkit.getPluginManager().registerEvents(new PotionListener(), this);
                break;
        }

        //Generating/Loading Config File
        if (!getDataFolder().exists()) getDataFolder().mkdir();
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            if (pre1_13) {
                getLogger().info("Generating 1.8-1.12 configuration file.");
                copy(getResource("config1_8.yml"), configFile);
            } else {
                getLogger().info("Generating 1.13+ configuration file.");
                copy(getResource("config1_13.yml"), configFile);
            }
        }

        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        config.options().copyHeader(true);

        ConfigUtil.init(config, configFile);

        if (!loadTypes()) {
            return;
        }



        holograms = Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");

        if (holograms) {
            getLogger().info("HolographicDisplays has been detected detected.");
        }

        dbManager = new DatabaseManager();

        try {
            dbManager.setup(ConfigUtil.getString("Settings.Database.Type", "SQLite").equalsIgnoreCase("mysql"));
        } catch (Exception e) {
            getLogger().severe("There has been an error connecting to the database. The plugin will now be disabled.  Stack Trace:\n");
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
        Bukkit.getPluginManager().registerEvents(new MountListener(), this);
        Bukkit.getPluginManager().registerEvents(new CommandListener(), this);
        Bukkit.getPluginManager().registerEvents(new SignListener(), this);

        getCommand("parkour").setExecutor(new CommandParkour());
        getCommand("parkour").setTabCompleter(new ParkourTabComplete());

        for (Parkour parkour : CacheManager.getParkours()) {
            for (PressurePlate pp : parkour.getAllPoints()) {
                pp.placeMaterial();
                if (pp.getType() != 4) {
                    if (pp.getType() != 2) {
                        CacheManager.addPoint(pp);
                    } else {
                        CacheManager.addRestartPoint(pp);
                    }
                }
            }

            if (isHolograms()) {
                getLogger().info("Generating holograms for parkour " + parkour.getName() + "...");
                parkour.generateHolograms();
                getLogger().info("Holograms successfully generated for parkour " + parkour.getName() + "!");
            }
        }

        for (LeaderboardHologram hologram : CacheManager.getLeaderboards()) {
            if (isHolograms()) {
                getLogger().info("Generating leaderboard hologram for parkour " + hologram.getParkour().getName() + " (ID: " + hologram.getId() + ") ...");
                hologram.generate();
                getLogger().info("Leaderboard hologram for parkour " + hologram.getParkour().getName() + " (ID: " + hologram.getId() + ") successfully generated!");
            }
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholders = true;
            getLogger().info("PlaceholderAPI detected, registering HubParkour placeholder expansion.");
            new HubParkourExpansion(this).register();
        }

        if (!loadItems()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Plugin successfully enabled!");

        if (ConfigUtil.getBoolean("Settings.Version-Checker.Enabled", true)) {
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
        if (isHolograms()) {
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

    public static HubParkour getInstance() {
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

    public static String c(boolean prefix, String message) {
        return ChatColor.translateAlternateColorCodes('&',((prefix)?ConfigUtil.getString("Messages.Prefix", "&2Parkour>> &r"):"&r") + message);
    }

    public static boolean isHolograms(){return holograms;}

    private boolean loadTypes() {

        String startMaterial = ConfigUtil.getString("Settings.Pressure-Plates.Start", ((isPre1_13())?"WOOD_PLATE":"OAK_PRESSURE_PLATE")), checkpointMaterial = ConfigUtil.getString("Settings.Pressure-Plates.Checkpoint",  ((isPre1_13())?"GOLD_PLATE":"LIGHT_WEIGHTED_PRESSURE_PLATE")), endMaterial = ConfigUtil.getString("Settings.Pressure-Plates.End", ((isPre1_13())?"IRON_PLATE":"HEAVY_WEIGHTED_PRESSURE_PLATE"));

        Material start = ((startMaterial.toLowerCase().contains("plate"))?Material.matchMaterial(startMaterial):null);
        Material checkpoint = ((checkpointMaterial.toLowerCase().contains("plate"))?Material.matchMaterial(checkpointMaterial):null);
        Material end = ((endMaterial.toLowerCase().contains("plate"))?Material.matchMaterial(endMaterial):null);

        if (start == null || checkpoint == null || end == null) {
            getLogger().info("There are invalid values in your config.yml for the pressure plate types. Please correct the error and restart your server. The plugin will now be disabled.");
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        }

        CacheManager.setType(0, start);
        CacheManager.setType(1, end);
        CacheManager.setType(2, Material.AIR);
        CacheManager.setType(3, checkpoint);
        CacheManager.setType(4, Material.AIR);
        return true;
    }

    private boolean loadItems() {
        Material reset = Material.matchMaterial(ConfigUtil.getString("Settings.Parkour-Items.Reset.Item", ((isPre1_13()?"WOOD_DOOR":"OAK_DOOR"))));
        Material checkpoint = Material.matchMaterial(ConfigUtil.getString("Settings.Parkour-Items.Checkpoint.Item", ((isPre1_13()?"GOLD_PLATE":"LIGHT_WEIGHTED_PRESSURE_PLATE"))));
        Material cancel = Material.matchMaterial(ConfigUtil.getString("Settings.Parkour-Items.Cancel.Item", ((isPre1_13()?"BED":"RED_BED"))));

        Material hidden = Material.matchMaterial(ConfigUtil.getString("Settings.Parkour-Items.Hide.Hidden.Item", "MAGMA_CREAM"));
        Material shown = Material.matchMaterial(ConfigUtil.getString("Settings.Parkour-Items.Hide.Shown.Item", "SLIME_BALL"));

        if (reset == null || checkpoint == null || cancel == null || hidden == null || shown == null) {
            getLogger().info("There are invalid values in your config.yml for the parkour items. Please correct the error and restart your server. The plugin will now be disabled.");
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        }

        if (ConfigUtil.getInt("Settings.Parkour-Items.Cancel.Slot", 6) != -1) {
            ItemStack item = ItemUtil.ci(cancel, ConfigUtil.getString("Settings.Parkour-Items.Cancel.Name", "&cCancel"), 1, "", (short) ConfigUtil.getInt("Settings.Parkour-Items.Cancel.Item-Data", 0));
            CacheManager.setItem(2, item);
        }

        if (ConfigUtil.getInt("Settings.Parkour-Items.Reset.Slot", 5) != -1) {
            ItemStack item = ItemUtil.ci(reset, ConfigUtil.getString("Settings.Parkour-Items.Reset.Name", "&cReset"), 1, "", (short) ConfigUtil.getInt("Settings.Parkour-Items.Reset.Item-Data", 0));
            CacheManager.setItem(0, item);
        }

        if (ConfigUtil.getInt("Settings.Parkour-Items.Checkpoint.Slot", 4) != -1) {
            ItemStack item = ItemUtil.ci(checkpoint, ConfigUtil.getString("Settings.Parkour-Items.Checkpoint.Name", "&aTeleport to Last Checkpoint"), 1, "", (short) ConfigUtil.getInt("Settings.Parkour-Items.Checkpoint.Item-Data", 0));
            CacheManager.setItem(1, item);
        }

        if (ConfigUtil.getInt("Settings.Parkour-Items.Hide.Slot", 8) != -1) {
            ItemStack item = ItemUtil.ci(hidden, ConfigUtil.getString("Settings.Parkour-Items.Hide.Hidden.Name", "&cShow all players"), 1, "", (short) ConfigUtil.getInt("Settings.Parkour-Items.Hide.Hidden.Item-Data", 0));
            CacheManager.setItem(4, item);
            item = ItemUtil.ci(shown, ConfigUtil.getString("Settings.Parkour-Items.Hide.Shown.Name", "&aHide all players"), 1, "", (short) ConfigUtil.getInt("Settings.Parkour-Items.Hide.Shown.Item-Data", 0));
            CacheManager.setItem(3, item);
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

    public static boolean isPost1_8() {
        return post1_8;
    }

    public static boolean isPost1_9() {
        return post1_9;
    }

    public static boolean isPlaceholders() {
        return placeholders;
    }
}
