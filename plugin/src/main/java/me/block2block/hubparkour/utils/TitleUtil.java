package me.block2block.hubparkour.utils;

import me.block2block.hubparkour.HubParkour;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.util.Objects;
import java.util.logging.Level;

public class TitleUtil {

    public static void sendActionBar(Player player, String message, ChatColor color, boolean bold) {
        try {
            if (!HubParkour.isPost1_9()) {
                Object chatTitle = Objects.requireNonNull(getNMSClass("IChatBaseComponent")).getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\": \"" + message + "\",\"color\":\"" + color.name().toLowerCase() + "\",\"bold\":\"" + ((bold)?"true":"false") + "\"}");

                Constructor<?> titleConstructor = Objects.requireNonNull(getNMSClass("PacketPlayOutChat")).getConstructor(getNMSClass("IChatBaseComponent"), byte.class);

                Object packet = titleConstructor.newInstance(chatTitle, (byte) 2);
                sendPacket(player, packet);
            } else {
                TextComponent textComponent = new TextComponent(message);
                textComponent.setColor(color.asBungee());
                textComponent.setBold(bold);

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, textComponent);
            }
        } catch (Exception e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "An exception occurred when attempting to send an action bar message", e);
        }
    }

    private static void sendPacket(Player player, Object packet)
    {
        try
        {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
        }
        catch(Exception ex)
        {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "An exception occurred when attempting to send a packet", ex);
        }
    }

    private static Class<?> getNMSClass(String name)
    {
        try
        {
            return Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + "." + name);
        }
        catch(ClassNotFoundException ex)
        {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "An exception occurred when attempting to fetch an NMS class", ex);
        }
        return null;
    }

}
