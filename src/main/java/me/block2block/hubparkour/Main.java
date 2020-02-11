package me.block2block.hubparkour;

import me.block2block.hubparkour.commands.CommandParkour;
import me.block2block.hubparkour.commands.ParkourTabComplete;
import me.block2block.hubparkour.entities.Parkour;
import me.block2block.hubparkour.entities.plates.PressurePlate;
import me.block2block.hubparkour.listeners.BreakListener;
import me.block2block.hubparkour.listeners.FlyListener;
import me.block2block.hubparkour.listeners.PressurePlateListener;
import me.block2block.hubparkour.listeners.SetupListener;
import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.managers.DatabaseManager;
import me.block2block.hubparkour.utils.HubParkourExpansion;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Main extends JavaPlugin {

    private static Main instance;

    private static File configFile;
    private static FileConfiguration config;

    private static boolean holograms;
    private static DatabaseManager dbManager;

    private static boolean pre1_13 = false;

    @Override
    public void onEnable() {
        instance = this;

        //Generating/Loading Config File
        if (!getDataFolder().exists()) getDataFolder().mkdir();
        configFile = new File(getDataFolder(), "config.yml");
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
            dbManager.setup(getConfig().getString("Settings.Database.Type").toLowerCase().equals("MySQL"));
        } catch (Exception e) {
            getLogger().severe("There has been an error connecting to the database. The plugin will now be disabled.  Stack Trace:\n");
            e.printStackTrace();
        }

        Bukkit.getPluginManager().registerEvents(new SetupListener(), this);

        Bukkit.getPluginManager().registerEvents(new PressurePlateListener(), this);
        Bukkit.getPluginManager().registerEvents(new BreakListener(), this);
        Bukkit.getPluginManager().registerEvents(new FlyListener(), this);

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

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new HubParkourExpansion(this).register();
        }

        switch (Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]) {
            case "v1_13_R1":
            case "v1_13_R2":
            case "v1_14_R1":
            case "v1_15_R1":
                pre1_13 = false;
                getLogger().info("1.13/1.14 server version detected.");
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
        dbManager.closeConnection();
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
        Material end = ((getConfig().getString("Settings.Pressure-Plates.End").toLowerCase().contains("plate"))?Material.matchMaterial(getConfig().getString("Settings.Pressure-Plates.End")):null);;

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
            if(!newVersion.equals(oldVersion)) {
                return newVersion;
            }
            return null;
        }
        catch(Exception e) {
            getInstance().getLogger().info("Unable to check for new versions.");
        }
        return null;
    }

    private static String fetchSpigotVersion() {
        try {
            // We're connecting to spigot's API
            URL url = new URL("https://www.spigotmc.org/api/general.php");
            // Creating a connection
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            // We're writing a body that contains the API access key (Not required and obsolete, but!)
            con.setDoOutput(true);

            // Can't think of a clean way to represent this without looking bad
            String body = "key" + "=" + "98BE0FE67F88AB82B4C197FAF1DC3B69206EFDCC4D3B80FC83A00037510B99B4" + "&" +
                    "resource=47713";

            // Get the output stream, what the site receives
            try (OutputStream stream = con.getOutputStream()) {
                // Write our body containing version and access key
                stream.write(body.getBytes(StandardCharsets.UTF_8));
            }

            // Get the input stream, what we receive
            try (InputStream input = con.getInputStream()) {
                // Read it to string
                String version = IOUtils.toString(input);

                // If the version is not empty, return it
                if (!version.isEmpty()) {
                    return version;
                }
            }
        }
        catch (Exception ex) {
            Bukkit.getLogger().warning("Failed to check for a update on spigot.");
        }

        return null;
    }

    public static boolean isPre1_13() {
        return pre1_13;
    }

}
