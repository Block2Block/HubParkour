package me.block2block.hubparkour.listeners;

import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.utils.ConfigUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.Collections;

public class MountListener implements Listener {

    @EventHandler
    public void onMount(EntityMountEvent e) {
        if (e.getEntity() instanceof Player) {
            Player rider = (Player) e.getEntity();
            if (!CacheManager.isParkour(rider)) {
                if (e.getMount() instanceof Player) {
                    if (ConfigUtil.getBoolean("Settings.Exploit-Prevention.Block-Stacker", true)) {
                        //This is some sort of stacker functionality provided by a hub plugin.
                        Player mount = (Player) e.getMount();
                        if (CacheManager.isParkour(mount)) {
                            ConfigUtil.sendMessage(mount, "Messages.Parkour.Cannot-Stack-In-Parkour", "You cannot stack entities while in a parkour. To leave your current parkour, do /parkour leave.", true, Collections.emptyMap());
                        }
                    }
                }
            } else {
                if (ConfigUtil.getBoolean("Settings.Exploit-Prevention.Block-Mounting", true)) {
                    ConfigUtil.sendMessage(rider, "Messages.Parkour.Cannot-Mount-In-Parkour", "You cannot mount an entity while in a parkour. To leave your current parkour, do /parkour leave.", true, Collections.emptyMap());
                    if (e.getMount() instanceof Player) {
                        if (ConfigUtil.getBoolean("Settings.Exploit-Prevention.Block-Stacker", true)) {
                            //This is some sort of stacker functionality provided by a hub plugin.
                            Player mount = (Player) e.getMount();
                            ConfigUtil.sendMessage(mount, "Messages.Parkour.Cannot-Stack-Player", "You cannot stack this player as they are currently in a parkour. Please wait for them to leave the parkour and try again.", true, Collections.emptyMap());
                        }
                    }
                    e.setCancelled(true);
                }
            }
        } else {
            if (e.getMount() instanceof Player) {
                //This is some sort of stacker provided by a hub plugin.
                if (ConfigUtil.getBoolean("Settings.Exploit-Prevention.Block-Stacker", true)) {
                    //This is some sort of stacker functionality provided by a hub plugin.
                    Player mount = (Player) e.getMount();
                    if (CacheManager.isParkour(mount)) {
                        ConfigUtil.sendMessage(mount, "Messages.Parkour.Cannot-Stack-In-Parkour", "You cannot stack entities while in a parkour. To leave your current parkour, do /parkour leave.", true, Collections.emptyMap());
                    }
                }
            }
        }
    }

}
