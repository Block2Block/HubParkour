package me.block2block.hubparkour.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class LoggerUtil {
    public static void log(LogLevel level, String message) {
        if(message == null){
            return;
        }

        switch(level){
            case ERROR:
                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&cHub&4Parkour&8] &c" + message));
                break;
            case WARN:
                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&cHub&4Parkour&8] &6" + message));
                break;
            case INFO:
                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&cHub&4Parkour&8] &b" + message));
                break;
            case SUCCESS:
                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&cHub&4Parkour&8] &a" + message));
                break;
            case OUTLINE:
                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&8" + message));
                break;
            case PLUGIN_NAME:
                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + message));
                break;
            case PLUGIN_NAME2:
                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4" + message));
                break;
        }
    }

    public enum LogLevel{ ERROR, WARN, INFO, SUCCESS, OUTLINE, PLUGIN_NAME, PLUGIN_NAME2 }

}