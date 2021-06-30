package me.block2block.hubparkour.listeners;

import me.block2block.hubparkour.Main;
import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.utils.ConfigUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Collections;

@SuppressWarnings("unused")
public class BreakListener implements Listener {

    @SuppressWarnings("unused")
    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (CacheManager.isPoint(e.getBlock().getLocation())) {
            e.setCancelled(true);
            ConfigUtil.sendMessage(e.getPlayer(), "Messages.Break-Disallowed", "You are not allowed to break HubParkour Pressure Plates. In order to remove HubParkour Pressure Plates, please delete the parkour with /parkour delete [id].", true, Collections.emptyMap());
        }
    }

}
