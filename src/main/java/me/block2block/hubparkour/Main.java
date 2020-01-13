package me.block2block.hubparkour;

import me.block2block.hubparkour.managers.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Main extends JavaPlugin {

    private static Main instance;

    private static File configFile;
    private static FileConfiguration config;

    private static boolean holograms;
    private static DatabaseManager dbManager;

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

        holograms = Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");

        dbManager = new DatabaseManager();

        try {
            dbManager.setup(getConfig().getString("Settings.Database.Type").toLowerCase().equals("MySQL"));
        } catch (Exception e) {
            getLogger().severe("There has been an error connecting to the database. The plugin will now be disabled.  Stack Trace:\n");
            e.printStackTrace();
        }

    }

    @Override
    public void onDisable() {

    }

    public static Main getInstance() {
        return instance;
    }

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

}
