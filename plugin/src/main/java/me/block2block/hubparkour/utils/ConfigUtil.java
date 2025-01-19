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
import java.util.logging.Level;

public class ConfigUtil {

    private static FileConfiguration config;
    private static File configFile;

    private static FileConfiguration internal;

    public static void init(FileConfiguration config, File configFile, FileConfiguration internal) {
        ConfigUtil.config = config;
        ConfigUtil.configFile = configFile;
        ConfigUtil.internal = internal;
    }

    /**
     * Sends a message to a player based on a given message ID from the configuration.
     * If the message ID does not exist in the configuration, the provided default value
     * will be used and added to the configuration file. Variables within the message can
     * be dynamically replaced using the provided variable mappings. Optionally, a prefix
     * can be applied to the message.
     *
     * @param player the player to whom the message will be sent
     * @param id the identifier of the message in the configuration
     * @param defaultValue the default message to use if the message ID is not found in the configuration
     * @param prefix whether a prefix should be included in the message
     * @param variableMappings a map containing placeholder keys and their replacement values for the message
     */
    public static void sendMessage(Player player, String id, String defaultValue, boolean prefix, Map<String, String> variableMappings) {
        if (config.contains(id)) {
            String message = config.getString(id);
            if (!message.isEmpty()) {
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
                HubParkour.getInstance().getLogger().log(Level.WARNING, "An attempt was made to insert a missing config value but an error occurred during the attempt. Stack trace:", e);
            }
            String message = defaultValue;
            for (Map.Entry<String, String> entry : variableMappings.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
            if (HubParkour.isPlaceholders()) {
                message = PlaceholderAPI.setPlaceholders(player, message);
            }
            player.getPlayer().sendMessage(HubParkour.c(prefix, message));
        }
    }

    /**
     * Sends a message to a player based on the given ID. If the message ID does not exist in the configuration
     * or is blank, the specified defaultMessage will be used instead. The message can also include variable mappings
     * that dynamically replace placeholders, and optionally, a prefix can be applied to the message.
     *
     * @param player the player to send the message to
     * @param id the ID of the message in the configuration
     * @param defaultMessage the default message to send if the ID is missing or blank in the configuration
     * @param prefix whether to include a prefix in the message
     * @param variableMappings a map of placeholders and their corresponding replacement values for the message
     */
    public static void sendMessageOrDefault(Player player, String id, String defaultMessage, boolean prefix, Map<String, String> variableMappings) {
        if (config.contains(id) && !config.getString(id).isEmpty()) {
            String message = config.getString(id);
            for (Map.Entry<String, String> entry : variableMappings.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
            if (HubParkour.isPlaceholders()) {
                message = PlaceholderAPI.setPlaceholders(player, message);
            }
            player.getPlayer().sendMessage(HubParkour.c(prefix, message));
        } else {
            config.set(id, defaultMessage);
            try {
                config.save(configFile);
            } catch (IOException e) {
                HubParkour.getInstance().getLogger().log(Level.WARNING, "An attempt was made to insert a missing config value but an error occurred during the attempt. Stack trace:", e);
            }
            player.getPlayer().sendMessage(HubParkour.c(prefix, defaultMessage));
        }
    }

    public static boolean getBoolean(String id, boolean defaultValue) {
        if (!config.contains(id)) {
            config.set(id, defaultValue);
            try {
                config.save(configFile);
            } catch (IOException e) {
                HubParkour.getInstance().getLogger().log(Level.WARNING, "An attempt was made to insert a missing config value but an error occurred during the attempt. Stack trace:", e);
            }
            return defaultValue;
        }

       return config.getBoolean(id);
    }

    public static String getString(String id, String defaultValue) {
        if (!config.contains(id)) {
            config.set(id, defaultValue);
            try {
                config.save(configFile);
            } catch (IOException e) {
                HubParkour.getInstance().getLogger().log(Level.WARNING, "An attempt was made to insert a missing config value but an error occurred during the attempt. Stack trace:", e);
            }
            return defaultValue;
        }

        return config.getString(id);
    }

    public static List<String> getStringList(String id, List<String> defaultValue) {
        if (!config.contains(id)) {
            config.set(id, defaultValue);
            try {
                config.save(configFile);
            } catch (IOException e) {
                HubParkour.getInstance().getLogger().log(Level.WARNING, "An attempt was made to insert a missing config value but an error occurred during the attempt. Stack trace:", e);
            }
            return defaultValue;
        }
        return config.getStringList(id);
    }

    public static int getInt(String id, int defaultValue) {
        if (!config.contains(id)) {
            config.set(id, defaultValue);
            try {
                config.save(configFile);
            } catch (IOException e) {
                HubParkour.getInstance().getLogger().log(Level.WARNING, "An attempt was made to insert a missing config value but an error occurred during the attempt. Stack trace:", e);
            }
            return defaultValue;
        }

        return config.getInt(id);
    }

    @SuppressWarnings("unused")
    public static long getLong(String id, long defaultValue) {
        if (!config.contains(id)) {
            config.set(id, defaultValue);
            try {
                config.save(configFile);
            } catch (IOException e) {
                HubParkour.getInstance().getLogger().log(Level.WARNING, "An attempt was made to insert a missing config value but an error occurred during the attempt. Stack trace:", e);
            }
            return defaultValue;
        }

        return config.getLong(id);
    }

    public static double getDouble(String id, double defaultValue) {
        if (!config.contains(id)) {
            config.set(id, defaultValue);
            try {
                config.save(configFile);
            } catch (IOException e) {
                HubParkour.getInstance().getLogger().log(Level.WARNING, "An attempt was made to insert a missing config value but an error occurred during the attempt. Stack trace:", e);
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
                HubParkour.getInstance().getLogger().log(Level.WARNING, "An attempt was made to insert a missing config value but an error occurred during the attempt. Stack trace:", e);
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

        return format.replace("hh", String.format("%02d", hours))
                .replace("mm", String.format("%02d", minutes))
                .replace("ss", String.format("%02d", seconds))
                .replace("MMM", String.format("%03d", ms));
    }

    public static void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public static FileConfiguration getInternal() {
        return internal;
    }
}
