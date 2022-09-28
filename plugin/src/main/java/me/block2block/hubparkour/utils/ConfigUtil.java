package me.block2block.hubparkour.utils;

import me.block2block.hubparkour.HubParkour;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ConfigUtil {

    private static FileConfiguration config;
    private static File configFile;

    private static FileConfiguration internal;
    private static File internalFile;

    public static void init(FileConfiguration config, File configFile, FileConfiguration internal, File internalFile) {
        ConfigUtil.config = config;
        ConfigUtil.configFile = configFile;
        ConfigUtil.internal = internal;
        ConfigUtil.internalFile = internalFile;
    }

    /**
     * Send message to user, or nothing if the config ID is missing or blank.
     * @param player the player to send the message to.
     * @param id the id of the config value you want.
     */
    public static void sendMessage(Player player, String id, String defaultValue, boolean prefix, Map<String, String> variableMappings) {
        String message = config.getString(id);
        if (message != null) {
            if (!message.equals("")) {
                for (Map.Entry<String, String> entry : variableMappings.entrySet()) {
                    message = message.replace("{" + entry.getKey() + "}", entry.getValue());
                }
                if (HubParkour.isPlaceholders()) {
                    message = PlaceholderAPI.setPlaceholders(player, message);
                }
                player.getPlayer().sendMessage(HubParkour.c(prefix, message));
            }
        } else {
            config.set(id, defaultValue);
            try {
                config.save(configFile);
            } catch (IOException e) {
                HubParkour.getInstance().getLogger().warning("An attempt was made to insert a missing config value but an error occurred during the attempt. Stack trace:");
                e.printStackTrace();
            }
            message = defaultValue;
            for (Map.Entry<String, String> entry : variableMappings.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
            if (HubParkour.isPlaceholders()) {
                message = PlaceholderAPI.setPlaceholders(player, message);
            }
            player.getPlayer().sendMessage(HubParkour.c(prefix, message));
        }
    }

    public static void sendMessageOrDefault(Player player, String id, String defaultMessage, boolean prefix, Map<String, String> variableMappings) {
        String message = config.getString(id);
        if (message != null) {
            if (!message.equals("")) {
                for (Map.Entry<String, String> entry : variableMappings.entrySet()) {
                    message = message.replace("{" + entry.getKey() + "}", entry.getValue());
                }
                if (HubParkour.isPlaceholders()) {
                    message = PlaceholderAPI.setPlaceholders(player, message);
                }
                player.getPlayer().sendMessage(HubParkour.c(prefix, message));
            }
        } else {
            config.set(id, defaultMessage);
            try {
                config.save(configFile);
            } catch (IOException e) {
                HubParkour.getInstance().getLogger().warning("An attempt was made to insert a missing config value but an error occurred during the attempt. Stack trace:");
                e.printStackTrace();
            }
            player.getPlayer().sendMessage(HubParkour.c(prefix, defaultMessage));
        }
    }

    public static boolean getBoolean(String id, boolean defaultValue) {
        if (config.get(id) == null) {
            config.set(id, defaultValue);
            try {
                config.save(configFile);
            } catch (IOException e) {
                HubParkour.getInstance().getLogger().warning("An attempt was made to insert a missing config value but an error occurred during the attempt. Stack trace:");
                e.printStackTrace();
            }
            return defaultValue;
        }

       return config.getBoolean(id);
    }

    public static String getString(String id, String defaultValue) {
        String value = config.getString(id);
        if (value == null) {
            config.set(id, defaultValue);
            try {
                config.save(configFile);
            } catch (IOException e) {
                HubParkour.getInstance().getLogger().warning("An attempt was made to insert a missing config value but an error occurred during the attempt. Stack trace:");
                e.printStackTrace();
            }
            return defaultValue;
        }

        return value;
    }

    public static List<String> getStringList(String id, List<String> defaultValue) {
        List<String> value = config.getStringList(id);
        if (value == null) {
            config.set(id, defaultValue);
            try {
                config.save(configFile);
            } catch (IOException e) {
                HubParkour.getInstance().getLogger().warning("An attempt was made to insert a missing config value but an error occurred during the attempt. Stack trace:");
                e.printStackTrace();
            }
            return defaultValue;
        }

        return value;
    }

    public static int getInt(String id, int defaultValue) {
        if (config.get(id) == null) {
            config.set(id, defaultValue);
            try {
                config.save(configFile);
            } catch (IOException e) {
                HubParkour.getInstance().getLogger().warning("An attempt was made to insert a missing config value but an error occurred during the attempt. Stack trace:");
                e.printStackTrace();
            }
            return defaultValue;
        }

        return config.getInt(id);
    }

    public static long getLong(String id, long defaultValue) {
        if (config.get(id) == null) {
            config.set(id, defaultValue);
            try {
                config.save(configFile);
            } catch (IOException e) {
                HubParkour.getInstance().getLogger().warning("An attempt was made to insert a missing config value but an error occurred during the attempt. Stack trace:");
                e.printStackTrace();
            }
            return defaultValue;
        }

        return config.getLong(id);
    }

    public static double getDouble(String id, double defaultValue) {
        if (config.get(id) == null) {
            config.set(id, defaultValue);
            try {
                config.save(configFile);
            } catch (IOException e) {
                HubParkour.getInstance().getLogger().warning("An attempt was made to insert a missing config value but an error occurred during the attempt. Stack trace:");
                e.printStackTrace();
            }
            return defaultValue;
        }

        return config.getDouble(id);
    }

    public static String formatTime(long ms) {
        String format = config.getString("Messages.Time-Format");

        if (format == null) {
            config.set("Messages.Time-Format", "ss.MMM");
            try {
                config.save(configFile);
            } catch (IOException e) {
                HubParkour.getInstance().getLogger().warning("An attempt was made to insert a missing config value but an error occurred during the attempt. Stack trace:");
                e.printStackTrace();
            }
            format = "ss.MMM";
        }

        long hours = -1, minutes = -1, seconds = -1;

        if (format.contains("hh")) {
            hours = ms / 3600000;
            ms -= (hours * 3600000);
        }

        if (format.contains("mm")) {

            minutes = ms / 60000;
            ms -= (minutes * 60000);
        }

        if (format.contains("ss")) {
            seconds = ms / 1000;
            ms -= (seconds * 1000);
        }

        return format.replace("hh", hours + "").replace("mm", minutes + "").replace("ss", seconds + "").replace("MMM", ((ms < 100)?((ms < 10)?"00":"0"):"") + ms);
    }

    public static void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public static FileConfiguration getInternal() {
        return internal;
    }
}
